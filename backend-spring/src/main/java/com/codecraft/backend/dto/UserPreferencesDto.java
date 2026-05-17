package com.codecraft.backend.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class UserPreferencesDto {
    public long id;
    @JsonProperty("user_id")
    public String userId;
    @JsonProperty("dark_theme")
    public boolean darkTheme;
    @JsonProperty("sound_enabled")
    public boolean soundEnabled;
    @JsonProperty("notifications_enabled")
    public boolean notificationsEnabled;
}

