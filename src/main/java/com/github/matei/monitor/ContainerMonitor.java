package com.github.matei.monitor;

import com.github.matei.client.DockerClient;
import com.github.matei.formatter.EventFormatter;
import com.github.matei.model.Container;
import com.github.matei.model.dto.ContainerDto;
import lombok.extern.slf4j.Slf4j;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

@Slf4j
public class ContainerMonitor implements AutoCloseable
{
    private final DockerClient apiClient;
    private final EventFormatter formatter;
    private final EventDetector eventDetector;

    private volatile boolean running = false;
    private final ScheduledExecutorService scheduled;

    private final AtomicLong totalPollCount = new AtomicLong(0);
    private final AtomicLong totalEventsReported = new AtomicLong(0);
    private final AtomicReference<Instant> monitoringStartTime = new AtomicReference<>();

    public ContainerMonitor(DockerClient apiClient, EventFormatter formatter, EventDetector eventDetector)
    {
        this.apiClient = apiClient;
        this.formatter = formatter;
        this.eventDetector = eventDetector;
        this.scheduled = Executors.newSingleThreadScheduledExecutor();
    }

    public void start()
    {
        if (running)
        {
            throw new IllegalStateException("Monitor is already running");
        }

        this.running = true;

        this.monitoringStartTime.set(Instant.now());

        log.info("Starting monitoring containers...");
        log.info("Start time: {}", this.monitoringStartTime.get());
        log.info("Press CTRL + C to stop the process.");
        System.out.println();

        scheduled.scheduleAtFixedRate(this::pollAndProcessEvents,
                0,
                5,
                TimeUnit.SECONDS);

        while (running) // keep reference to main Thread
        {
            try
            {
                Thread.sleep(1000);
            } catch (InterruptedException e)
            {
                Thread.currentThread().interrupt();
                break;
            }
        }

        log.info("Monitoring stopped.");
        printSummary();
    }

    private void pollAndProcessEvents()
    {
        try
        {
            totalPollCount.incrementAndGet();

            List<ContainerDto> containerDtos = this.apiClient.getContainers();
            List<Container> filteredContainers = eventDetector.detectEvents(containerDtos);

            for (var filteredCont : filteredContainers)
            {
                System.out.println(formatter.format(filteredCont));
                totalEventsReported.incrementAndGet();
            }
        } catch (Exception e)
        {
            log.error("Error polling containers: {}", e.getMessage());
        }
    }

    private void printSummary()
    {
        Duration uptime = Duration.between(this.monitoringStartTime.get(), Instant.now());
        System.out.println();
        System.out.println("=== Monitoring summary ===");
        System.out.println("Total runtime: " + formatDuration(uptime));
        System.out.println("Total polls: " + totalPollCount);
        System.out.println("Events reported: " + totalEventsReported);
        System.out.println("=".repeat(30));
    }

    private String formatDuration(Duration duration)
    {
        long hours = duration.toHours();
        long minutes = duration.toMinutesPart();
        long seconds = duration.toSecondsPart();

        StringBuilder sb = new StringBuilder();

        if (hours > 0) sb.append(hours).append("h ");
        if (minutes > 0) sb.append(minutes).append("m ");
        if (seconds > 0 || sb.isEmpty()) sb.append(seconds).append("s");

        return sb.toString().trim();
    }

    @Override
    public void close() throws Exception
    {
        if (!running)
        {
            return;
        }

        log.info("Shutting down ExecutorService");
        this.running = false;
        this.scheduled.shutdown();

        if (!this.scheduled.awaitTermination(5, TimeUnit.SECONDS))
        {
            this.scheduled.shutdownNow();
        }
    }
}
