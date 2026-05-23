# Bước 1: Sử dụng JDK để biên dịch code
FROM openjdk:17-jdk-slim AS build
WORKDIR /app

# Copy toàn bộ code vào trong container
COPY . .

# Biên dịch toàn bộ các file java (đảm bảo cấu trúc package của bạn chính xác)
# Lệnh này sẽ biên dịch file AuctionServer.java và các file phụ thuộc
RUN javac -d bin $(find . -name "*.java")

# Bước 2: Tạo môi trường chạy nhỏ gọn
FROM openjdk:17-jdk-slim
WORKDIR /app

# Copy các file class đã biên dịch từ bước build
COPY --from=build /app/bin /app/bin

# Cổng mà container sẽ mở ra nội bộ (Railway sẽ ánh xạ cổng này)
EXPOSE 2514

# Lệnh khởi chạy Server (thay 'server.AuctionServer' bằng đường dẫn package chuẩn của bạn nếu có thay đổi)
CMD ["java", "-cp", "bin", "server.AuctionServer"]