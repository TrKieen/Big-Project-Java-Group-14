//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//



import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

public class HelloController {
    @FXML
    private TextField txtUsername;
    @FXML
    private PasswordField txtPassword;
    @FXML
    private Label lblMessage;

    @FXML
    protected void onLoginButtonClick() {
        String user = this.txtUsername.getText();
        String pass = this.txtPassword.getText();
        if (user.equals("admin") && pass.equals("123")) {
            this.lblMessage.setText("Đăng nhập thành công!");
            this.lblMessage.setStyle("-fx-text-fill: green;");
        } else {
            this.lblMessage.setText("Sai tài khoản hoặc mật khẩu!");
            this.lblMessage.setStyle("-fx-text-fill: red;");
        }

    }
}
