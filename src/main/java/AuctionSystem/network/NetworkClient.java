package AuctionSystem.network;

import java.io.*;
import java.net.*;
import java.util.List;
import java.util.ArrayList;

import AuctionSystem.model.Item;
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
    public List<Item> sendGetAllItemsRequest() {
        try (
                java.net.Socket socket = new java.net.Socket(host, port);
                java.io.ObjectOutputStream oos = new java.io.ObjectOutputStream(socket.getOutputStream());
                java.io.ObjectInputStream ois = new java.io.ObjectInputStream(socket.getInputStream())
        ) {
            // Gửi request lấy toàn bộ sản phẩm lên Server
            Request request = new Request("GET_ALL_ITEMS", null);
            oos.writeObject(request);
            oos.flush();

            // Nhận phản hồi trả về từ Server
            Response response = (Response) ois.readObject();
            if ("SUCCESS".equals(response.getStatus())) {
                // Trả về danh sách sản phẩm lấy được từ
                return (List<Item>) response.getData();
            }
        } catch (Exception e) {
            System.err.println("Lỗi kết nối tới Server khi lấy danh sách sản phẩm: " + e.getMessage());
        }
        return new ArrayList<>();
    }
    public boolean sendStopAuctionRequest(String auctionId) {
        try (
                Socket socket = new Socket(host, port);
                ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
                ObjectInputStream ois = new ObjectInputStream(socket.getInputStream())
        ) {
            Request request = new Request("STOP_AUCTION", auctionId);
            oos.writeObject(request);
            oos.flush();

            Response response = (Response) ois.readObject();
            System.out.println("Phản hồi từ Server khi dừng phiên: " + response.getMessage());
            return "SUCCESS".equals(response.getStatus());
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Lỗi kết nối khi dừng phiên: " + e.getMessage());
            return false;
        }
    }
    // Thêm mới phương thức này vào cuối file NetworkClient.java để phục vụ tính năng Auto-Bid
    public boolean sendRegisterAutoBidRequest(String auctionId, String bidderName, double maxBid, double increment) {
        String payload = auctionId + ";" + bidderName + ";" + maxBid + ";" + increment;
        try (
                Socket socket = new Socket(host, port);
                ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
                ObjectInputStream ois = new ObjectInputStream(socket.getInputStream())
        ) {
            Request request = new Request("REGISTER_AUTO_BID", payload);
            oos.writeObject(request);
            oos.flush();

            Response response = (Response) ois.readObject();
            System.out.println("Phản hồi từ Server khi đăng ký Auto-Bid: " + response.getMessage());
            return "SUCCESS".equals(response.getStatus());
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Lỗi kết nối đăng ký Auto-Bid: " + e.getMessage());
            return false;
        }
    }
    //Gửi chuỗi data đấu giá lên Server
    public boolean sendPlaceBidRequest(String auctionId, String bidderName, double bidAmount) {
        String payload = auctionId + ";" + bidderName + ";" + bidAmount;
        try (
                Socket socket = new Socket(host, port);
                ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
                ObjectInputStream ois = new ObjectInputStream(socket.getInputStream())
        ) {
            Request request = new Request("PLACE_BID", payload);
            oos.writeObject(request);
            oos.flush();

            Response response = (Response) ois.readObject();
            System.out.println("Phản hồi từ Server khi đặt giá: " + response.getMessage());
            return "SUCCESS".equals(response.getStatus());
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Lỗi kết nối đặt giá: " + e.getMessage());
            return false;
        }
    }
}