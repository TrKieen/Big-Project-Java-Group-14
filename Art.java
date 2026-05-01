public class Art extends Item {
    public Art(int id, String description, double startPrice) {
        super(id, description, startPrice);
    }

    @Override
    public void displayCategory() {
        System.out.println("Danh mục: Art");
    }
}