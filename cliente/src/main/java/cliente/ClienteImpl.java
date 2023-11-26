package cliente;

import cliente.views.MainView;
import shared.*;
import shared.Utils.Color;

import static shared.Utils.*;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

import javax.crypto.KeyAgreement;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;

import com.vaadin.flow.component.UI;

public class ClienteImpl extends UnicastRemoteObject implements ICliente {
    int puerto;
    int puerto_servidor;
    String ip_servidor;
    IServidor servidor;

    String user;
    Map<String, Amigo> amigos;

    KeyPair keys;

    UI ui;
    MainView view;

    class Amigo {
        List<Mensaje> mensajes;
        ICliente conexion;
        SecretKey secreto;

        Amigo() {
            this.mensajes = new ArrayList<>();
        }

        void conectar(ICliente c) {
            this.conexion = c;

            if (secreto == null)
                generarSecreto();
        }

        void desconectar() {
            this.conexion = null;
        }

        boolean estaConectado() {
            return this.conexion != null;
        }

        void generarSecreto() {
            // Obtenemos la llave pública del amigo
            byte[] pubkey = null;
            try {
                pubkey = conexion.pubkey();
            } catch (RemoteException e) {
                log("error al obtener la llave pública de " + user);
                return;
            }

            // Generamos el secreto
            try {
                // Usamos el algoritmo Diffie-Hellman para generar el secreto
                KeyFactory factory = KeyFactory.getInstance("DH");
                X509EncodedKeySpec spec = new X509EncodedKeySpec(pubkey);
                PublicKey pub = factory.generatePublic(spec);

                KeyAgreement agree = KeyAgreement.getInstance("DH");
                agree.init(keys.getPrivate());
                agree.doPhase(pub, true);

                byte[] sec = agree.generateSecret();
                sec = Arrays.copyOf(sec, 16); // Usamos solo los primeros 128 bits (AES)
                debug("secreto generado con " + user + ": " + emojiFromHex(bytesToHex(sec)));
                
                this.secreto = new SecretKeySpec(sec, "AES");
            } catch (NoSuchAlgorithmException | InvalidKeySpecException | InvalidKeyException e) {
                e.printStackTrace();
            }
        }
    }

    public ClienteImpl(int puerto_c, int puerto_s, String ip_s) throws RemoteException {
        super(puerto_c);
        this.amigos = new HashMap<>();

        this.puerto = puerto_c;
        this.puerto_servidor = puerto_s;
        this.ip_servidor = ip_s;

        // Generar llaves de encriptación
        try {
            KeyPairGenerator gen = KeyPairGenerator.getInstance("DH");
            gen.initialize(2048);
            this.keys = gen.generateKeyPair();

            debug("llaves de encriptación generadas:");
            debug("  llave pública: " + printKey(keys.getPublic().getEncoded()));
            debug("  llave privada: " + printKey(keys.getPrivate().getEncoded()));
        } catch (Exception e) {
            throw new RemoteException("error al generar las llaves de encriptación");
        }
    }

    // Getters y setters

    public void setUI(UI ui, MainView view) {
        this.ui = ui;
        this.view = view;

        ui.access(() -> {
            Map<String, Amigo> conectados = getAmigosConectados();
            view.actualizarClientes(conectados.keySet());
            for (String user : conectados.keySet()) {
                view.actualizarMensajes(user, conectados.get(user).mensajes);
            }
        });
    }

    // Funciones de interfaz

