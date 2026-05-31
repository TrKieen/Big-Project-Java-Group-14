public class Launcher {
    public static void main(String[] args) {
        // Daemon thread tự tắt khi ứng dụng JavaFX đóng
        Thread serverThread = new Thread(() -> {
            try {
                new server.AuctionServer().start();
            } catch (Exception e) {
                // Server có thể đã chạy rồi (chạy từ máy khác / lần trước)
                System.out.println("[Launcher] Server đã chạy sẵn hoặc không thể khởi động: " + e.getMessage());
            }
        }, "auction-server-thread");
        serverThread.setDaemon(true); // tự tắt khi đóng app
        serverThread.start();

        // Đợi 800ms để Server kịp bind cổng trước khi Client kết nối
        try {
            Thread.sleep(800);
        } catch (InterruptedException ignored) {}

        HelloApplication.main(args);
    }
}