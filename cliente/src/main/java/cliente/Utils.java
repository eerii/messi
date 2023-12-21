package cliente;

import java.io.IOException;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.stage.Stage;

public class Utils {
    public static void changeScene(String fxml, Label errorLabel, Class<?> c) {
        try {
            FXMLLoader loader = new FXMLLoader(c.getResource(fxml));
            Parent registerView = loader.load();

            Scene registerScene = new Scene(registerView);
            Stage currentStage = (Stage) errorLabel.getScene().getWindow();

            currentStage.setScene(registerScene);
        } catch (IOException e) {
            errorLabel.setText("Error cargando registro: " + e.getMessage());
            errorLabel.setVisible(true);
        }
    }
}
