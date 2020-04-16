package com.euii.jager.handlers;

public enum EventTypes {

    MESSAGE_RECEIVED_EVENT(MessageCreateEvent.class);

    private final Class instance;

    EventTypes(Class instance) {
        this.instance = instance;
    }

    public <T> Class<T> getInstance() {
        return instance;
    }
}
