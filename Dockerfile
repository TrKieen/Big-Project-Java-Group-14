# Bước 1: Sử dụng Maven để build dự án thành file .jar
FROM maven:3.8.5-openjdk-17 AS build
WORKDIR /app
COPY . .
RUN mvn clean package -DskipTests

# Bước 2: Dùng JDK nhẹ để chạy file .jar sau khi build
FROM openjdk:17-jdk-slim
WORKDIR /app
# Copy file .jar từ bước build ở trên sang (Đổi tên 'AuctionSystem-1.0-SNAPSHOT.jar' nếu dự án của bạn tên khác)
COPY --from=build /app/target/AuctionSystem-1.0-SNAPSHOT.jar app.jar

# Biến môi trường mặc định nếu chạy local
ENV PORT=2514
EXPOSE $PORT

# Lệnh để khởi chạy Server
CMD ["java", "-jar", "app.jar"]