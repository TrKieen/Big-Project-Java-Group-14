public abstract class User {
    private int id;
    private String name;
    private String email;
    private String password;

    // Constructor
    public User(int id, String name, String email, String password) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.password = password;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    // Không nên có setter cho password nếu muốn bảo mật hơn
    public boolean checkPassword(String inputPassword) {
        return this.password.equals(inputPassword);
    }

    public void login() {
        System.out.println(name + " Đã đăng nhập.");
    }

    public void logout() {
        System.out.println(name + " Đã đăng xuất.");
    }

    public abstract void displayRole(); // vai trò
}
