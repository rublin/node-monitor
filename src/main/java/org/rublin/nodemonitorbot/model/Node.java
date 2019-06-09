package org.rublin.nodemonitorbot.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;

import javax.validation.constraints.NotNull;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
public class Node {

    @Id
    private String id;

    @NotNull
    @Indexed(name = "address_index", unique = true)
    private String address;
    private long height;
    private String version;
    private LocalDateTime updated = LocalDateTime.now();
    private boolean available = true;
    private boolean heightOk = true;
    private boolean versionOk = true;
    private Duration online = Duration.ZERO;
    private List<TelegramUser> subscribers = new ArrayList<>();
    private LocalDateTime added = LocalDateTime.now();

    public int uptime() {
        long secondsAfterAdded = Duration.between(added, LocalDateTime.now()).getSeconds();

        return secondsAfterAdded == 0 ? 100 : (int) (online.getSeconds() * 100 / secondsAfterAdded);
    }

    @Override
    public String toString() {
        return "Node{" +
                "\naddress='" + address + '\'' +
                ", \nheight=" + height +
                ", \nversion='" + version + '\'' +
                ", \nuptime=" + uptime() + "%" +
                '}';
    }
}
