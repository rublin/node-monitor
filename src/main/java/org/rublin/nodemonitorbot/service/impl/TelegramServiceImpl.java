package org.rublin.nodemonitorbot.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.rublin.nodemonitorbot.dto.TelegramResponseDto;
import org.rublin.nodemonitorbot.exception.TelegramProcessException;
import org.rublin.nodemonitorbot.model.Node;
import org.rublin.nodemonitorbot.model.TelegramUser;
import org.rublin.nodemonitorbot.service.NodeService;
import org.rublin.nodemonitorbot.service.TelegramService;
import org.rublin.nodemonitorbot.service.TelegramUserService;
import org.rublin.nodemonitorbot.telegram.TelegramCommand;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import static java.lang.String.format;
import static java.util.stream.Collectors.toList;
import static org.rublin.nodemonitorbot.telegram.TelegramCommand.ADD;
import static org.rublin.nodemonitorbot.telegram.TelegramCommand.GET;
import static org.rublin.nodemonitorbot.telegram.TelegramCommand.GET_ALL;
import static org.rublin.nodemonitorbot.telegram.TelegramCommand.SUBSCRIBE;
import static org.rublin.nodemonitorbot.telegram.TelegramCommand.UNSUBSCRIBE;
import static org.rublin.nodemonitorbot.telegram.TelegramKeyboardUtil.defaultKeyboard;
import static org.rublin.nodemonitorbot.telegram.TelegramKeyboardUtil.getAll;
import static org.rublin.nodemonitorbot.telegram.TelegramKeyboardUtil.returnKeyboard;
import static org.rublin.nodemonitorbot.utils.AddressResolver.getIpAddress;

@Slf4j
@Service
@RequiredArgsConstructor
public class TelegramServiceImpl implements TelegramService {

    private static final String HELLO_MESSAGE = "Hello, %s\n\nWhat you want to do with Karbo nodes?";
    private static final String ADDRESS_MESSAGE = "Type node IP or hostname for adding";
    private static final String ALREADY_SUBSCRIBED_MESSAGE = "You already subscribed to node %s";
    private static final String SUBSCRIBED_MESSAGE = "You successfully subscribed to node %s";
    private static final String UNSUBSCRIBED_MESSAGE = "You successfully unsubscribed from node %s";
    private static final String NO_SUBSCRIPTIONS_MESSAGE = "You do not have any subscriptions yet";
    private static final String NEXT_STEP_MESSAGE = "Select the next step";
    private static final String INFO_MESSAGE = "I can show you known Karbo nodes (Get button).\n" +
            "You can add your node (Add button) and subscribe for the notification about availability, height, and version\n\n" +
            "Also, I have REST endpoint with all active nodes:\n" +
            "https://node-monitor.cfapps.io/api/nodes\n\n" +
            "Address for donate: \n" +
            "donate.rublin.org or\n" +
            "KaAxHCPtJaFGDq4xLn3fASf3zVrAmqyE4359zn3r3deVjCeM3CYq7K4Y1pkfZkjfRd1W2VPXVZdA5RBdpc4Vzamo1H4F5qZ\n\n";

    private final NodeService nodeService;
    private final TelegramUserService telegramUserService;

    private Map<Long, TelegramCommand> previousCommand = new ConcurrentHashMap<>();

    @Override
    public TelegramResponseDto process(Message message) {
        log.debug("Received message {}", message);
        if ("/start".equalsIgnoreCase(message.getText())) {
            return processNewUser(message);
        }
        Optional<TelegramCommand> optionalCommand = TelegramCommand.fromCommandName(message.getText());
        if (optionalCommand.isPresent()) {
            return processCommand(optionalCommand.get(), message);
        }
        return processNotCommand(message);
    }

    @Override
    public void cleanPreviousCommand(Long chatId) {
        previousCommand.remove(chatId);
    }

    private TelegramResponseDto processNewUser(Message message) {
        String name = getName(message);
        telegramUserService.save(new TelegramUser(null, message.getChatId(), name));
        return TelegramResponseDto.builder()
                .id(message.getChatId())
                .messages(Collections.singletonList(
                        format(HELLO_MESSAGE, name)))
                .keyboard(defaultKeyboard())
                .build();
    }

