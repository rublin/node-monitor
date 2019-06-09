package org.rublin.nodemonitorbot.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;

import java.util.List;

@Getter
@Setter
@Builder
public class TelegramResponseDto {
    private Long id;
    private List<String> messages;
    private ReplyKeyboardMarkup keyboard;
}
