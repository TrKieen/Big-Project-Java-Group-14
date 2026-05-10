import java.io.IOException;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class HelloApplication extends Application {
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(HelloApplication.class.getResource("/hello-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 800.0F, 600.0F);
        stage.setTitle("eBay Auction System - Desktop Version");
        stage.setScene(scene);
        stage.setResizable(true);
        stage.show();
    }

    static void main(String[] args) {
        launch();
    }
}
