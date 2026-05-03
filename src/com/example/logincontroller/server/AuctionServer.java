package com.example.logincontroller.server;

import com.example.logincontroller.dao.ItemDAO;
import com.example.logincontroller.dao.ItemDAOImpl;
import com.example.logincontroller.model.Item;
import com.example.logincontroller.model.ItemFactory;
import com.example.logincontroller.network.ItemDTO;
import com.example.logincontroller.network.Request;
import com.example.logincontroller.network.Response;

import java.io.*;
import java.net.*;
import java.time.LocalDateTime;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AuctionServer {
    private static final int PORT = 2514;

    // Tạo pool chứa tối đa 10 luồng để xử lý 10 kết nối Client đồng thời.
    // Giúp hệ thống không bị nghẽn khi có nhiều người dùng cùng truy cập.
    private final ExecutorService pool = Executors.newFixedThreadPool(10);

    // Database ảo trên bộ nhớ RAM, lưu trữ thông tin sản phẩm
    private final ItemDAO itemDAO = new ItemDAOImpl();

    public void start() {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Server đã khởi động tại cổng " + PORT);

            // Lắng nghe liên tục các kết nối từ Client gửi tới
            while (!serverSocket.isClosed()) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("Có client mới kết nối từ: " + clientSocket.getRemoteSocketAddress());

                // Giao việc cho một luồng trong pool xử lý
                pool.execute(new ClientHandler(clientSocket));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Lớp xử lý yêu cầu riêng biệt cho mỗi Client (đa luồng)
    private class ClientHandler implements Runnable {
        private final Socket socket;

        public ClientHandler(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            try (
                    ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());
                    ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream())
            ) {
                // Đọc gói tin Request (đã triển khai Serializable) gửi từ Client
                Request request = (Request) ois.readObject();

                // Xử lý logic và tạo Response
                Response response = processRequest(request);

                // Trả gói tin Response về lại cho Client
                oos.writeObject(response);
                oos.flush();
            } catch (IOException | ClassNotFoundException e) {
                System.err.println("Lỗi xử lý client: " + e.getMessage());
            }
        }

        private Response processRequest(Request request) {
            try {
                switch (request.getAction()) {
                    case "ADD_ITEM":
                        ItemDTO dto = (ItemDTO) request.getPayload();
                        // Khởi tạo đối tượng Item từ ItemFactory
                        Item item = ItemFactory.createItem(
                                dto.type, dto.id, dto.name, dto.description, dto.startingPrice,
                                LocalDateTime.parse(dto.startTime), LocalDateTime.parse(dto.endTime), dto.extraInfo
                        );
                        itemDAO.addItem(item, dto.type, dto.extraInfo);
                        return new Response("SUCCESS", "Thêm sản phẩm thành công!", item);

                    case "UPDATE_ITEM":
                        ItemDTO updateDto = (ItemDTO) request.getPayload();
                        Item updateItem = ItemFactory.createItem(
                                updateDto.type, updateDto.id, updateDto.name, updateDto.description, updateDto.startingPrice,
                                LocalDateTime.parse(updateDto.startTime), LocalDateTime.parse(updateDto.endTime), updateDto.extraInfo
                        );
                        boolean isUpdated = itemDAO.updateItem(updateItem, updateDto.type, updateDto.extraInfo);
                        if (isUpdated) {
                            return new Response("SUCCESS", "Cập nhật sản phẩm thành công!", updateItem);
                        } else {
                            return new Response("ERROR", "Không tìm thấy sản phẩm cần cập nhật!", null);
                        }

                    case "DELETE_ITEM":
                        String id = (String) request.getPayload();
                        boolean isDeleted = itemDAO.deleteItem(id);
                        if (isDeleted) {
                            return new Response("SUCCESS", "Xóa sản phẩm thành công!", null);
                        } else {
                            return new Response("ERROR", "Không tìm thấy sản phẩm để xóa!", null);
                        }

                    default:
                        return new Response("ERROR", "Lệnh không hợp lệ!", null);
                }
            } catch (Exception e) {
                return new Response("ERROR", "Lỗi: " + e.getMessage(), null);
            }
        }
    }

    public static void main(String[] args) {
        new AuctionServer().start();
    }
}