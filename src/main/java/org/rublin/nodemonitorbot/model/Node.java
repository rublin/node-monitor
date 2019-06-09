package org.rublin.nodemonitorbot.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;

import javax.validation.constraints.NotNull;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class Node {

    @Id
    private String id;

    @NotNull
    @Indexed(unique = true)
    private String address;
    private long height;
    private String version;
    private LocalDateTime updated;
    private boolean available;
    private boolean heightOk;
    private boolean versionOk;
    private Duration online = Duration.ZERO;
    private List<TelegramUser> subscribers;
    private LocalDateTime added = LocalDateTime.now();

    public int uptime() {
        long secondsAfterAdded = Duration.between(added, LocalDateTime.now()).getSeconds();
        return (int) (online.getSeconds() * 100 / secondsAfterAdded);
    }
}
