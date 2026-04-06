package com.example.logincontroller.network;

public class NetworkClient {
    public boolean sendAddItemRequest(ItemDTO dto) {
        // Thực tế: Convert dto sang JSON (Gson) -> Bọc vào Request -> Gửi qua Socket -> Nhận Response
        System.out.println("Network: Đã gửi request ADD_ITEM cho " + dto.name);
        return true; // Giả lập thành công
    }

    public boolean sendUpdateItemRequest(ItemDTO dto) {
        System.out.println("Network: Đã gửi request UPDATE_ITEM cho " + dto.name);
        return true;
    }

    public boolean sendDeleteItemRequest(String itemId) {
        System.out.println("Network: Đã gửi request DELETE_ITEM cho ID: " + itemId);
        return true;
    }
}