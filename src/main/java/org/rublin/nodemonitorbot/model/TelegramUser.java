package org.rublin.nodemonitorbot.model;

import lombok.Data;
import org.springframework.data.annotation.Id;

@Data
public class TelegramUser {

    @Id
    private String id;
    private Long telegramId;
    private String name;
}
