package cliente;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

public class RegisterView {
    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private PasswordField confirmPasswordField;

    @FXML
    private Label errorLabel;

    public void handleRegisterButtonAction() {
        String username = usernameField.getText();
        String password = passwordField.getText();
        String confirmPassword = confirmPasswordField.getText();

        if (!password.equals(confirmPassword)) {
            errorLabel.setText("Las contraseñas no coinciden");
            errorLabel.setVisible(true);
            return;
        }

        try {
            ClienteImpl cliente = ClienteImpl.get();
            cliente.registrarse(username, password);
            Utils.changeScene("LoginView.fxml", errorLabel, this.getClass());
        } catch (Exception e) {
            errorLabel.setText("Error registrando usuario: " + e.getMessage());
            errorLabel.setVisible(true);
        }
    }

    public void handleGoToLoginButtonAction() {
        Utils.changeScene("LoginView.fxml", errorLabel, this.getClass());
    }
}