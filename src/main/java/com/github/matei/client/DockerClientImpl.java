package com.github.matei.client;

import com.github.matei.model.dto.ContainerDetailsDTO;
import com.github.matei.model.dto.ContainerDto;
import com.github.matei.util.Constants;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;


import java.io.IOException;
import java.net.StandardProtocolFamily;
import java.net.UnixDomainSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;


public class DockerClientImpl implements DockerClient
{
    private final Gson gson;
    private final UnixDomainSocketAddress socketAddress;

    public DockerClientImpl()
    {
        this.socketAddress = UnixDomainSocketAddress.of(Constants.SOCKET_FILE);
        this.gson = new GsonBuilder()
                .setPrettyPrinting()
                .create();
    }

    @Override
    public List<ContainerDto> getContainers() throws Exception
    {
        String httpRequest = Constants.HTTP_REQUEST;
        String response = executeRequest(httpRequest);
        String jsonBody = extractJsonArray(response);

        return parseContainerList(jsonBody);
    }

    @Override
    public Optional<ContainerDetailsDTO> getContainerDetails(String containerId) throws Exception
    {
        String httpRequest = buildDetailsRequest(containerId);
        String response = executeRequest(httpRequest);
        String jsonBody = extractJsonObject(response);

        ContainerDetailsDTO details = gson.fromJson(jsonBody, ContainerDetailsDTO.class);
        return Optional.ofNullable(details);
    }

    // === Private Helper Methods ===
    private String executeRequest(String httpRequest) throws IOException
    {
        try (SocketChannel channel = SocketChannel.open(StandardProtocolFamily.UNIX))
        {
            channel.connect(socketAddress);

            // send request
            ByteBuffer writeBuffer = ByteBuffer.wrap(httpRequest.getBytes());
            while (writeBuffer.hasRemaining())
            {
                channel.write(writeBuffer);
            }

            // read response
            StringBuilder responseBuilder = new StringBuilder();
            Optional<String> chunk = readChunk(channel);

            while (chunk.isPresent())
            {
                responseBuilder.append(chunk.get());
                chunk = readChunk(channel);
            }

            return responseBuilder.toString();
        }
    }

    private Optional<String> readChunk(SocketChannel channel) throws IOException
    {
        ByteBuffer buffer = ByteBuffer.allocate(Constants.BUFFER_SIZE);
        int bytesRead = channel.read(buffer);

        if (bytesRead < 0)
        {
            return Optional.empty();
        }

        byte[] bytes = new byte[bytesRead];
        buffer.flip();
        buffer.get(bytes);
        String message = new String(bytes);
        return Optional.of(message);
    }

    private String buildDetailsRequest(String containerId)
    {
        return "GET /containers/" + containerId + "/json HTTP/1.1\r\n" +
                "Host: localhost\r\n" +
                "Connection: close\r\n" +
                "\r\n";
    }

    private String extractJsonArray(String httpResponse)
    {
        String body = extractBody(httpResponse);

        int jsonStart = body.indexOf('[');
        int jsonEnd = body.lastIndexOf(']');

        if (jsonStart == -1 || jsonEnd == -1)
        {
            throw new IllegalArgumentException("Invalid response: no JSON array found");
        }

        return body.substring(jsonStart, jsonEnd + 1);
    }


    private String extractJsonObject(String httpResponse)
    {
        String body = extractBody(httpResponse);

        int jsonStart = body.indexOf('{');
        int jsonEnd = body.lastIndexOf('}');

        if (jsonStart == -1 || jsonEnd == -1)
        {
            throw new IllegalArgumentException("Invalid response: no JSON object found");
        }

        return body.substring(jsonStart, jsonEnd + 1);
    }

    private String extractBody(String httpResponse)
    {
        int headerEnd = httpResponse.indexOf("\r\n\r\n");

        if (headerEnd == -1)
        {
            throw new IllegalArgumentException("Invalid HTTP response: no header/body separator");
        }

        return httpResponse.substring(headerEnd + 4);
    }

    private List<ContainerDto> parseContainerList(String json)
    {
        JsonArray rootArray = JsonParser.parseString(json).getAsJsonArray();
        List<ContainerDto> dtos = gson.fromJson(
                rootArray,
                new TypeToken<List<ContainerDto>>() {}.getType());

        return new ArrayList<>(dtos);
    }
}
