//dùng để đóng gói và chuyển đổi dữ liệu giữa các thành phần ứng dụng, phổ biến nhất là giữa client và server
//giúp giảm số lần gọi hàm, tối ưu hóa băng thông, và bảo mật bằng cách chỉ chuyển thông tin cần thiết mà không bao gồm logic nghiệp vụ
package AuctionSystem.network;

import java.time.LocalDateTime;
import java.io.Serializable;

public class ItemDTO implements Serializable {
    public String id, name, description, type, extraInfo;
    public double startingPrice;
    public String startTime, endTime;

    public ItemDTO(String id, String name, String description, double startingPrice, LocalDateTime startTime, LocalDateTime endTime, String type, String extraInfo) {
        this.id = id; this.name = name; this.description = description;
        this.startingPrice = startingPrice;
        this.startTime = startTime.toString();
        this.endTime = endTime.toString();
        this.type = type; this.extraInfo = extraInfo;
    }
}
