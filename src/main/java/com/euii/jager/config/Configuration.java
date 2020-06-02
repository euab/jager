package com.euii.jager.config;

import com.euii.jager.contracts.config.CastableInterface;

import java.util.ArrayList;

public class Configuration implements CastableInterface {

    private String environment;
    private BotAuth botAuth;
    private Database database;
    private ArrayList<String> activities;
    private ArrayList<String> developers;

    public BotAuth getBotAuth() {
        return botAuth;
    }

    public Database getDatabase() {
        return database;
    }

    public ArrayList<String> getActivities() {
        return activities;
    }

    public ArrayList<String> getDevelopers() {
        return developers;
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

    public class Database {
        private String type;
        private String database;
        private String password;
        private String username;
        private String hostname;

        public String getType() {
            return type;
        }

        public String getDatabase() {
            return database;
        }

        public String getPassword() {
            return password;
        }

        public String getUsername() {
            return username;
        }

        public String getHostname() {
            return hostname;
        }
    }
}
