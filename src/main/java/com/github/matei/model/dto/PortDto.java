package com.github.matei.model.dto;


import com.google.gson.annotations.SerializedName;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
public class PortDto
{
    @SerializedName("IP")
    private String ip;

    @SerializedName("PrivatePort")
    private Integer privatePort;

    @SerializedName("PublicPort")
    private Integer publicPort;

    @SerializedName("Type")
    private String type;
}