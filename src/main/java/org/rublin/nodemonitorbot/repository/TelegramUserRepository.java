package org.rublin.nodemonitorbot.repository;

import org.rublin.nodemonitorbot.model.TelegramUser;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface TelegramUserRepository extends MongoRepository<TelegramUser, String> {
    Optional<TelegramUser> findByTelegramId(Long telegramId);
}
