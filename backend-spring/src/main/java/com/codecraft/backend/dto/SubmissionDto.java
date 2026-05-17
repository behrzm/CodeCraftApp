package com.codecraft.backend.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class SubmissionDto {
    @JsonProperty("user_id")
    public String userId;
    @JsonProperty("level_id")
    public long levelId;
    public String language;
    public String track;
    public String code;
    public boolean correct;
    public int xpEarned;
    @JsonProperty("hp_used")
    public int hpUsed;
    @JsonProperty("submitted_at")
    public String submittedAt;
}

