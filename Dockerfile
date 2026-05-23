# THAY THẾ BƯỚC 1: Đổi từ openjdk sang eclipse-temurin
FROM eclipse-temurin:17-jdk AS build
WORKDIR /app

# Copy toàn bộ code vào trong container
COPY . .

# Biên dịch toàn bộ các file java
RUN javac -d bin $(find . -name "*.java")

# THAY THẾ BƯỚC 2: Đổi môi trường chạy sang eclipse-temurin dạng jre cho nhẹ
FROM eclipse-temurin:17-jre
WORKDIR /app

# Copy các file class đã biên dịch từ bước build
COPY --from=build /app/bin /app/bin

# Cổng mở ra nội bộ
EXPOSE 2514

# Lệnh khởi chạy Server
CMD ["java", "-cp", "bin", "server.AuctionServer"]