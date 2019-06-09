package org.rublin.nodemonitorbot.service;

import org.rublin.nodemonitorbot.model.TelegramUser;

import java.util.Optional;

public interface TelegramUserService {
    TelegramUser save(TelegramUser user);

    Optional<TelegramUser> get(long chatId);
}
