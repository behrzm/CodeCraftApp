package com.codecraft.backend.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class LevelDto {
    public long id;
    public String language;
    public String track;
    @JsonProperty("level_number")
    public int levelNumber;
    public String title;
    public String description;
    public int difficulty;
    @JsonProperty("estimated_time")
    public int estimatedTime;
    @JsonProperty("initial_code")
    public String initialCode;
    @JsonProperty("test_cases")
    public String testCases; // JSON string
    public String hints; // JSON string
    public String solution;
    @JsonProperty("xp_reward")
    public int xpReward;
}

