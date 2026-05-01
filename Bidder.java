// người đấu thầu

public class Bidder extends User {
    private double balance;

    public Bidder(int id, String name, String email, String password, double balance) {
        super(id, name, email, password);
        this.balance = balance;
    }

    public double getBalance() {
        return balance;
    }

    public void setBalance(double balance) {
        this.balance = balance;
    }

    public void placeBid(Item item, double amount) {
        System.out.println(getName() + " đặt giá " + amount + " cho sản phẩm " + item.getDescription());
    }

    @Override
    public void displayRole() {
        System.out.println("Vai trò: Bidder");
    }
}
