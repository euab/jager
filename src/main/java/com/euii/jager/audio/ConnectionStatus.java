package com.euii.jager.audio;

public enum ConnectionStatus {

    CONNECTED(true),
    NOT_CONNECTED(false, "You need to be connected to a voice channel."),
    USER_LIMIT(false, "The limit of users on this voice channel has been reached."),
    MISSING_PERMISSIONS(false, "Unable to connect to the voice channel due to inadequate permissions.");

    private final boolean success;
    private final String errorMessage;

    ConnectionStatus(boolean success) {
        this(success, null);
    }

    ConnectionStatus(boolean success, String errorMessage) {
        this.success = success;
        this.errorMessage = errorMessage;
    }

    public boolean isSuccess() {
        return success;
    }

    public String getErrorMessage() {
        return errorMessage;
    }
}
