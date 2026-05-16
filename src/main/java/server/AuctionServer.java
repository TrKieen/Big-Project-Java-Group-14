package server;

import AuctionSystem.model.auction.Auction;
import AuctionSystem.model.auction.AuctionManager;
import AuctionSystem.DAO.ItemDAO;
import AuctionSystem.DAO.ItemDAOImpl;
import AuctionSystem.model.Item;
import AuctionSystem.model.ItemFactory;
import AuctionSystem.network.ItemDTO;
import AuctionSystem.network.Request;
import AuctionSystem.network.Response;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.ArrayList;
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
        // --- TỰ ĐỘNG ĐỌC DỮ LIỆU TỪ MYSQL KHI BẬT SERVER ---
        try {
            System.out.println("Server: Đang tải lại sản phẩm cũ từ MySQL...");
            List<Item> oldItems = itemDAO.getAllItems();
            if (oldItems != null) {
                for (Item oldItem : oldItems) {
                    // Tạo lại phiên đấu giá (mặc định thời lượng đấu giá ví dụ là 60 phút)
                    Auction oldAuction = new Auction(oldItem, 60);
                    oldAuction.startAuction();
                    AuctionManager.getInstance().addAuction(oldAuction);
                }
                System.out.println("Server: Đã phục hồi " + oldItems.size() + " sản phẩm từ Database!");
            }
        } catch (Exception e) {
            System.err.println("Không thể nạp dữ liệu cũ: " + e.getMessage());
        }

        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Server đã khởi động tại cổng " + PORT);

            while (!serverSocket.isClosed()) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("Có client mới kết nối từ: " + clientSocket.getRemoteSocketAddress());
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
            try {
                ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
                ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());

                // Dùng vòng lặp để lắng nghe liên tục nhiều yêu cầu từ 1 Client
                while (!socket.isClosed()) {
                    try {
                        Request request = (Request) ois.readObject();
                        if (request == null) break;

                        Response response = processRequest(request);

                        oos.writeObject(response);
                        oos.flush();
                    } catch (ClassNotFoundException e) {
                        System.err.println("Gói tin không hợp lệ.");
                    } catch (IOException e) {
                        // Client ngắt kết nối (tắt app) -> Thoát vòng lặp
                        System.out.println("Client từ biệt Server.");
                        break;
                    }
                }
            } catch (IOException e) {
                System.err.println("Lỗi khởi tạo luồng I/O: " + e.getMessage());
            } finally {
                try {
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        private Response processRequest(Request request) {
            try {
                switch (request.getAction()) {
                    case "ADD_ITEM":
                        ItemDTO dto = (ItemDTO) request.getPayload();
                        Item item = ItemFactory.createItem(
                                dto.type, dto.id, dto.name, dto.description, dto.startingPrice,
                                LocalDateTime.parse(dto.startTime), LocalDateTime.parse(dto.endTime), dto.extraInfo
                        );
                        itemDAO.addItem(item, dto.type, dto.extraInfo);

                        Duration duration = Duration.between(
                                LocalDateTime.parse(dto.startTime),
                                LocalDateTime.parse(dto.endTime)
                        );
                        int durationMinutes = (int) duration.toMinutes();

                        Auction newAuction = new Auction(item, durationMinutes);
                        newAuction.startAuction();
                        AuctionManager.getInstance().addAuction(newAuction);

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

                    case "GET_ALL_AUCTIONS":
                        List<Auction> auctions = AuctionManager.getInstance().getAuctions();
                        return new Response("SUCCESS", "Lấy danh sách thành công", auctions);
                    case "GET_ALL_ITEMS":
                        // Lấy toàn bộ danh sách sản phẩm từ MySQL thông qua DAO
                        List<Item> allItems = itemDAO.getAllItems();
                        return new Response("SUCCESS", "Lấy danh sách sản phẩm thành công", new ArrayList(allItems));
                    case "PLACE_BID":
                        // Payload gửi lên có thể là một chuỗi định dạng "auctionId;bidderName;bidAmount"
                        String bidData = (String) request.getPayload();
                        String[] parts = bidData.split(";");
                        String auctionId = parts[0];
                        String bidderName = parts[1];
                        double bidAmount = Double.parseDouble(parts[2]);

                        // Tìm phiên đấu giá đang chạy trong RAM Server
                        AuctionSystem.model.auction.Auction targetAuction = AuctionSystem.model.auction.AuctionManager.getInstance()
                                .getAuctions().stream()
                                .filter(a -> a.getItem().getId().equals(auctionId))
                                .findFirst()
                                .orElse(null);

                        if (targetAuction == null || targetAuction.isClosed()) {
                            return new Response("ERROR", "Phiên đấu giá không tồn tại hoặc đã kết thúc!", null);
                        }

                        if (bidAmount <= targetAuction.getItem().getCurrentHighestPrice()) {
                            return new Response("ERROR", "Giá đặt phải cao hơn giá hiện tại!", null);
                        }

                        // 1. Cập nhật giá mới vào đối tượng RAM trên Server
                        targetAuction.getItem().setCurrentHighestPrice(bidAmount);

                        System.out.println("Server: Người dùng " + bidderName + " đã đặt giá " + bidAmount + " cho " + targetAuction.getItem().getName());
                        return new Response("SUCCESS", "Đặt giá thành công!", targetAuction.getItem().getCurrentHighestPrice());
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