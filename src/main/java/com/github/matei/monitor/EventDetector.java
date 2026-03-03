package com.github.matei.monitor;

import com.github.matei.client.DockerClient;
import com.github.matei.model.Container;
import com.github.matei.model.ContainerMapper;
import com.github.matei.model.ContainerState;
import com.github.matei.model.dto.ContainerDetailsDTO;
import com.github.matei.model.dto.ContainerDto;
import lombok.extern.slf4j.Slf4j;

import java.util.*;

@Slf4j
public class EventDetector
{
    private final DockerClient dockerClient;
    private final Map<String, ContainerDto> previousContainers = new HashMap<>(); // id : ContainerDto

    public EventDetector(DockerClient dockerClient)
    {
        this.dockerClient = dockerClient;
    }

    public List<Container> detectEvents(List<ContainerDto> containers) throws Exception
    {
        List<Container> events = new ArrayList<>();

        for (var currentContainer : containers)
        {
            ContainerDto prevState = previousContainers.get(currentContainer.getId());

            events.addAll(detectContainerEvents(prevState, currentContainer));

            previousContainers.put(currentContainer.getId(), currentContainer);
        }

        return events;
    }

    // === Helper Methods ===
    private List<Container> detectContainerEvents(ContainerDto previousRun, ContainerDto currentRun) throws Exception
    {
        List<Container> events = new ArrayList<>();

        String currentState = currentRun.getState().toLowerCase();
        String previousState = previousRun != null ? previousRun.getState().toLowerCase() : null;

        // CONTAINER_STARTED
        if (currentState.equals("running"))
        {
            if (previousState == null || !previousState.equals("running"))
            {
                Container cont = ContainerMapper.toContainer(currentRun, ContainerState.CONTAINER_STARTED);
                events.add(cont);
            }
        }

        // CONTAINER_STOPPED OR CONTAINER_CRASHED:
        else if ("running".equals(previousState) && "exited".equals(currentState))
        {
            events.add(detectStoppedOrCrashed(currentRun));
        }

        // CONTAINER_HEALTH_CHANGED
        if (previousRun != null)
        {
            detectHealthChange(previousRun, currentRun).ifPresent(events::add);
        }

        return events;
    }

    private Container detectStoppedOrCrashed(ContainerDto containerDto)
    {
        try
        {
            Optional<ContainerDetailsDTO> details = dockerClient.getContainerDetails(containerDto.getId());

            if (details.isPresent())
            {
                int exitCode = details.get().getState().getExitCode();
                boolean oomKilled = details.get().getState().isOomKilled();

                if (exitCode == 0 && !oomKilled)
                {
                    return ContainerMapper.toContainer(containerDto, ContainerState.CONTAINER_STOPPED, exitCode, false);
                } else
                {
                    return ContainerMapper.toContainer(containerDto, ContainerState.CONTAINER_CRASHED, exitCode, oomKilled);
                }
            }

        } catch (Exception e)
        {
            log.error("Failed to get container details for {}: {}", containerDto.getId(), e.getMessage());
        }

        return ContainerMapper.toContainer(containerDto, ContainerState.CONTAINER_STOPPED);
    }

    private Optional<Container> detectHealthChange(ContainerDto previousRun, ContainerDto currentRun) throws Exception
    {
        if (previousRun.getHealth() != null &&
                previousRun.getHealth().getStatus() != null &&
                currentRun.getHealth() != null &&
                currentRun.getHealth().getStatus() != null)
        {
            String prevRunHealthStatus = previousRun.getHealth().getStatus();
            String currRunHealthStatus = currentRun.getHealth().getStatus();

            if (prevRunHealthStatus.equals(currRunHealthStatus))
            {
                return Optional.empty();
            }

            Optional<ContainerDetailsDTO> containerDetailsDto = dockerClient.getContainerDetails(currentRun.getId());

            if (containerDetailsDto.isPresent())
            {
                currentRun.setLog(containerDetailsDto.get().getState().getHealth().getLog());
                String health = containerDetailsDto.get().getState().getHealth().getStatus();
                String output = containerDetailsDto.get().getState().getHealth().getLog().getLast().getOutput();

                if ("healthy".equals(prevRunHealthStatus) && "unhealthy".equals(currRunHealthStatus))
                {
                    return Optional.of(ContainerMapper.toContainerWithHealth(currentRun,
                            ContainerState.CONTAINER_HEALTH_CHANGED,
                            health,
                            output));
                } else if ("starting".equals(prevRunHealthStatus) && "unhealthy".equals(currRunHealthStatus))
                {
                    return Optional.of(ContainerMapper.toContainerWithHealth(currentRun,
                            ContainerState.CONTAINER_HEALTH_CHANGED,
                            health,
                            output));
                }

                return Optional.of(ContainerMapper.toContainerWithHealth(currentRun,
                        ContainerState.CONTAINER_HEALTH_CHANGED,
                        health,
                        output));
            }
        }

        return Optional.empty();
    }
}
