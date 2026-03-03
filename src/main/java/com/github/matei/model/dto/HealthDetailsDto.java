package com.github.matei.model.dto;

import com.google.gson.annotations.SerializedName;
import lombok.Getter;
import lombok.ToString;

import java.util.List;

@Getter
@ToString
public class HealthDetailsDto
{
    @SerializedName("Status")
    private String status;

    @SerializedName("FailingStreak")
    private int failingStreak;

    @SerializedName("Log")
    private List<HealthLogDto> log;
}
