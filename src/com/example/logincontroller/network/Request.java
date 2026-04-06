package com.example.logincontroller.network;

public class Request {
    private final String action;
    private final String payload; // JSON data

    public Request(String action, String payload) {
        this.action = action;
        this.payload = payload;
    }

    public String getAction() { return action; }
    public String getPayload() { return payload; }
}