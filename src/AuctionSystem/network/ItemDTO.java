package AuctionSystem.network;

import java.time.LocalDateTime;
import java.io.Serializable;

public class ItemDTO implements Serializable {
    public String id, name, description, type, extraInfo;
    public double startingPrice;
    public String startTime, endTime;

    public ItemDTO(String id, String name, String description, double startingPrice, LocalDateTime startTime, LocalDateTime endTime, String type, String extraInfo) {
        this.id = id; this.name = name; this.description = description;
        this.startingPrice = startingPrice;
        this.startTime = startTime.toString();
        this.endTime = endTime.toString();
        this.type = type; this.extraInfo = extraInfo;
    }
}
