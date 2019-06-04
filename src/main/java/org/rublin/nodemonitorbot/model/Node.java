package org.rublin.nodemonitorbot.model;

import lombok.Data;
import org.springframework.data.annotation.Id;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class Node {

    @Id
    private String id;
    private String domain;
    private String ipAddress;
    private long height;
    private String version;
    private int uptime;
    private LocalDateTime updated;
    private List<TelegramUser> subscribers;
}
