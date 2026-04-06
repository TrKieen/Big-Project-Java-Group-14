package com.example.logincontroller.model;
import java.time.LocalDateTime;

public class ItemFactory {
    public static Item createItem(String type, String id, String name, String desc, double price, LocalDateTime start, LocalDateTime end, String extra) {
        return switch (type.toLowerCase()) {
            case "sản phẩm điện tử" -> {
                int warranty = extra.isEmpty() ? 0 : Integer.parseInt(extra);
                yield new Electronics(id, name, desc, price, start, end, warranty);
            }
            case "tác phẩm nghệ thuật" -> new Art(id, name, desc, price, start, end, extra);
            case "phương tiện di chuyển" -> new Vehicle(id, name, desc, price, start, end, extra);
            default -> throw new IllegalArgumentException("Loại sản phẩm không hỗ trợ!");
        };
    }
}