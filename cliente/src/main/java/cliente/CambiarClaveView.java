package cliente;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;

public class CambiarClaveView {
    @FXML
    private PasswordField campoClaveAntigua;

    @FXML
    private PasswordField campoClave;

    @FXML
    private PasswordField campoConfirmarClave;

    @FXML
    private Label textoError;

    @FXML
    public void botonCambio() {
        String a = campoClaveAntigua.getText();
        String c = campoClave.getText();
        String cc = campoConfirmarClave.getText();

        if (!c.equals(cc)) {
            textoError.setText("Las contraseñas no coinciden");
            textoError.setVisible(true);
            return;
        }

        try {
            ClienteImpl.get().cambiarClave(a, c);
            ClienteImpl.get().cerrarSesion();
        } catch (Exception e) {
            textoError.setText("Error cambiando clave: " + e.getMessage());
            textoError.setVisible(true);
            System.out.println(e);
            return;
        }

        Utils.changeScene("LoginView.fxml", textoError, this.getClass());
    }

    @FXML
    public void botonVolver() {
        Utils.changeScene("MessageView.fxml", textoError, this.getClass());
    }
}