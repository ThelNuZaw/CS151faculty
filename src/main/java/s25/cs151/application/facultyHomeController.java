package s25.cs151.application;

import java.io.IOException;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.stage.Stage;

public class facultyHomeController {
    @FXML
    private Label welcomeText;

    @FXML
    protected void onOfficeHourButtonClick() {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("office-hours.fxml"));
            Scene scene = new Scene(fxmlLoader.load(), 700, 400);
            Stage stage = new Stage();
            stage.setTitle("Semester Office Hours");
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}