    @Override
    @SuppressWarnings("unchecked")
    public void notificar(EventoConexion e, Object o) throws RemoteException {
        switch (e) {
            case CLIENTE_CONECTADO: { // String
                String user = (String) o;
                ICliente c = servidor.buscar(user);

                // Añadimos el usuario a clientes y mensajes si no estaba
                if (!amigos.containsKey(user))
                    amigos.put(user, new Amigo());

                // Conectamos al usuario
                if (amigos.get(user).estaConectado())
                    break;
                amigos.get(user).conectar(c);

                // Actualizamos la vista
                if (ui != null) {
                    ui.access(() -> {
                        view.actualizarClientes(getAmigosConectados().keySet());
                    });
                }

                log(user + " se ha conectado");
                break;
            }
            case CLIENTE_DESCONECTADO: { // String
                String user = (String) o;
                if (amigos.containsKey(user))
                    amigos.get(user).desconectar();

                // Actualizamos la vista
                if (ui != null) {
                    ui.access(() -> {
                        view.actualizarClientes(getAmigosConectados().keySet());
                    });
                }

                log(user + " se ha desconectado");
                break;
            }
            case LISTA_CLIENTES: { // List<String>
                // Nueva lista de clientes (filtramos para que no se incluya a si mismo)
                List<String> clientes = ((List<String>) o).stream()
                        .filter(u -> !u.equals(this.user))
                        .collect(Collectors.toList());

                // Añadimos a amigos
                for (String user : clientes) {
                    if (!amigos.containsKey(user))
                        amigos.put(user, new Amigo());
                    ICliente c = servidor.buscar(user);
                    amigos.get(user).conectar(c);
                }

                // Actualizamos la vista
                if (ui != null) {
                    ui.access(() -> {
                        view.actualizarClientes(getAmigosConectados().keySet());
                    });
                }

                // Mostramos los clientes conectados
                if (clientes.size() == 0)
                    log("no hay clientes conectados");
                else
                    log("clientes conectados: " + clientes.stream().collect(Collectors.joining(", ")));

                break;
            }
            case SOLICITUD_AMISTAD: { // String
                // FIX: Aceptar solicitud de amistad automáticamente
                servidor.responderSolicitud(this.user, (String) o, true);
            }
            case PING: { // String
                debug("ping de " + o);
                break;
            }
            default:
                break;
        }
    }

    @Override
    public void enviar(String user, Mensaje msg) throws RemoteException {
        try {
            msg.setUsuario(this.user);
            if (amigos.get(user).secreto != null)
                msg.encriptar(amigos.get(user).secreto);
            else
                debug("mensaje enviado sin encriptar a " + user + "!", Color.ROJO);

            Amigo amigo = amigos.get(user);
            if (amigo == null)
                throw new RemoteException("el usuario " + user + " no existe");
            if (!amigo.estaConectado())
                throw new RemoteException("el usuario " + user + " no está conectado");

            amigo.mensajes.add(msg);
            amigo.conexion.recibir(this.user, msg);

            // Actualizamos la vista
            if (ui != null) {
                ui.access(() -> {
                    view.actualizarMensajes(user, amigo.mensajes);
                });
            }
        } catch (RemoteException e) {
            servidor.ping(user);
        }
    }

    @Override
    public void recibir(String user, Mensaje msg) throws RemoteException {
        if (!msg.encriptado()) {
            debug("mensaje sin encriptar recibido de " + user + "!", Color.ROJO);
        } else {
            if (amigos.get(user).secreto == null)
                throw new RemoteException("el usuario " + user + " no tiene un secreto compartido");
            msg.desencriptar(amigos.get(user).secreto);
        }

        Amigo amigo = amigos.get(user);
        if (amigo == null)
            throw new RemoteException("el usuario " + user + " no existe");

        amigo.mensajes.add(msg);

        // Actualizamos la vista
        if (ui != null) {
            ui.access(() -> {
                view.actualizarMensajes(user, amigo.mensajes);
            });
        }

        log("mensaje recibido de " + user + ": " + msg);
    }

    @Override
    public byte[] pubkey() throws RemoteException {
        return keys.getPublic().getEncoded();
    }

    // Funciones propias

    public void iniciarSesion(String user) throws RemoteException {
        this.user = user;

        // Nos conectamos al servidor y pasamos la interfaz
        Registry registro = LocateRegistry.getRegistry(ip_servidor, puerto_servidor);
        try {
            servidor = (IServidor) registro.lookup("Servidor");
        } catch (NotBoundException e) {
            throw new RemoteException("error al buscar el servidor en el registro");
        }
        servidor.conectar((ICliente) this, user);
        log("cliente conectado al servidor " + ip_servidor + ":" + puerto_servidor);
    }

    public void cerrarSesion() throws RemoteException {
        servidor.salir(user);
        servidor = null;
        log("cliente desconectado del servidor");
    }

    public boolean estaConectado() {
        return servidor != null;
    }

    public Map<String, Amigo> getAmigosConectados() {
        return amigos.entrySet().stream()
                .filter(e -> e.getValue().conexion != null)
                .collect(Collectors.toMap(e -> e.getKey(), e -> e.getValue()));
    }
}
