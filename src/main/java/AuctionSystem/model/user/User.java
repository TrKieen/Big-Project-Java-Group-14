package AuctionSystem.model.user;

import java.io.Serializable;

public abstract class User implements Serializable {
    protected String username;
    protected String password;

    public String getUsername() {
        return username;
    }

    public User(String username, String password) {
        this.username = username;
        this.password = password;
    }

    public boolean login(String inputPassword) {
        return password.equals(inputPassword);
    }

    public abstract String getRole();
}