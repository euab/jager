package com.euii.jager.config;

import com.euii.jager.contracts.config.CastableInterface;

public class Configuration implements CastableInterface {

    private String environment;
    private BotAuth botAuth;

    public BotAuth getBotAuth() {
        return botAuth;
    }

    public class BotAuth {
        private String token;
        private String oauth;
        private int activationDelay;

        public String getToken() {
            return token;
        }

        public String getOauth() {
            return oauth;
        }

        public int getActivationDelay() {
            return activationDelay;
        }
    }
}
