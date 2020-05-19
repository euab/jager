package com.euii.jager.contracts.api;

import com.euii.jager.api.Prometheus;
import spark.Route;

public abstract class AbstractRoute implements Route {

    protected final Prometheus prometheus;

    public AbstractRoute(Prometheus prometheus) {
        this.prometheus = prometheus;
    }
}
