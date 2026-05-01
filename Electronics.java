public class Electronics extends Item {
    public Electronics(int id, String description, double startPrice) {
        super(id, description, startPrice);
    }

    @Override
    public void displayCategory() {
        System.out.println("Danh mục: Electronics");
    }
}

