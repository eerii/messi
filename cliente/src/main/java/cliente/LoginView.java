package cliente;

import java.rmi.RemoteException;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

public class LoginView {

    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Label errorLabel;

    public void handleLoginButtonAction() {
        String username = usernameField.getText();
        String password = passwordField.getText();        

        try {
            ClienteImpl cliente = ClienteImpl.get();
            cliente.iniciarSesion(username, password);
            Utils.changeScene("MessageView.fxml", errorLabel, this.getClass());
        } catch (RemoteException e) {
            errorLabel.setText("Error iniciando sesión: " + e.getMessage());
            errorLabel.setVisible(true);
        }
    }

    public void handleGoToRegisterButtonAction() {
        Utils.changeScene("RegisterView.fxml", errorLabel, this.getClass());
    }
}