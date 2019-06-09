package org.rublin.nodemonitorbot.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TelegramUser {

    @Id
    private String id;
    private Long telegramId;
    private String name;
}
