public abstract class Item {
    private int id;
    private String description; // mô tả
    private double startPrice; // giá khởi điểm

    public Item(int id, String description, double startPrice) {
        this.id = id;
        this.description = description;
        this.startPrice = startPrice;
    }

    public int getId() { 
        return id; 
    }
    public String getDescription() { 
        return description; 
    }
    public double getStartPrice() { 
        return startPrice; 
    }

    public abstract void displayCategory();
}
