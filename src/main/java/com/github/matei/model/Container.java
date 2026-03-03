package com.github.matei.model;


import lombok.*;

import java.time.Instant;

@Builder
@ToString
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Getter
public class Container
{
    @EqualsAndHashCode.Include
    private final String id;

    private final ContainerState state;

    private final String name;

    private final String image;

    @Builder.Default
    private final String port = "none";

    @Builder.Default
    private final String healthStatus = "none"; // "HEALTHY" or "UNHEALTHY"

    @Builder.Default
    private final String reason = "unknown"; // if State is HEALTH_CHANGED --> "connection refused"

    @Builder.Default
    private final Integer exitCode = 0; // if State is CRASHED

    @Builder.Default
    private final Boolean oomKilled = false; // if the container was killed for using too much memory

    private final String error; // if state is CONTAINER_CRASHED

    @Builder.Default
    private final Instant stateChangedTimestamp = Instant.now();
}
