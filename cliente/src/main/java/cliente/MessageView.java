package cliente;

import java.rmi.RemoteException;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import shared.EventoConexion;
import shared.IObserver;
import shared.Mensaje;

public class MessageView implements IObserver {
    String usuarioActual;

    @FXML
    private Label textoNombre;

    @FXML
    private ListView<String> listaAmigues;

    @FXML
    private TextField campoEntradaMensaje;

    @FXML
    private Label textoError;

    @FXML
    public void botonEnviar() {
        String m = campoEntradaMensaje.getText();

        if (m.isEmpty())
            return;
        if (usuarioActual == null)
            return;

        Mensaje msg = new Mensaje(m);

        try {
            ClienteImpl.get().enviar(usuarioActual, msg);
        } catch (RemoteException e) {
            textoError.setText("Error enviando mensaje: " + e.getMessage());
            textoError.setVisible(true);
        }

        campoEntradaMensaje.clear();
    }

    @FXML
    public void botonSalir() {
        try {
            ClienteImpl.get().cerrarSesion();
            Utils.changeScene("LoginView.fxml", textoError, this.getClass());
        } catch (Exception e) {
            textoError.setText("Error cerrando sesión: " + e.getMessage());
            textoError.setVisible(true);
        }  
    }

    @FXML
    public void initialize() throws RemoteException {
        ClienteImpl c = ClienteImpl.get();
        c.addObservador(this);

        textoNombre.setText(c.getUsuario());

        for (String nombre : c.getAmiguesConectados().keySet()) {
            listaAmigues.getItems().add(nombre);
        }

        listaAmigues.getSelectionModel().selectedItemProperty().addListener((obs, antigua, nueva) -> {
            if (nueva != null) {
                usuarioActual = nueva;
                // TODO: mostrar mensajes
            }
        });
    }

    @Override
    public void push(EventoConexion e, Object o) {
        switch (e) {
            case CLIENTE_CONECTADO:
                listaAmigues.getItems().add((String) o);
                break;
            case CLIENTE_DESCONECTADO:
                listaAmigues.getItems().remove((String) o);
                break;
            case SOLICITUD_AMISTAD:
                // TODO:
                break;
            case MENSAJE_ENVIADO:
                // TODO:
                break;
            case MENSAJE_RECIBIDO:
                // TODO:
                break;
            default:
        }
    }
}