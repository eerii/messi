package cliente;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cliente.ClienteImpl.MensajeDesencriptado;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import shared.EventoConexion;
import shared.IObserver;
import shared.Mensaje;

public class MessageView implements IObserver {
    String usuario;
    String chatActual;
    Map<String, List<MensajeDesencriptado>> mensajes = new HashMap<>();

    class ItemListaAmigues {
        String nombre;
        boolean notificacion;

        ItemListaAmigues(String nombre) {
            this.nombre = nombre;
            this.notificacion = false;
        }

        @Override
        public String toString() {
            return nombre + (notificacion ? " ●" : "");
        }

        @Override
        public boolean equals(Object o) {
            if (o == this)
                return true;
            if (!(o instanceof ItemListaAmigues))
                return false;
            ItemListaAmigues i = (ItemListaAmigues) o;
            return i.nombre.equals(this.nombre);
        }

        @Override
        public int hashCode() {
            return nombre.hashCode();
        }
    }

    @FXML
    private Label textoNombre;

    @FXML
    private ListView<ItemListaAmigues> listaAmigues;

    @FXML
    private VBox listaSolicitudes;

    @FXML
    private TextField campoEntradaSolicitud;

    @FXML
    private VBox listaChat;

    @FXML
    private TextField campoEntradaMensaje;

    @FXML
    private Label textoError;

    @FXML
    public void initialize() throws RemoteException {
        ClienteImpl c = ClienteImpl.get();
        c.addObservador(this);
        usuario = c.getUsuario();

        textoNombre.setText(c.getUsuario());

        for (String nombre : c.getAmiguesConectados().keySet()) {
            nuevoCliente(nombre);
        }

        listaAmigues.getSelectionModel().selectedItemProperty().addListener((obs, antigua, nueva) -> {
            if (nueva != null) {
                chatActual = nueva.nombre;

                listaChat.getChildren().clear();

                if (mensajes.containsKey(nueva.nombre)) {
                    mensajes.get(nueva.nombre).forEach(m -> dibujarMensaje(m));
                }
            }
        });
    }

    @FXML
    public void botonEnviar() {
        String m = campoEntradaMensaje.getText();

        if (m.isEmpty())
            return;
        if (chatActual == null)
            return;

        Mensaje msg = new Mensaje(m);

        try {
            ClienteImpl.get().enviar(chatActual, msg);
        } catch (RemoteException e) {
            textoError.setText("Error enviando mensaje: " + e.getMessage());
            textoError.setVisible(true);
        }

        campoEntradaMensaje.clear();
    }

    @FXML
    public void botonSalir() {
        try {
            ClienteImpl c = ClienteImpl.get();
            c.eliminarObservador(this);
            c.cerrarSesion();
            usuario = null;
            Utils.changeScene("LoginView.fxml", textoError, this.getClass());
        } catch (Exception e) {
            textoError.setText("Error cerrando sesión: " + e.getMessage());
            textoError.setVisible(true);
        }  
    }

    @FXML
    public void botonSolicitud() {
        String amigue = campoEntradaSolicitud.getText();
        campoEntradaSolicitud.clear();

        if (amigue.isEmpty())
            return;

        if (amigue.equals(usuario)) {
            textoError.setText("No puedes enviarte una solicitud a ti mismo");
            textoError.setVisible(true);
            return;
        }

        if (listaAmigues.getItems().contains(new ItemListaAmigues(amigue))) {
            textoError.setText("Ya eres amigue de " + amigue);
            textoError.setVisible(true);
            return;
        }

        try {
            ClienteImpl.get().enviarSolicitud(amigue);
        } catch (RemoteException e) {
            textoError.setText("Error enviando solicitud: " + e.getMessage());
            textoError.setVisible(true);
        }
    }

    @Override
    public void push(EventoConexion e, Object o) {
        switch (e) {
            case CLIENTE_CONECTADO:
                nuevoCliente((String) o);
                break;
            case CLIENTE_DESCONECTADO:
                eliminarCliente((String) o);
                break;
            case SOLICITUD_AMISTAD:
                dibujarSolicitud((String) o);
                break;
            case MENSAJE_ENVIADO:
                nuevoMensaje((MensajeDesencriptado) o);
                break;
            case MENSAJE_RECIBIDO:
                nuevoMensaje((MensajeDesencriptado) o);
                break;
            default:
        }
    }

