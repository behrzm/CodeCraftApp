package com.codecraft.backend.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ProfileDto {

    public String id;
    @JsonProperty("display_name")
    public String displayName;
    public String email;
    public int xp;
    public int level;
    public int streak;

}
