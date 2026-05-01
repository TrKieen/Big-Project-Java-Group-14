public class Vehicle extends Item {
    public Vehicle(int id, String description, double startPrice) {
        super(id, description, startPrice);
    }

    @Override
    public void displayCategory() {
        System.out.println("Danh mục: Vehicle");
    }
}
