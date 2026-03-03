package com.github.matei;

import com.github.matei.client.DockerClient;
import com.github.matei.client.DockerClientImpl;
import com.github.matei.formatter.ConsoleEventFormatter;
import com.github.matei.formatter.EventFormatter;
import com.github.matei.monitor.ContainerMonitor;
import com.github.matei.monitor.EventDetector;

public class Main {
    public static void main(String[] args) throws Exception
    {
        DockerClient client = new DockerClientImpl();
        EventFormatter formatter = new ConsoleEventFormatter();
        EventDetector detector = new EventDetector(client);

        try (ContainerMonitor cm = new ContainerMonitor(client,
                formatter,
                detector
        ))
        {
            cm.start();
        }
    }
}