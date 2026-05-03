package com.example.logincontroller.network;

import java.io.Serializable;

public class Request implements Serializable {
    // 1L giúp cố định phiên bản, không bị thay đổi ngầm bởi trình biên dịch
    private static final long serialVersionUID = 1L;

    private final String action;
    private final Object payload;

    public Request(String action, Object payload) {
        this.action = action;
        this.payload = payload;
    }

    public String getAction() { return action; }
    public Object getPayload() { return payload; }
}
