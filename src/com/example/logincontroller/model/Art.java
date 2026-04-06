package com.example.logincontroller.model;
import java.time.LocalDateTime;

public class Art extends Item {
    private final String artistName;

    public Art(String id, String name, String description, double startingPrice, LocalDateTime startTime, LocalDateTime endTime, String artistName) {
        super(id, name, description, startingPrice, startTime, endTime);
        this.artistName = artistName;
    }

    @Override
    public void printInfo() {
        System.out.println("Nghệ thuật: " + getName() + " - Tác giả: " + artistName);
    }
}