    void nuevoCliente(String usuario) {
        Platform.runLater(() -> {
            ItemListaAmigues i = new ItemListaAmigues(usuario);
            if (!listaAmigues.getItems().contains(i))
                listaAmigues.getItems().add(i);
        });
        List<MensajeDesencriptado> list = new ArrayList<>();

        try {
            list = ClienteImpl.get().getMensajes(usuario);
        } catch (RemoteException e) {
        }

        if (!mensajes.containsKey(usuario)) {
            mensajes.put(usuario, list);
        }

        if (chatActual == null) {
            chatActual = usuario;
            Platform.runLater(() -> listaAmigues.getSelectionModel().select(new ItemListaAmigues(usuario)));
        }
    }

    void eliminarCliente(String usuario) {
        Platform.runLater(() -> listaAmigues.getItems().remove(new ItemListaAmigues(usuario)));
    }

    void nuevoMensaje(MensajeDesencriptado msg) {
        if (!mensajes.containsKey(msg.usuario)) {
            mensajes.put(msg.usuario, new ArrayList<>());
        }
        mensajes.get(msg.usuario).add(msg);

        if (chatActual.equals(msg.usuario)) {
            dibujarMensaje(msg);
        } else {
            ItemListaAmigues i = new ItemListaAmigues(msg.usuario);
            if (listaAmigues.getItems().contains(i)) {
                listaAmigues.getItems().get(listaAmigues.getItems().indexOf(i)).notificacion = true;
            }
        }
    }

    void dibujarMensaje(MensajeDesencriptado msg) {
        Circle avatar = new Circle();
        avatar.setRadius(24.0);
        avatar.setFill(msg.recibido ? Color.web("#218bff") : Color.web("#4ac26b"));

        Label textNombre = new Label(msg.usuario + " - " + msg.hora);
        Label textoMensaje = new Label(msg.mensaje);
        textoMensaje.setWrapText(true);

        VBox cajaContenido = new VBox();
        cajaContenido.setAlignment(msg.recibido ? Pos.CENTER_LEFT : Pos.CENTER_RIGHT);
        cajaContenido.getChildren().addAll(textNombre, textoMensaje);
        HBox.setHgrow(cajaContenido, Priority.ALWAYS);

        HBox cajaMensaje = new HBox();
        cajaMensaje.setSpacing(8.0);
        if (msg.recibido) {
            cajaMensaje.getChildren().addAll(avatar, cajaContenido);
        } else {
            cajaMensaje.getChildren().addAll(cajaContenido, avatar);
        }

        Platform.runLater(() -> listaChat.getChildren().add(cajaMensaje));
    }

    void dibujarSolicitud(String amigue) {
        Label textoNombre = new Label(amigue);

        HBox cajaNombre = new HBox();
        HBox.setHgrow(cajaNombre, Priority.ALWAYS);
        cajaNombre.getChildren().add(textoNombre);

        Button botonAceptar = new Button("V");
        botonAceptar.setOnAction(event -> {
            try {
                ClienteImpl.get().responderSolicitud(amigue, true);
                Platform.runLater(() -> listaSolicitudes.getChildren().remove(textoNombre.getParent()));

            } catch (RemoteException e) {
                e.printStackTrace();
            }
        });

        Button botonRechazar = new Button("X");
        botonRechazar.setOnAction(event -> {
            try {
                ClienteImpl.get().responderSolicitud(amigue, false);
                Platform.runLater(() -> listaSolicitudes.getChildren().remove(textoNombre.getParent()));
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        });

        HBox cajaSolicitud = new HBox();
        cajaSolicitud.setSpacing(8.0);
        cajaSolicitud.setAlignment(Pos.CENTER_LEFT);
        cajaSolicitud.getChildren().addAll(cajaNombre, botonAceptar, botonRechazar);

        Platform.runLater(() -> listaSolicitudes.getChildren().add(cajaSolicitud));
    }
}