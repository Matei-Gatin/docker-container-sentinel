package com.github.matei.util;

import lombok.experimental.UtilityClass;

import java.nio.file.Path;

@UtilityClass
public class Constants
{
    public static final Path SOCKET_FILE = Path.of("/var/run/docker.sock");

    public static final String HTTP_REQUEST = """
            GET /containers/json?all=1 HTTP/1.1\r
            Host: localhost\r
            Connection: close\r
            \r
            """;

    public static final int BUFFER_SIZE = 4096;
}
