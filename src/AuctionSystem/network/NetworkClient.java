package AuctionSystem.network;

import java.io.*;
import java.net.*;
import java.util.List;
import java.util.ArrayList;
import AuctionSystem.model.auction.Auction;

public class NetworkClient {
    private final String host = "localhost";
    private final int port = 2514;

    public boolean sendAddItemRequest(ItemDTO dto) {
        return sendRequest("ADD_ITEM", dto);
    }

    public boolean sendUpdateItemRequest(ItemDTO dto) {
        return sendRequest("UPDATE_ITEM", dto);
    }

    public boolean sendDeleteItemRequest(String itemId) {
        return sendRequest("DELETE_ITEM", itemId);
    }

    private boolean sendRequest(String action, Object payload) {
        try (
                Socket socket = new Socket(host, port);
                ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
                ObjectInputStream ois = new ObjectInputStream(socket.getInputStream())
        ) {

            Request request = new Request(action, payload);

            oos.writeObject(request);
            oos.flush();

            Response response = (Response) ois.readObject();

            System.out.println("Phản hồi từ Server: " + response.getMessage());
            return "SUCCESS".equals(response.getStatus());

        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Lỗi kết nối tới Server: " + e.getMessage());
            return false;
        }
    }

    public List<Auction> sendGetAuctionsRequest() {
        try (
                Socket socket = new Socket(host, port);
                ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
                ObjectInputStream ois = new ObjectInputStream(socket.getInputStream())
        ) {
            Request request = new Request("GET_ALL_AUCTIONS", null);
            oos.writeObject(request);
            oos.flush();

            Response response = (Response) ois.readObject();
            if ("SUCCESS".equals(response.getStatus())) {
                return (List<Auction>) response.getData();
            }
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Lỗi kết nối tới Server: " + e.getMessage());
        }
        return new ArrayList<>();
    }
}