package com.example.logincontroller.model;
import java.time.LocalDateTime;

public class Vehicle extends Item {
    private final String licensePlate;

    public Vehicle(String id, String name, String description, double startingPrice, LocalDateTime startTime, LocalDateTime endTime, String licensePlate) {
        super(id, name, description, startingPrice, startTime, endTime);
        this.licensePlate = licensePlate;
    }

    @Override
    public void printInfo() {
        System.out.println("Xe cộ: " + getName() + " - Biển số: " + licensePlate);
    }
}