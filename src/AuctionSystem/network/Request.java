package AuctionSystem.network;

import java.io.Serial;
import java.io.Serializable;

public class Request implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
    private final String action;
    private final Object payload;

    public Request(String action, Object payload) {
        this.action = action;
        this.payload = payload;
    }

    public String getAction() {
        return action;
    }
    public Object getPayload() {
        return payload;
    }
}