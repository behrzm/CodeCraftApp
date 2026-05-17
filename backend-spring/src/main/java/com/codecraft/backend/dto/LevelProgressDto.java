package com.codecraft.backend.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class LevelProgressDto {
    public long id;
    @JsonProperty("user_id")
    public String userId;
    public String language;
    public String track;
    @JsonProperty("level_id")
    public long levelId;
    public int stars;
    public boolean completed;
    @JsonProperty("completed_at")
    public String completedAt;
    public int attempts;
}

