public class Admin extends User {

    public Admin(int id, String name, String email, String password) {
        super(id, name, email, password);
    }

    public void manageUsers() {
        System.out.println("Admin đang quản lý người dùng...");
    }

    public void manageAuctions() {
        System.out.println("Admin đang quản lý các phiên đấu giá...");
    }

    @Override
    public void displayRole() {
        System.out.println("Vai trò: Admin");
    }
}