    private TelegramResponseDto processCommand(TelegramCommand command, Message message) {
        log.info("Received command {} from user {}", command, message.getChatId());
        ReplyKeyboardMarkup keyboard = defaultKeyboard();
        List<String> responseMessages = new ArrayList<>();
        Long chatId = message.getChatId();
        TelegramUser user = getUser(message);
        switch (command) {
            case ADD:
                previousCommand.put(chatId, command);
                responseMessages.add(ADDRESS_MESSAGE);
                keyboard = returnKeyboard();
                break;

            case SUBSCRIBE:
                Node node = nodeService.get(getIpAddress(addressFromCommand(message.getText())));
                if (node.getSubscribers().contains(user)) {
                    log.debug("User {} already subscribed to node {}", user.getTelegramId(), node.getAddress());
                    responseMessages.add(format(ALREADY_SUBSCRIBED_MESSAGE, node.getAddress()));
                } else {
                    node = nodeService.subscribe(node, user);
                    log.info("User {} successfully subscribed to node {}", user.getTelegramId(), node.getAddress());
                    responseMessages.add(format(SUBSCRIBED_MESSAGE, node.getAddress()));
                }
                break;

            case UNSUBSCRIBE:
                String address = addressFromCommand(message.getText());
                nodeService.unsubscribe(address, user);
                responseMessages.add(format(UNSUBSCRIBED_MESSAGE, address));
                break;

            case GET:
                previousCommand.put(chatId, command);
                responseMessages.add(ADDRESS_MESSAGE.concat("\nOr select ").concat(GET_ALL.getCommandName()));
                keyboard = getAll();
                break;

            case GET_ALL:
                previousCommand.remove(chatId);
                List<String> nodes = nodeService.getAll().stream()
                        .map(n -> nodeToTelegram(n, user))
                        .collect(toList());
                responseMessages.addAll(nodes);
                break;

            case MY_SUBSCRIPTIONS:
                List<Node> mySubscriptions = nodeService.mySubscriptions(getUser(message));
                responseMessages.addAll(mySubscriptions.stream()
                        .map(n -> nodeToTelegram(n, user))
                        .collect(toList()));
                if (responseMessages.isEmpty()) {
                    responseMessages.add(NO_SUBSCRIPTIONS_MESSAGE);
                }
                break;

            case RETURN:
                previousCommand.remove(chatId);
                responseMessages.add(NEXT_STEP_MESSAGE);
                break;

            case INFO:
                responseMessages.add(INFO_MESSAGE);
                break;

        }
        return TelegramResponseDto.builder()
                .id(chatId)
                .keyboard(keyboard)
                .messages(responseMessages)
                .build();
    }

    private String addressFromCommand(String text) {
        return text.substring(text.indexOf("_") + 1).replaceAll("_", "\\.");
    }

    private String nodeToTelegram(Node node, TelegramUser user) {
        StringBuilder sb = new StringBuilder();
        sb.append("Address: ").append(node.getAddress()).append("\n");
        sb.append("Height: ").append(node.getHeight()).append(isOkEmoji(node.isHeightOk()));
        sb.append("Version: ").append(node.getVersion()).append(isOkEmoji(node.isVersionOk()));
        sb.append("Active: ").append(node.isAvailable() ? "active" : "not active").append(isOkEmoji(node.isAvailable()));
        sb.append("\n\n");
        sb.append(internalSubscriptionCommand(node.getSubscribers().contains(user), node.getAddress()));
        return sb.toString();
    }

    private String internalSubscriptionCommand(boolean subscribed, String address) {
        address = address.replaceAll("\\.", "_");
        if (subscribed) {
            return "Unsubscribe: " + UNSUBSCRIBE.getCommandName().concat(address);
        }
        return "Subscribe: " + SUBSCRIBE.getCommandName().concat(address);
    }

    private String isOkEmoji(boolean isOk) {
        return isOk ? " \uD83D\uDC4C\n" : " \uD83D\uDC4E\n";
    }

    private TelegramResponseDto processNotCommand(Message message) {
        ReplyKeyboardMarkup keyboard = defaultKeyboard();
        List<String> responseMessages = new ArrayList<>();
        Long chatId = message.getChatId();
        TelegramCommand command = previousCommand.get(chatId);
        previousCommand.remove(chatId);
        TelegramUser user = getUser(message);
        if (command == ADD) {
            Node node = nodeService.registerNode(getIpAddress(message.getText()));
            node = nodeService.subscribe(node, user);
            responseMessages.add("Node successfully added\n\n" + node.toString());
        } else if (command == GET) {
            responseMessages.add(nodeToTelegram(nodeService.get(getIpAddress(message.getText())), user));
        } else {
            throw new TelegramProcessException("Unknown command: " + message.getText());
        }

        return TelegramResponseDto.builder()
                .id(chatId)
                .messages(responseMessages)
                .keyboard(keyboard)
                .build();
    }

    private TelegramUser getUser(Message message) {
        Optional<TelegramUser> optional = telegramUserService.get(message.getChatId());
        if (optional.isPresent()) {
            return optional.get();
        } else {
            log.warn("For some reason there is no user with id {}. Will create it now", message.getChatId());
            return telegramUserService.save(
                    new TelegramUser(null, message.getChatId(), getName(message)));
        }
    }

    private String getName(Message message) {
        String userName = message.getFrom().getUserName();
        String name = message.getFrom().getFirstName();
        if (Objects.nonNull(name))
            return name;
        else if (Objects.nonNull(userName))
            return userName;
        return "my anonymous friend";
    }
}
