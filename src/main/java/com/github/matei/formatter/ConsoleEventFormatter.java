package com.github.matei.formatter;

import com.github.matei.model.Container;

import java.time.format.DateTimeFormatter;

public class ConsoleEventFormatter implements EventFormatter
{
    private static final DateTimeFormatter TIMESTAMP_FORMATTER = DateTimeFormatter.ISO_INSTANT;

    @Override
    public String format(Container container)
    {
        String timestamp = TIMESTAMP_FORMATTER.format(container.getStateChangedTimestamp());
        String stateName = container.getState().name();

        String details = switch (container.getState())
        {
            case CONTAINER_STARTED ->
                String.format("name:%s image:%s ports:%s",
                        container.getName(), container.getImage(), container.getPort());

            case CONTAINER_STOPPED ->
                String.format("name:%s exit_code:%d",
                        container.getName(),
                        container.getExitCode());

            case CONTAINER_CRASHED ->
                String.format("name:%s exit_code:%d %s error:%s",
                        container.getName(),
                        container.getExitCode(),
                        container.getOomKilled() ? "(OOMKilled)" : "",
                        container.getError() == null ? "" : container.getError());

            case CONTAINER_HEALTH_CHANGED ->
            {
                if (container.getHealthStatus().equalsIgnoreCase("healthy")) {
                    yield String.format("[%s] name:%s",
                            container.getHealthStatus().toUpperCase(),
                            container.getName());
                } else {
                    yield String.format("[%s] name:%s reason:\"%s\"",
                            container.getHealthStatus().toUpperCase(),
                            container.getName(),
                            formatReason(container.getReason().trim()));
                }
            }
        };

        return String.format("[%s][%s] %s", timestamp, stateName, details);
    }

    // === Private Helper Methods ===
    private String formatReason(String reason)
    {
        int indexOf = reason.lastIndexOf(':');
        return reason.substring(indexOf + 2);
    }
}
