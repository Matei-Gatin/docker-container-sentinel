package com.github.matei.model.dto;

import com.google.gson.annotations.SerializedName;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

@Getter
@ToString
public class ContainerDto
{
    @SerializedName("Id")
    private String id;

    @SerializedName("Names")
    private List<String> names;

    @SerializedName("Image")
    private String image;

    @SerializedName("State")
    private String state; // running

    @SerializedName("Status")
    private String status; // Up to 16 seconds

    @SerializedName("Ports")
    private List<PortDto> ports;

    @Setter
    private List<HealthLogDto> log; // reason

    @SerializedName("Health")
    private HealthDetailsDto health;

    @SerializedName("Created")
    private long created;
}
