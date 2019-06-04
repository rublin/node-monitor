package org.rublin.nodemonitorbot.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class NodeInfoResponseDto {
    private String feeAddress;
    private long height;
    private String status;
    private String version;
    private String contact;
}
