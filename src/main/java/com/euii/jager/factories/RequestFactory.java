package com.euii.jager.factories;

import com.euii.jager.requests.Request;

public class RequestFactory {

    public static Request makeGetRequest(String url) {
        return new Request(url);
    }
}
