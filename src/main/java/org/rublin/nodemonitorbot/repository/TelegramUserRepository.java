package org.rublin.nodemonitorbot.repository;

import org.rublin.nodemonitorbot.model.TelegramUser;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface TelegramUserRepository extends MongoRepository<TelegramUser, String> {

}
