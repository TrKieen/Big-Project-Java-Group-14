package AuctionSystem.model;
import java.time.LocalDateTime;

public class ItemFactory {
    public static Item createItem(String type, String id, String name, String desc, double price, LocalDateTime start, LocalDateTime end, String extra) {
        if (type == null) {
            throw new IllegalArgumentException("Loại sản phẩm không được trống!");
        }
        String safeExtra = extra == null ? "" : extra.trim();
        return switch (type.toLowerCase()) {
            case "sản phẩm điện tử" -> {
                int warranty = 0;
                if (!safeExtra.isEmpty()) {
                    try {
                        warranty = Integer.parseInt(safeExtra);
                    } catch (NumberFormatException e) {
                        throw new IllegalArgumentException("Thời gian bảo hành phải là một số nguyên hợp lệ! (Giá trị bạn đã nhập: '" + safeExtra + "')");
                    }
                }
                yield new Electronics(id, name, desc, price, start, end, warranty);
            }
            case "tác phẩm nghệ thuật" -> new Art(id, name, desc, price, start, end, safeExtra);
            case "phương tiện di chuyển" -> new Vehicle(id, name, desc, price, start, end, safeExtra);
            default -> throw new IllegalArgumentException("Loại sản phẩm không hỗ trợ!");
        };
    }
}