public class ItemFactory {
    public static Item createItem(String type, int id, String description, double startPrice) {
        switch (type.toLowerCase()) {
            case "electronics":
                return new Electronics(id, description, startPrice);
            case "art":
                return new Art(id, description, startPrice);
            case "vehicle":
                return new Vehicle(id, description, startPrice);
            default:
                throw new IllegalArgumentException("Loại Item không hợp lệ: " + type);
        }
    }
}
