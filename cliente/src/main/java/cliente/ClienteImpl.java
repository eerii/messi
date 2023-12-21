package cliente;

import shared.*;
import shared.Utils.Color;

import static shared.Utils.*;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
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

public class ClienteImpl extends UnicastRemoteObject implements ICliente {
    static ClienteImpl instancia;
    static int puerto;
    static int puerto_servidor;
    static String ip_servidor;

    IServidor servidor;

    String user;
    Map<String, Amigo> amigos;

    KeyPair keys;

    class Amigo {
        String user;
        List<Mensaje> mensajes;
        ICliente conexion;
        SecretKey secreto;

        Amigo(String user) {
            this.mensajes = new ArrayList<>();
            this.user = user;
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
                debug("secreto generado con " + this.user + ": " + emojiFromHex(bytesToHex(sec)));

                this.secreto = new SecretKeySpec(sec, "AES");
            } catch (NoSuchAlgorithmException | InvalidKeySpecException | InvalidKeyException e) {
                e.printStackTrace();
            }
        }
    }

    ClienteImpl() throws RemoteException {
        super(puerto);
        this.amigos = new HashMap<>();

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

    public static void set(int puerto_c, int puerto_s, String ip_s) {
        puerto = puerto_c;
        puerto_servidor = puerto_s;
        ip_servidor = ip_s;
        log("cliente iniciado en el puerto " + puerto);
        log("servidor: " + ip_servidor + ":" + puerto_servidor);
    }

    public static ClienteImpl get() throws RemoteException {
        if (instancia == null)
            instancia = new ClienteImpl();
        return instancia;
    }

    // Funciones de interfaz

    @Override
    public String getUsername() throws RemoteException{
        return user;
    }

    @Override
    @SuppressWarnings("unchecked")
    public void notificar(EventoConexion e, Object o) throws RemoteException {
        switch (e) {
            case CLIENTE_CONECTADO: { // ICliente
                ICliente c = (ICliente) o;
                String user = c.getUsername();

                // Añadimos el usuario a clientes y mensajes si no estaba
                if (!amigos.containsKey(user))
                    amigos.put(user, new Amigo(user));

                // Conectamos al usuario
                if (amigos.get(user).estaConectado())
                    break;
                amigos.get(user).conectar(c);

                log(user + " se ha conectado");
                break;
            }
            case CLIENTE_DESCONECTADO: { // String
                String user = (String) o;
                if (amigos.containsKey(user))
                    amigos.get(user).desconectar();

                log(user + " se ha desconectado");
                break;
            }
            case LISTA_CLIENTES: { // Set<ICliente>
                // Nueva lista de clientes (filtramos para que no se incluya a si mismo)
                Set<ICliente> clientes = (Set<ICliente>) o;
                List<String>  usernames = new ArrayList<>();
                clientes.remove(this);

                // Añadimos a amigos
                for (ICliente c : clientes) {
                    String user = c.getUsername();
                    usernames.add(user);
                    if(!amigos.containsKey(user)){
                        amigos.put(user, new Amigo(user));
                        amigos.get(user).conectar(c);
                    }
                }

                // Mostramos los clientes conectados
                if (clientes.size() == 0)
                    log("no hay clientes conectados");
                else
                    log("clientes conectados: " + usernames.stream().collect(Collectors.joining(", ")));
                break;
            }
            case SOLICITUD_AMISTAD: { // String
                // TODO: Aceptar solicitud de amistad automáticamente
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
    public void recibir(String user, Mensaje msg) throws RemoteException {
        if (!msg.encriptado()) {
            debug("mensaje sin encriptar recibido de " + user + "!", Color.ROJO);
        }

        Amigo amigo = amigos.get(user);
        if (amigo == null)
            throw new RemoteException("el usuario " + user + " no existe");

        amigo.mensajes.add(msg);

        String msg_str = " " + msg;
        if (msg.encriptado())
            try {
                msg_str = " " + msg.desencriptar(amigo.secreto);
            } catch (InvalidKeyException | IllegalBlockSizeException | BadPaddingException e) {
                msg_str = "Error al desencriptar";
            }
        log("mensaje recibido de " + user + ": " + msg_str);
    }

    @Override
    public byte[] pubkey() throws RemoteException {
        return keys.getPublic().getEncoded();
    }

    // Funciones propias

    private boolean conectarServidor() throws RemoteException{
        Registry registro = LocateRegistry.getRegistry(ip_servidor, puerto_servidor);
        try {
            servidor = (IServidor) registro.lookup("Servidor");
            return true;
        } catch (NotBoundException e) {
            throw new RemoteException("error al buscar el servidor en el registro");
        }
    }

    public void registrarse(String user, String pass) throws RemoteException{
        if (this.user == null)
            this.user = user;

        if(!estaConectado())
            conectarServidor();
        
        servidor.registrar(user, pass);
        log("cliente registrado en el servidor " + ip_servidor + ":" + puerto_servidor);
    }

    public void desubscribirse(String password) throws RemoteException{
        servidor.eliminar(user, password);
    }

    public void iniciarSesion(String user, String pass) throws RemoteException {
        if (this.user == null)
            this.user = user;

        // Nos conectamos al servidor y pasamos la interfaz
        if(!estaConectado())
            conectarServidor();
        
        servidor.conectar((ICliente) this, user, pass);
        log("cliente conectado al servidor " + ip_servidor + ":" + puerto_servidor);
    }

    public void cerrarSesion() throws RemoteException {
        servidor.salir(user);
        servidor = null;
        log("cliente desconectado del servidor");
    }

    public void enviar(String user, Mensaje msg) {
        try {
            msg.setUsuario(this.user);

            try {
                msg.encriptar(amigos.get(user).secreto);
            } catch (InvalidKeyException | IllegalBlockSizeException | BadPaddingException e) {
                debug("No se ha podido encriptar el mensaje a" + user, Color.ROJO);
            }

            if (!msg.encriptado())
                debug("mensaje enviado sin encriptar a " + user + "!", Color.ROJO);

            Amigo amigo = amigos.get(user);

            if (amigo == null)
                return;
                //throw new RemoteException("el usuario " + user + " no existe");
            if (!amigo.estaConectado())
                return;
                //throw new RemoteException("el usuario " + user + " no está conectado");
            
            amigo.mensajes.add(msg);

            amigo.conexion.recibir(this.user, msg);

        } catch (RemoteException e) {
            debug("no se ha podido enviar el mensaje a:" + user, Color.ROJO);
        }
    }

    public void cambiarPassword(String oldPassword, String newPassword) throws RemoteException{

        servidor.cambiarPassword(user, oldPassword, newPassword);
    }

    public boolean estaConectado() {
        return servidor != null;
    }

    private Map<String, Amigo> getAmigosConectados() {
        return amigos.entrySet().stream()
                .filter(e -> e.getValue().conexion != null)
                .collect(Collectors.toMap(e -> e.getKey(), e -> e.getValue()));
    }
}
