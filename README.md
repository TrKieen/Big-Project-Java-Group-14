# Big# Big-Project-Java-Group-14
**Bài tập lớn môn Lập trình nâng cao - Hệ thống Đấu giá trực tuyến**

## 1. Mô tả ngắn gọn bài toán và phạm vi hệ thống
Dự án là một **Hệ thống đấu giá trực tuyến (Auction System)** hoạt động theo kiến trúc mạng **Client-Server**. Hệ thống cho phép nhiều người dùng (Clients) kết nối đồng thời tới một máy chủ trung tâm (Server) để tham gia vào các phiên đấu giá.

Phạm vi hệ thống bao gồm:
* Quản lý phân quyền người dùng: Admin, Seller (Người bán), Bidder (Người mua).
* Quản lý danh mục sản phẩm đa dạng (Thiết bị điện tử, Tác phẩm nghệ thuật, Phương tiện di chuyển).
* Vận hành các phiên đấu giá thời gian thực với các tính năng: Đặt giá thủ công, tự động đặt giá (Auto-bid), trực quan hóa dữ liệu biểu đồ và ép dừng phiên đấu giá.

## 2. Công nghệ sử dụng, môi trường chạy và yêu cầu cài đặt
* **Ngôn ngữ lập trình:** Java 17.
* **Giao diện người dùng (GUI):** JavaFX 17.0.6.
* **Quản lý dự án & Build tool:** Maven.
* **Cơ sở dữ liệu:** MySQL (sử dụng thư viện `mysql-connector-j` bản 8.3.0, hỗ trợ đồng bộ đám mây Aiven).
* **Giao tiếp mạng:** Java Sockets (TCP/IP) truyền tải Object đa luồng.
* **Kiểm thử & Format:** JUnit 5, Checkstyle (chuẩn Google).
* **Yêu cầu cài đặt:** Cần cài đặt JDK 17 (trở lên), Maven và MySQL Server trên máy tính.

## 3. Cấu trúc thư mục, Kiến trúc phần mềm và Design Patterns
Dự án được tổ chức theo kiến trúc **Client-Server** và mô hình **MVC (Model-View-Controller)**, tuân thủ chặt chẽ các nguyên lý **OOP** và áp dụng các **Design Patterns** chuẩn:
* **`src/main/java/server/`**: Chứa mã nguồn của Server, xử lý đa luồng (Multi-threading) bằng `ExecutorService` (FixedThreadPool). Tự động đồng bộ với Cơ sở dữ liệu ngay khi khởi động.
    * Áp dụng **Singleton Pattern** (`AuctionManager`) nhằm quản lý tập trung và duy nhất các phiên đấu giá.
* **`src/main/java/AuctionSystem/`**: Chứa mã nguồn Client và các thành phần cốt lõi:
    * **`model/`**: Tổ chức OOP chặt chẽ (`Electronics`, `Art` kế thừa từ `Item`).
        * Áp dụng **Factory Pattern** (`ItemFactory`) để linh hoạt khởi tạo các loại sản phẩm.
        * Áp dụng **Observer Pattern** (`AuctionObserver`) để lắng nghe và cập nhật giao diện thời gian thực (Realtime Update) cho mọi Client trong phòng.
    * **`controller/`**: Điều khiển logic giao diện JavaFX của các bảng điều khiển phân quyền (Dashboard). Sử dụng `Timeline` và `ScheduledExecutorService` để tự động làm mới giao diện.
    * **`DAO/`**: Lớp Data Access Object (`ItemDAO`, `BidDAO`) bóc tách logic truy vấn CSDL.
    * **`network/`**: Định nghĩa gói tin giao tiếp qua mạng TCP/IP (`Request`, `Response`, `ItemDTO`).
    * **`exception/`**: Quản lý ngoại lệ tự định nghĩa (`InvalidBidException`, `AuctionClosedException`).

## 4. Vị trí các file .jar
Dự án sử dụng plugin `maven-shade-plugin` để đóng gói toàn bộ ứng dụng và các thư viện phụ thuộc vào một file Fat `.jar` duy nhất.
* Sau khi thực hiện lệnh build, file thực thi nằm tại: **`target/Big-Project-Java-Group-14-1.0-SNAPSHOT.jar`**.

## 5. Hướng dẫn chạy Server/Client theo thứ tự cụ thể
**Bắt buộc** phải chạy máy chủ Server trước khi chạy Client:
1. **Cơ sở dữ liệu:** Đảm bảo MySQL đang chạy và đã cấu hình đúng thông tin kết nối trong `JDBCUtil`.
2. **Build dự án:** Mở terminal tại thư mục gốc dự án, chạy lệnh `mvn clean package` để tạo file Fat JAR.
3. **Chạy Server:** Chạy class `server.AuctionServer`. Mặc định Server sẽ khởi động, đồng bộ dữ liệu và lắng nghe tại cổng `2514`.
4. **Chạy Client:** * Mở terminal và chạy lệnh: `java -jar target/Big-Project-Java-Group-14-1.0-SNAPSHOT.jar`
    * **Lưu ý:** Để giả lập hệ thống nhiều người dùng tương tác cùng lúc, hãy **mở nhiều cửa sổ terminal khác nhau** và chạy lại lệnh trên.

## 6. Danh sách chức năng đã hoàn thành
**Các chức năng cốt lõi (Bắt buộc):**
* Khởi tạo máy chủ đa luồng xử lý đồng thời kết nối nhiều Client mà không nghẽn mạng.
* Phân quyền và quản lý CRUD thông tin sản phẩm (Đồng bộ sâu 2 chiều với MySQL: Tự nạp khi bật Server và ghi nhận lập tức khi có thay đổi).
* Hệ thống đặt giá (Place Bid) thời gian thực: Tự động kiểm tra giá hợp lệ, theo dõi thời gian đếm ngược và tự chuyển trạng thái phiên (OPEN -> RUNNING -> FINISHED).
* **Xử lý Concurrent Bidding an toàn:** Sử dụng từ khóa `synchronized` kết hợp danh sách luồng an toàn `CopyOnWriteArrayList` để giải quyết triệt để Race Condition và Lost Update khi nhiều Bidder cùng trả giá trong 1 mili-giây.
* **Realtime Update (Observer/Socket):** Ngay lập tức thông báo giá dẫn đầu mới nhất cho toàn bộ các Client đang mở phiên.
* Xử lý lỗi toàn diện bằng cơ chế Custom Exceptions.

**Các chức năng nâng cao (Bonus):**
* **Auto-Bidding (Đấu giá tự động):** Người dùng thiết lập cấu hình giá trần (`maxBid`) và bước giá (`increment`), Server sẽ tự động chạy thuật toán cạnh tranh giá liên tục thay người dùng.
* **Bid History Visualization:** Tại màn hình Admin, cung cấp một biểu đồ dạng đường (LineChart) vẽ tự động biến động giá của 20 lượt trả giá gần nhất theo thời gian thực.
* Quyền can thiệp hệ thống: Admin có thể ép dừng phiên đấu giá thủ công bất kỳ lúc nào (`STOP_AUCTION`).

## 7. Link báo cáo PDF và video demo
https://drive.google.com/drive/folders/19vm5_PiYUxlqoq-RNZ5tcg4TgnTClyvc?usp=drive_link
