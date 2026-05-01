// người bán

public class Seller extends User {
    private String storeName;

    public Seller(int id, String name, String email, String password, String storeName) {
        super(id, name, email, password);
        this.storeName = storeName;
    }

    public String getStoreName() {
        return storeName;
    }

    public void setStoreName(String storeName) {
        this.storeName = storeName;
    }

    public void createAuction(Item item) {
        System.out.println(getName() + " đã tạo phiên đấu giá cho sản phẩm: " + item.getDescription());
    }

    @Override
    public void displayRole() {
        System.out.println("Vai trò: Seller");
    }
}
