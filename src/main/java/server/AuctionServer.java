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
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AuctionServer {
    private static final int PORT = System.getenv("PORT") != null
            ? Integer.parseInt(System.getenv("PORT")) : 2514;

    private static final DateTimeFormatter DT_FMT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    // Tạo pool chứa tối đa 10 luồng để xử lý 10 kết nối Client đồng thời.
    // Giúp hệ thống không bị nghẽn khi có nhiều người dùng cùng truy cập.
    private final ExecutorService pool = Executors.newFixedThreadPool(10);

    // Database ảo trên bộ nhớ RAM, lưu trữ thông tin sản phẩm
    private final ItemDAO itemDAO = new ItemDAOImpl();

    public void start() {
        // --- TỰ ĐỘNG ĐỌC DỮ LIỆU TỪ MYSQL KHI BẬT SERVER ---
        try {
            List<Item> savedItems = itemDAO.getAllItems();
            List<Auction> currentAuctions = AuctionManager.getInstance().getAuctions();

            // >>> BỔ SUNG: Khởi tạo lớp BidDAO để load lịch sử từ DB lên RAM Server <<<
            AuctionSystem.DAO.BidDAO bidDAO = new AuctionSystem.DAO.BidDAO();

            for (Item item : savedItems) {
                boolean isAlreadyLoaded = currentAuctions.stream()
                        .anyMatch(a -> a.getItem().getId().equals(item.getId()));

                if (!isAlreadyLoaded) {
                    Auction auction = new Auction(item);

                    // >>> BỔ SUNG: Gọi hàm nạp lịch sử các lượt đặt giá cũ vào phiên đấu giá <<<
                    try {
                        // Bạn kiểm tra trong lớp BidDAO của bạn xem tên hàm là gì nhé, thông thường là:
                        List<AuctionSystem.model.auction.Bid> oldBids = bidDAO.getBidHistory(item.getId());
                        if (oldBids != null) {
                            auction.getBidHistory().addAll(oldBids);
                        }
                    } catch (Exception e) {
                        System.err.println("Chưa nạp được lịch sử bids cũ của sản phẩm: " + item.getId());
                    }

                    currentAuctions.add(auction);
                }
            }
            System.out.println("Server [MySQL]: Đã đồng bộ thành công " + currentAuctions.size() + " phiên đấu giá kèm lịch sử.");
        } catch (Exception e) {
            System.err.println("Lỗi nạp dữ liệu MySQL khi khởi động: " + e.getMessage());
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
                                LocalDateTime.parse(dto.startTime, DT_FMT),
                                LocalDateTime.parse(dto.endTime, DT_FMT),
                                dto.extraInfo
                        );
                        itemDAO.addItem(item, dto.type, dto.extraInfo);

                        Auction newAuction = new Auction(item);

                        // Để hệ thống tự động nhận diện OPEN/RUNNING/FINISHED dựa trên startTime và endTime thực tế
                        newAuction.updateStatusBasedOnTime();

                        AuctionManager.getInstance().addAuction(newAuction);
                        return new Response("SUCCESS", "Thêm sản phẩm thành công!", item);
                    case "UPDATE_ITEM":
                        ItemDTO updateDto = (ItemDTO) request.getPayload();
                        Item updateItem = ItemFactory.createItem(
                                updateDto.type, updateDto.id, updateDto.name, updateDto.description,
                                updateDto.startingPrice,
                                LocalDateTime.parse(updateDto.startTime, DT_FMT),
                                LocalDateTime.parse(updateDto.endTime, DT_FMT),
                                updateDto.extraInfo
                        );
                        boolean isUpdated = itemDAO.updateItem(updateItem, updateDto.type, updateDto.extraInfo);
                        if (isUpdated) {
                            return new Response("SUCCESS", "Cập nhật sản phẩm thành công!", updateItem);
                        } else {
                            return new Response("ERROR", "Không tìm thấy sản phẩm cần cập nhật!", null);
                        }
                    case "REGISTER_AUTO_BID":
                        String autoPayload = (String) request.getPayload();
                        // Định dạng chuỗi nhận từ NetworkClient: auctionId;bidderName;maxBid;increment
                        String[] autoParts = autoPayload.split(";");
                        String autoAuctionId = autoParts[0];
                        String autoBidderName = autoParts[1];
                        double maxBid = Double.parseDouble(autoParts[2]);
                        double increment = Double.parseDouble(autoParts[3]);

                        // Kiểm tra xem phiên đấu giá đó có tồn tại không
                        Auction autoTarget = AuctionManager.getInstance()
                                .getAuctions().stream()
                                .filter(a -> a.getItem().getId().equals(autoAuctionId))
                                .findFirst()
                                .orElse(null);

                        if (autoTarget == null || autoTarget.isClosed()) {
                            return new Response("ERROR", "Phiên đấu giá không tồn tại hoặc đã kết thúc!", null);
                        }

                        // Lưu cấu hình vào danh sách hệ thống
                        autoBidList.add(new AutoBidConfig(autoAuctionId, autoBidderName, maxBid, increment));
                        System.out.println("Server [Auto-Bid]: " + autoBidderName + " đã đăng ký Auto-Bid cho SP " + autoAuctionId + " (Max: " + maxBid + ")");

                        // Kích hoạt chạy thử Auto-Bid ngay lập tức phòng trường hợp người đăng ký sau giá hiện tại
                        checkAndTriggerAutoBids(autoTarget);

                        return new Response("SUCCESS", "Đăng ký Auto-Bid thành công!", null);
                    case "STOP_AUCTION":
                        // 1. Nhận chuỗi ID từ Payload một cách an toàn
                        String stopAuctionId = String.valueOf(request.getPayload());

                        // 2. Tìm phiên đấu giá tương ứng trên RAM Server bằng .equals()
                        Auction auctionToStop = AuctionManager.getInstance()
                                .getAuctions().stream()
                                .filter(a -> a.getItem().getId().equals(stopAuctionId))
                                .findFirst()
                                .orElse(null);

                        if (auctionToStop == null) {
                            return new Response("ERROR", "Không tìm thấy phiên đấu giá cần dừng!", null);
                        }

                        // 3. GỌI HÀM CÓ SẴN: Đổi trạng thái sang FINISHED & kích hoạt Observer cập nhật giao diện công khai
                        if (auctionToStop.getItem() != null) {
                            auctionToStop.getItem().setEndTime(java.time.LocalDateTime.now().minusSeconds(1));
                        }

                        // Đồng thời set thuộc tính closed của bạn thành true
                        auctionToStop.finishAuction();
                        auctionToStop.closeAuction();

                        // 4. CẬP NHẬT DATABASE: Cập nhật giá cao nhất hiện tại hoặc trạng thái nếu cần
                        // itemDAO.updateCurrentPrice(stopAuctionId, auctionToStop.getItem().getCurrentHighestPrice());

                        System.out.println("Server [Action]: Admin đã ép dừng thủ công phiên đấu giá: " + stopAuctionId);

                        // Trả về thành công kèm đối tượng auction đã cập nhật trạng thái mới
                        return new Response("SUCCESS", "Đã dừng phiên đấu giá thành công!", auctionToStop);

                    case "DELETE_ITEM":
                        String id = (String) request.getPayload();
                        // 1. Xóa trong Database MySQL
                        boolean isDeleted = itemDAO.deleteItem(id);

                        if (isDeleted) {
                            // 2. PHẢI XÓA THÊM: Loại bỏ phiên đấu giá này khỏi bộ nhớ RAM của Server
                            AuctionManager.getInstance().getAuctions().removeIf(a -> a.getItem().getId().equals(id));
                            return new Response("SUCCESS", "Xóa sản phẩm thành công!", null);
                        } else {
                            return new Response("ERROR", "Không tìm thấy sản phẩm để xóa!", null);
                        }

                    case "GET_ALL_AUCTIONS":
                        List<Auction> auctions = AuctionManager.getInstance().getAuctions();
                        auctions.forEach(Auction::updateStatusBasedOnTime);
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
                        checkAndTriggerAutoBids(targetAuction);

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
    private static class AutoBidConfig {
        String auctionId;
        String bidderName;
        double maxBid;
        double increment;

        public AutoBidConfig(String auctionId, String bidderName, double maxBid, double increment) {
            this.auctionId = auctionId;
            this.bidderName = bidderName;
            this.maxBid = maxBid;
            this.increment = increment;
        }
    }
    private void checkAndTriggerAutoBids(Auction auction) {
        String auctionId = auction.getItem().getId();
        boolean bidPlaced;

        // Vòng lặp do-while đảm bảo nếu có nhiều người cùng cài Auto-Bid, họ sẽ tự động "đấu" với nhau cho tới giới hạn
        do {
            bidPlaced = false;
            double currentPrice = auction.getItem().getCurrentHighestPrice();
            String currentLeader = auction.getLeadingBidder() != null ? auction.getLeadingBidder().getUsername() : "";

            for (AutoBidConfig config : autoBidList) {
                // Điều kiện kích hoạt: Đúng sản phẩm, người này chưa phải dẫn đầu, và giá hiện tại vẫn dưới mức tối đa của họ
                if (config.auctionId.equals(auctionId) && !config.bidderName.equals(currentLeader) && currentPrice < config.maxBid) {

                    // Tính mức giá mới = Giá hiện tại + Bước giá đặt trước
                    double nextBid = currentPrice + config.increment;

                    // Nếu vượt quá maxBid thì lấy thẳng mức maxBid làm lượt cuối
                    if (nextBid > config.maxBid) {
                        nextBid = config.maxBid;
                    }

                    // Nếu giá mới tính toán vẫn hợp lệ (lớn hơn giá cao nhất hiện tại)
                    if (nextBid > currentPrice) {
                        try {
                            // 1. Đồng bộ lên Database đám mây
                            AuctionSystem.DAO.BidDAO bidDAO = new AuctionSystem.DAO.BidDAO();
                            bidDAO.addBid(auctionId, config.bidderName, nextBid);

                            // 2. Cập nhật lại trên RAM Server
                            auction.getItem().setCurrentHighestPrice(nextBid);
                            AuctionSystem.model.user.Bidder autoBidder = new AuctionSystem.model.user.Bidder(config.bidderName, "");
                            AuctionSystem.model.auction.Bid newAutoBid = new AuctionSystem.model.auction.Bid(autoBidder, nextBid);
                            auction.getBidHistory().add(newAutoBid);

                            // 3. Cập nhật giá sản phẩm vào DB
                            itemDAO.updateCurrentPrice(auctionId, nextBid);

                            System.out.println("Server [Auto-Bid Kích Hoạt]: Hệ thống tự đặt " + nextBid + " cho " + config.bidderName);

                            bidPlaced = true; // Đánh dấu là vừa có lượt đặt giá tự động thành công
                            break; // Thoát vòng lặp for để nạp lại trạng thái giá mới nhất cho lượt quét kế tiếp
                        } catch (Exception e) {
                            System.err.println("Lỗi thực hiện Auto-Bid: " + e.getMessage());
                        }
                    }
                }
            }
        } while (bidPlaced); // Tiếp tục nếu vẫn còn lượt nâng giá tự động hợp lệ
    }

    // Danh sách chứa tất cả cấu hình Auto-Bid đang chạy trên Server
    private static final List<AutoBidConfig> autoBidList = new java.util.concurrent.CopyOnWriteArrayList<>();

    public static void main(String[] args) {
        new AuctionServer().start();
    }
}