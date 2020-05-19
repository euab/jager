package com.euii.jager.api.routes;

import com.euii.jager.api.Prometheus;
import com.euii.jager.api.PrometheusServlet;
import com.euii.jager.contracts.api.AbstractRoute;
import spark.Request;
import spark.Response;

public class GetMetrics extends AbstractRoute {

    private static final PrometheusServlet prometheusServlet = new PrometheusServlet();

    public GetMetrics(Prometheus prometheus) {
        super(prometheus);
    }

    @Override
    public Object handle(Request request, Response response) throws Exception {
        return prometheusServlet.servletGet(request.raw(), response.raw());
    }
}
