package com.euii.jager.api.routes;

import com.euii.jager.Jager;
import com.euii.jager.api.Prometheus;
import com.euii.jager.contracts.api.AbstractRoute;
import org.json.JSONObject;
import spark.Request;
import spark.Response;

public class GetStatus extends AbstractRoute {

    public GetStatus(Prometheus prometheus) {
        super(prometheus);
    }

    @Override
    public Object handle(Request request, Response response) {
        response.type("application/json");

        JSONObject root = new JSONObject();

        Jager jager = prometheus.getJager();

        root
                .put("guilds", jager.getJda().getGuilds().size())
                .put("channels", jager.getJda().getTextChannels().size())
                .put("users", jager.getJda().getUsers().size())
                .put("status", jager.getJda().getStatus());

        return root;
    }
}
