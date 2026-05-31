# Big-Project-Java-Group-14

## 1. Mô tả ngắn gọn bài toán và phạm vi hệ thống
Dự án là một **Hệ thống đấu giá trực tuyến (Auction System)** hoạt động theo mô hình mạng **Client-Server**. Hệ thống cho phép nhiều người dùng (Clients) kết nối đồng thời tới một máy chủ trung tâm (Server) để tham gia vào các phiên đấu giá.

Phạm vi hệ thống bao gồm:
* Quản lý phân quyền người dùng: Admin, Seller (Người bán), Bidder (Người mua).
* Quản lý danh mục sản phẩm đa dạng (Thiết bị điện tử, Tác phẩm nghệ thuật, Phương tiện di chuyển).
* Vận hành các phiên đấu giá thời gian thực với các tính năng: Đặt giá thủ công, tự động đặt giá (Auto-bid), và ép dừng phiên đấu giá.

## 2. Công nghệ sử dụng, môi trường chạy và yêu cầu cài đặt
* **Ngôn ngữ lập trình:** Java 17.
* **Giao diện người dùng (GUI):** JavaFX 17.0.6.
* **Quản lý dự án & Build tool:** Maven.
* **Cơ sở dữ liệu:** MySQL (sử dụng thư viện `mysql-connector-j` bản 8.3.0).
* **Giao tiếp mạng:** Java Sockets (TCP/IP) với đa luồng (Multi-threading).
* **Kiểm thử & Format:** JUnit 5, Checkstyle.
* **Yêu cầu cài đặt:** Cần cài đặt JDK 17 (trở lên), Maven và MySQL Server trên máy.

## 3. Cấu trúc thư mục hoặc các module chính
Cấu trúc mã nguồn được tổ chức theo mô hình MVC và phân tầng rõ ràng:
* **`src/main/java/server/`**: Chứa mã nguồn của Server, nơi lắng nghe các kết nối Socket và xử lý logic nghiệp vụ trung tâm.
* **`src/main/java/AuctionSystem/`**: Chứa mã nguồn của Client và các thành phần chia sẻ:
    * **`model/`**: Các lớp thực thể (Item, User, Auction, Bid,...) và các mẫu thiết kế (Design pattern) như Factory.
    * **`controller/`**: Điều khiển logic giao diện JavaFX của các Role khác nhau.
    * **`DAO/`**: Lớp thao tác cơ sở dữ liệu giao tiếp với MySQL.
    * **`network/`**: Chứa các lớp định nghĩa gói tin giao tiếp giữa Client và Server.
    * **`exception/`**: Các lớp ngoại lệ tùy chỉnh xử lý lỗi.
* **`src/main/resources/`**: Chứa file cấu hình giao diện (.fxml) và file định dạng kiểu (.css).
* **`src/main/java/Launcher.java`**: Điểm neo khởi động chính của ứng dụng Client.

## 4. Vị trí các file .jar
Dự án sử dụng `maven-shade-plugin` để đóng gói toàn bộ ứng dụng và thư viện vào một file `.jar` duy nhất.
* Sau khi thực hiện lệnh build bằng Maven, file `.jar` sẽ được sinh ra và nằm tại thư mục: **`target/Big-Project-Java-Group-14-1.0-SNAPSHOT.jar`**.

## 5. Hướng dẫn chạy Server/Client theo thứ tự cụ thể
Để hệ thống hoạt động đúng, bạn **bắt buộc** phải chạy Server trước khi chạy Client theo các bước sau:
1. **Cấu hình Cơ sở dữ liệu:** Đảm bảo MySQL đang chạy.
2. **Build dự án:** Chạy lệnh `mvn clean package` trong terminal tại thư mục gốc của dự án để tải thư viện và tạo file `.jar`.
3. **Chạy Server:** Chạy class `server.AuctionServer`. Mặc định Server sẽ lắng nghe tại cổng `2514`.
4. **Chạy Client:**
    * Mở terminal và chạy lệnh: `java -jar target/Big-Project-Java-Group-14-1.0-SNAPSHOT.jar`
    * **Lưu ý:** Để chạy nhiều client, bạn hãy mở nhiều cửa sổ terminal khác nhau và chạy lại lệnh trên.

## 6. Danh sách chức năng đã hoàn thành
* Khởi tạo máy chủ đa luồng xử lý đồng thời nhiều kết nối Client (tối đa 10 luồng cùng lúc).
* Quản lý CRUD thông tin sản phẩm (Thêm, cập nhật, xóa) và đồng bộ với MySQL.
* Phân loại đa dạng mặt hàng đấu giá bằng Factory Pattern.
* Hệ thống đặt giá (Place Bid) thời gian thực, lưu lại lịch sử đấu giá.
* Thuật toán đấu giá tự động (Auto-Bid) tự động trả giá thay người dùng.
* Phân quyền và tính năng ép dừng phiên đấu giá thủ công của Admin.

## 7. Link báo cáo PDF và video demo
* **Báo cáo PDF:** [Chèn link tại đây]
* **Video Demo:** [Chèn link tại đây]

