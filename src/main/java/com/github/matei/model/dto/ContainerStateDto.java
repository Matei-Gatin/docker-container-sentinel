package com.github.matei.model.dto;

import com.google.gson.annotations.SerializedName;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@ToString
public class ContainerStateDto
{
    @SerializedName("Status")
    private String status; // running, exited, etc...

    @SerializedName("ExitCode")
    private int exitCode;

    @SerializedName("OOMKilled")
    private boolean oomKilled;

    @SerializedName("Error")
    private String error = "unknown";

    @SerializedName("Health")
    private HealthDetailsDto health;
}
