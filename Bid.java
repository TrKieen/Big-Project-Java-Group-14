import java.util.Date;

public class Bid {
    private int id;
    private double amount;
    private Date timestamp;
    private Bidder bidder;   // Người đặt giá
    private Item item;       // Sản phẩm được đấu giá

    // Constructor
    public Bid(int id, double amount, Date timestamp, Bidder bidder, Item item) {
        this.id = id;
        this.amount = amount;
        this.timestamp = timestamp;
        this.bidder = bidder;
        this.item = item;
    }

    public int getId() {
        return id;
    }

    public double getAmount() {
        return amount;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public Bidder getBidder() {
        return bidder;
    }

    public Item getItem() {
        return item;
    }

    // Hiển thị thông tin bid
    public void displayBidInfo() {
        System.out.println("Bid #" + id + ": " + bidder.getName() +
                " đặt giá " + amount + " cho sản phẩm " + item.getDescription() +
                " vào lúc " + timestamp);
    }
}