## 1. Mô tả ngắn gọn bài toán và phạm vi hệ thống
Dự án là một **Hệ thống đấu giá trực tuyến (Auction System)** hoạt động theo mô hình mạng **Client-Server**. Hệ thống cho phép nhiều người dùng (Clients) kết nối đồng thời tới một máy chủ trung tâm (Server) để tham gia vào các phiên đấu giá.

Phạm vi hệ thống bao gồm:
* Quản lý phân quyền người dùng: Admin, Seller (Người bán), Bidder (Người mua).
* Quản lý danh mục sản phẩm đa dạng (Thiết bị điện tử, Tác phẩm nghệ thuật, Phương tiện di chuyển).
* Vận hành các phiên đấu giá thời gian thực với các tính năng: Đặt giá thủ công, tự động đặt giá (Auto-bid), và ép dừng phiên đấu giá.

## 2. Công nghệ sử dụng, môi trường chạy và yêu cầu cài đặt
* **Ngôn ngữ lập trình:** Java 17.
* **Giao diện người dùng (GUI):** JavaFX 17.0.6.
* **Quản lý dự án & Build tool:** Maven.
* **Cơ sở dữ liệu:** MySQL (sử dụng thư viện `mysql-connector-j` bản 8.3.0).
* **Giao tiếp mạng:** Java Sockets (TCP/IP) với đa luồng (Multi-threading).
* **Kiểm thử & Format:** JUnit 5, Checkstyle.
* **Yêu cầu cài đặt:** Cần cài đặt JDK 17 (trở lên), Maven và MySQL Server trên máy.

## 3. Cấu trúc thư mục hoặc các module chính
Cấu trúc mã nguồn được tổ chức theo mô hình MVC và phân tầng rõ ràng:
* **`src/main/java/server/`**: Chứa mã nguồn của Server, nơi lắng nghe các kết nối Socket và xử lý logic nghiệp vụ trung tâm.
* **`src/main/java/AuctionSystem/`**: Chứa mã nguồn của Client và các thành phần chia sẻ:
    * **`model/`**: Các lớp thực thể (Item, User, Auction, Bid,...) và các mẫu thiết kế (Design pattern) như Factory.
    * **`controller/`**: Điều khiển logic giao diện JavaFX của các Role khác nhau.
    * **`DAO/`**: Lớp thao tác cơ sở dữ liệu giao tiếp với MySQL.
    * **`network/`**: Chứa các lớp định nghĩa gói tin giao tiếp giữa Client và Server.
    * **`exception/`**: Các lớp ngoại lệ tùy chỉnh xử lý lỗi.
* **`src/main/resources/`**: Chứa file cấu hình giao diện (.fxml) và file định dạng kiểu (.css).
* **`src/main/java/Launcher.java`**: Điểm neo khởi động chính của ứng dụng Client.

## 4. Vị trí các file .jar
Dự án sử dụng `maven-shade-plugin` để đóng gói toàn bộ ứng dụng và thư viện vào một file `.jar` duy nhất.
* Sau khi thực hiện lệnh build bằng Maven, file `.jar` sẽ được sinh ra và nằm tại thư mục: **`target/Big-Project-Java-Group-14-1.0-SNAPSHOT.jar`**.

## 5. Hướng dẫn chạy Server/Client theo thứ tự cụ thể
Để hệ thống hoạt động đúng, bạn **bắt buộc** phải chạy Server trước khi chạy Client theo các bước sau:
1. **Cấu hình Cơ sở dữ liệu:** Đảm bảo MySQL đang chạy.
2. **Build dự án:** Chạy lệnh `mvn clean package` trong terminal tại thư mục gốc của dự án để tải thư viện và tạo file `.jar`.
3. **Chạy Server:** Chạy class `server.AuctionServer`. Mặc định Server sẽ lắng nghe tại cổng `2514`.
4. **Chạy Client:**
    * Mở terminal và chạy lệnh: `java -jar target/Big-Project-Java-Group-14-1.0-SNAPSHOT.jar`
    * **Lưu ý:** Để chạy nhiều client, bạn hãy mở nhiều cửa sổ terminal khác nhau và chạy lại lệnh trên.

## 6. Danh sách chức năng đã hoàn thành
* Khởi tạo máy chủ đa luồng xử lý đồng thời nhiều kết nối Client (tối đa 10 luồng cùng lúc).
* Quản lý CRUD thông tin sản phẩm (Thêm, cập nhật, xóa) và đồng bộ với MySQL.
* Phân loại đa dạng mặt hàng đấu giá bằng Factory Pattern.
* Hệ thống đặt giá (Place Bid) thời gian thực, lưu lại lịch sử đấu giá.
* Thuật toán đấu giá tự động (Auto-Bid) tự động trả giá thay người dùng.
* Phân quyền và tính năng ép dừng phiên đấu giá thủ công của Admin.

## 7. Link báo cáo PDF và video demo
https://drive.google.com/drive/folders/19vm5_PiYUxlqoq-RNZ5tcg4TgnTClyvc?usp=drive_link
