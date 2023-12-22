package cliente;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

public class RegisterView {
    @FXML
    private TextField campoUsuario;

    @FXML
    private PasswordField campoClave;

    @FXML
    private PasswordField campoConfirmarClave;

    @FXML
    private Label textoError;

    public void botonRegistrar() {
        String u = campoUsuario.getText();
        String c = campoClave.getText();
        String cc = campoConfirmarClave.getText();

        if (!c.equals(cc)) {
            textoError.setText("Las contraseñas no coinciden");
            textoError.setVisible(true);
            return;
        }

        try {
            ClienteImpl.get().registrar(u, c);
            Utils.changeScene("LoginView.fxml", textoError, this.getClass());
        } catch (Exception e) {
            textoError.setText("Error registrando usuario: " + e.getMessage());
            textoError.setVisible(true);
        }
    }

    public void botonIrInicioSesion() {
        Utils.changeScene("LoginView.fxml", textoError, this.getClass());
    }
}