package com.codecraft.backend.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class AchievementDto {
    public long id;
    @JsonProperty("user_id")
    public String userId;
    @JsonProperty("badge_code")
    public String badgeCode;
    @JsonProperty("unlocked_at")
    public String unlockedAt;
}

