package com.github.matei.model.dto;

import com.google.gson.annotations.SerializedName;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

@Getter
@ToString
public class ContainerDetailsDTO
{
    @SerializedName("Id")
    private String id;

    @SerializedName("Name")
    private String name;

//    @SerializedName("Log")
//    private List<LogDto> log;

    @SerializedName("State")
    private ContainerStateDto state;
}
