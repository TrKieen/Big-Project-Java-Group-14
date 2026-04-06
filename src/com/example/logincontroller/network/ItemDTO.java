package com.example.logincontroller.network;
import java.time.LocalDateTime;

public class ItemDTO {
    public String id, name, description, type, extraInfo;
    public double startingPrice;
    public String startTime, endTime; // Dùng String ISO-8601 thay vì LocalDateTime để tránh lỗi thư viện JSON

    public ItemDTO(String id, String name, String description, double startingPrice, LocalDateTime startTime, LocalDateTime endTime, String type, String extraInfo) {
        this.id = id; this.name = name; this.description = description;
        this.startingPrice = startingPrice;
        this.startTime = startTime.toString();
        this.endTime = endTime.toString();
        this.type = type; this.extraInfo = extraInfo;
    }
}
