package org.rublin.nodemonitorbot.events;

import lombok.Getter;
import org.rublin.nodemonitorbot.model.Node;
import org.springframework.context.ApplicationEvent;

@Getter
public class OnNodeVersionEvent extends ApplicationEvent {

    private String message;
    private Node node;

    public OnNodeVersionEvent(String message, Node node) {
        super(message);
        this.message = message;
        this.node = node;
    }
}
