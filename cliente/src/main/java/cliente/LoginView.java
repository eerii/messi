package cliente;

import java.rmi.RemoteException;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

public class LoginView {

    @FXML
    private TextField campoUsuario;

    @FXML
    private PasswordField campoClave;

    @FXML
    private Label textoError;

    public void botonInicioSesion() {
        String u = campoUsuario.getText();
        String c = campoClave.getText();        

        try {
            ClienteImpl.get().iniciarSesion(u, c);
            Utils.changeScene("MessageView.fxml", textoError, this.getClass());
        } catch (RemoteException e) {
            textoError.setText("Error iniciando sesión: " + e.getMessage());
            textoError.setVisible(true);
        }
    }

    public void botonIrRegistrar() {
        Utils.changeScene("RegisterView.fxml", textoError, this.getClass());
    }
}