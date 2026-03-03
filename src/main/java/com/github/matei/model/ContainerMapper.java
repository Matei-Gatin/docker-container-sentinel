package com.github.matei.model;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

import com.github.matei.model.dto.ContainerDto;
import com.github.matei.model.dto.HealthLogDto;
import com.github.matei.model.dto.PortDto;

public class ContainerMapper
{
    public static Container toContainer(ContainerDto dto, ContainerState state)
    {
        return Container.builder()
                .id(dto.getId())
                .state(state)
                .name(parseContainerName(dto.getNames()))
                .image(dto.getImage())
                .port(formatPorts(dto.getPorts()))
//                .healthStatus(dto.get() != null ? dto.getHealth() : "none")
//                .reason(parseReason(dto.getLog()))
                .stateChangedTimestamp(Instant.now())
                .build();
    }

    public static Container toContainer(ContainerDto dto, ContainerState state, int exitCode, boolean oomKilled)
    {
        return Container.builder()
                .id(dto.getId())
                .name(parseContainerName(dto.getNames()))
                .image(dto.getImage())
                .port(formatPorts(dto.getPorts()))
                .state(state)
                .exitCode(exitCode)
                .oomKilled(oomKilled)
//                .healthStatus(dto.getHealth() != null ? dto.getHealth().getStatus() : "none")
                .stateChangedTimestamp(Instant.now())
                .build();
    }

    public static Container toContainerWithHealth(ContainerDto dto, ContainerState state, String healthStatus, String reason)
    {
           return Container.builder()
                   .id(dto.getId())
                   .state(state)
                   .name(parseContainerName(dto.getNames()))
                   .image(dto.getImage())
                   .port(formatPorts(dto.getPorts()))
                   .healthStatus(healthStatus)
                   .reason(reason)
                   .stateChangedTimestamp(Instant.now())
                   .build();
    }

    // Helper methods
    private static String parseContainerName(List<String> names)
    {
        if (names == null || names.isEmpty())
        {
            return "unknown";
        }

        String name = names.getFirst();
        return name.startsWith("/") ? name.substring(1) : name;
    }

    private static String formatPorts(List<PortDto> ports)
    {
        if (ports == null || ports.isEmpty())
        {
            return "none";
        }

        return ports.stream()
                .map(ContainerMapper::formatSinglePorts)
                .filter(s -> !s.isEmpty())
                .distinct()
                .collect(Collectors.joining(", "));
    }

    private static String formatSinglePorts(PortDto port)
    {
        Integer publicPort = port.getPublicPort();
        Integer privatePort = port.getPublicPort();
        String type = port.getType();

        return String.format("%s%s/%s",
                publicPort == null ? "" : publicPort + ":",
                privatePort == null ? "" : privatePort,
                type == null ? "" : type);
    }
}
