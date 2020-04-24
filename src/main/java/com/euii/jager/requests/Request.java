package com.euii.jager.requests;

import com.euii.jager.contracts.async.AbstractFuture;
import okhttp3.OkHttpClient;
import okhttp3.Request.Builder;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class Request extends AbstractFuture {

    private final String url;
    private final RequestType type;

    private final OkHttpClient client;
    private final Builder builder;

    private final Map<String, Object> parameters = new HashMap<>();
    private final Map<String, String> headers = new HashMap<>();

    public Request(String url) {
        this(url, RequestType.GET);
    }

    public Request(String url, RequestType type) {
        this.url = url;
        this.type = type;

        client  = new OkHttpClient();
        builder = new Builder();

        headers.put("User-Agent", "Mozilla/5.0");
    }

    public Request addHeader(String key, String value) {
        headers.put(key, value);
        return this;
    }

    public Request addParameter(String key, Object value) {
        parameters.put(key, value);
        return this;
    }

    @Override
    protected void handle(Consumer success, Consumer<Throwable> failiure) {
        try {
            builder.url(buildUrl());

            for (Map.Entry<String, String> entry : headers.entrySet()) {
                builder.addHeader(entry.getKey(), entry.getValue());
            }

            // TODO: Add a RequestType.POST case when needed.
            switch (type) {
                case GET:
                    builder.get();
                    break;
            }

            success.accept(new Response(client.newCall(builder.build()).execute()));
        } catch (Exception e) {
            failiure.accept(e);
        }
    }

    private URL buildUrl() throws MalformedURLException {
        return new URL(url + (url.contains("?") ? "" : '?') +  formatUrlParameters());
    }

    private String formatUrlParameters() {
        return parameters.entrySet().stream().map(item -> {
            try {
                return String.format("%s=%s", item.getKey(), URLEncoder.encode(item.getValue().toString(),
                        "UTF-8"));
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
                return String.format("%s=%s", item.getKey(), "invalid-format");
            }
        }).collect(Collectors.joining("&"));
    }
}
