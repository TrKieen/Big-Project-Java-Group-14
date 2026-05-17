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
            List<Item> itemsFromDB = itemDAO.getAllItems();

            // Khai báo thêm bộ nạp dữ liệu lịch sử đặt giá từ Database đám mây
            AuctionSystem.DAO.BidDAO initBidDAO = new AuctionSystem.DAO.BidDAO();

            for (Item item : itemsFromDB) {
                // Khởi tạo phiên đấu giá (Mặc định thời gian là 30 phút)
                Auction auction = new Auction(item);

                // NẠP LẠI LỊCH SỬ ĐẶT GIÁ CŨ TỪ CLOUD (Nếu có)
                List<AuctionSystem.model.auction.Bid> oldBids = initBidDAO.getBidHistory(item.getId());
                if (oldBids != null && !oldBids.isEmpty()) {
                    auction.getBidHistory().addAll(oldBids);
                }

                // Thêm phiên đấu giá hoàn chỉnh vào bộ quản lý Manager
                AuctionManager.getInstance().addAuction(auction);
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

                        // =======================================================
                        // KHẮC PHỤC: Khởi tạo Auction chuẩn theo Constructor mới (Chỉ nhận Item)
                        // =======================================================
                        Auction newAuction = new Auction(item);

                        // Để hệ thống tự động nhận diện OPEN/RUNNING/FINISHED dựa trên startTime và endTime thực tế
                        newAuction.updateStatusBasedOnTime();

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
                        String payload = (String) request.getPayload();
                        String[] parts = payload.split(";");
                        String auctionId = parts[0];
                        String bidderName = parts[1];
                        double bidAmount = Double.parseDouble(parts[2]);

                        // Tìm phiên đấu giá tương ứng trên RAM của Server
                        Auction targetAuction = AuctionManager.getInstance()
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

                        // 1. LƯU LƯỢT ĐẶT GIÁ VÀO DATABASE MÂY (AIVEN)
                        // Gọi lớp DAO mà chúng ta đã viết ở bước trước để INSERT vào bảng `bids`
                        AuctionSystem.DAO.BidDAO bidDAO = new AuctionSystem.DAO.BidDAO();
                        boolean isSaved = bidDAO.addBid(auctionId, bidderName, bidAmount);

                        if (!isSaved) {
                            return new Response("ERROR", "Lỗi đồng bộ dữ liệu đặt giá lên cơ sở dữ liệu đám mây!", null);
                        }

                        // 2. CẬP NHẬT LẠI GIÁ TRÊN RAM SERVER & ĐỒNG BỘ LỊCH SỬ
                        targetAuction.getItem().setCurrentHighestPrice(bidAmount);

                        // Tạo đối tượng người đặt giá và lượt đặt giá mới
                        AuctionSystem.model.user.Bidder currentBidder = new AuctionSystem.model.user.Bidder(bidderName, "");
                        AuctionSystem.model.auction.Bid newBid = new AuctionSystem.model.auction.Bid(currentBidder, bidAmount);

                        // Thêm vào danh sách lịch sử của phiên (để AdminDashboard lấy được vẽ biểu đồ)
                        targetAuction.getBidHistory().add(newBid);

                        // 3. CẬP NHẬT GIÁ CAO NHẤT VÀO BẢNG `items` TRONG DATABASE
                        itemDAO.updateCurrentPrice(auctionId, bidAmount);

                        System.out.println("Server [Cloud Sync]: " + bidderName + " đặt giá " + bidAmount + " cho " + targetAuction.getItem().getName());

                        // Trả về toàn bộ đối tượng targetAuction (đã có lịch sử và người dẫn đầu mới)
                        // để các Client/Admin nhận được và cập nhật UI ngay lập tức
                        return new Response("SUCCESS", "Đặt giá thành công!", targetAuction);
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