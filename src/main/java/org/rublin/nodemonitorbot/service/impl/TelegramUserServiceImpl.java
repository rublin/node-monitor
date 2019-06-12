package org.rublin.nodemonitorbot.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.rublin.nodemonitorbot.model.TelegramUser;
import org.rublin.nodemonitorbot.repository.TelegramUserRepository;
import org.rublin.nodemonitorbot.service.TelegramUserService;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class TelegramUserServiceImpl implements TelegramUserService {

    private final TelegramUserRepository telegramUserRepository;

    @Override
    public TelegramUser save(TelegramUser user) {
        Optional<TelegramUser> optionalUser = get(user.getTelegramId());
        if (optionalUser.isPresent()) {
            log.warn("User with telegram id {} already present", user.getTelegramId());
            return optionalUser.get();
        }
        return telegramUserRepository.save(user);
    }

    @Override
    public Optional<TelegramUser> get(long chatId) {
        return telegramUserRepository.findByTelegramId(chatId);
    }
}
