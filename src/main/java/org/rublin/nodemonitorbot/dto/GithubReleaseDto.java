package org.rublin.nodemonitorbot.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.time.ZonedDateTime;

@Data
public class GithubReleaseDto {
    private Long id;
    private String url;

    @JsonProperty("tag_name")
    private String tagName;

    @JsonProperty("created_at")
    private ZonedDateTime createdAt;

    @JsonProperty("published_at")
    private ZonedDateTime publishedAt;

    public String getVersion() {
        return tagName.replace("v.", "");
    }
}
