package org.rublin.nodemonitorbot.service;

import org.rublin.nodemonitorbot.dto.TelegramResponseDto;
import org.telegram.telegrambots.meta.api.objects.Message;

public interface TelegramService {
    TelegramResponseDto process(Message message);

    void cleanPreviousCommand(Long chatId);
}
