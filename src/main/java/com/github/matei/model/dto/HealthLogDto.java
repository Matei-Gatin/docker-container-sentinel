package com.github.matei.model.dto;

import com.google.gson.annotations.SerializedName;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
public class HealthLogDto
{
    @SerializedName("Start")
    private String start;

    @SerializedName("End")
    private String end;

    @SerializedName("ExitCode")
    private int exitCode;

    @SerializedName("Output")
    private String output;
}
