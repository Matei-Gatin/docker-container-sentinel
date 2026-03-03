package com.github.matei.client;

import com.github.matei.model.Container;
import com.github.matei.model.dto.ContainerDetailsDTO;
import com.github.matei.model.dto.ContainerDto;

import java.util.List;
import java.util.Optional;

public interface DockerClient
{
    List<ContainerDto> getContainers() throws Exception;

    Optional<ContainerDetailsDTO> getContainerDetails(String containerId) throws Exception;
}
