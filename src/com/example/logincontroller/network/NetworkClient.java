package com.example.logincontroller.network;

import java.io.*;
import java.net.*;

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
            // Đóng gói vào Request
            Request request = new Request(action, payload);

            // Gửi request tới Server
            oos.writeObject(request);
            oos.flush();

            // Nhận Response từ Server
            Response response = (Response) ois.readObject();

            System.out.println("Phản hồi từ Server: " + response.getMessage());
            return "SUCCESS".equals(response.getStatus());

        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Lỗi kết nối tới Server: " + e.getMessage());
            return false;
        }
    }
}