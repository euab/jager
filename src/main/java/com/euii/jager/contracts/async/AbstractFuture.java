package com.euii.jager.contracts.async;

import com.euii.jager.requests.Response;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

public abstract class AbstractFuture {

    private static final ExecutorService SERVICE = Executors.newFixedThreadPool(3);

    private Consumer<Response> DEFAULT_SUCCESS = (Response) -> { };

    private Consumer<Throwable> DEFAULT_FAILURE = (Exception) -> {
        LoggerFactory.getLogger(AbstractFuture.class).error(String.format(
                "Future consumer returned a FAILURE. Details: [%s] %s", Exception.getClass().getSimpleName(),
                Exception.getMessage()
        ), Exception);
    };

    public void send() {
        this.send(null, null);
    }

    public void send(Consumer success) {
        this.send(success, null);
    }

    public void send(final Consumer success, final Consumer<Throwable> failure) {
        SERVICE.submit(() -> handle(
                success == null ? DEFAULT_SUCCESS : success,
                failure == null ? DEFAULT_FAILURE : failure
        ));
    }

    protected abstract void handle(Consumer success, Consumer<Throwable> failiure);
}
