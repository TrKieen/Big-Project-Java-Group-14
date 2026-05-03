package com.example.logincontroller.model;

import java.io.Serializable;
import java.time.LocalDateTime;

public class Electronics extends Item implements Serializable {
    private static final long serialVersionUID = 1L;

    private final int warrantyMonths; // Khai báo rõ ràng

    public Electronics(String id, String name, String description, double startingPrice,
                       LocalDateTime startTime, LocalDateTime endTime, int warrantyMonths) {
        super(id, name, description, startingPrice, startTime, endTime);
        this.warrantyMonths = warrantyMonths;
    }

    public int getWarrantyMonths() {
        return warrantyMonths;
    }
    public void printInfo() {
        System.out.println("Điện tử: " + getName() + " - Bảo hành: " + warrantyMonths + " tháng");
    }
}