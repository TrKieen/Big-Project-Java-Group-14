package com.example.logincontroller.model.user;

import com.example.logincontroller.model.Entity;

public abstract class User {
    protected String username;
    protected String password;

    public User(String username, String password) {
        this.username = username;
        this.password = password;
    }

    public boolean login(String inputPassword) {
        return password.equals(inputPassword);
    }

    public abstract String getRole();
}