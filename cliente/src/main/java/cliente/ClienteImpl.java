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
import java.rmi.ConnectException;
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

    String usuario;
    Map<String, Amigue> amigues;

    KeyPair keys;

    List<IObserver> observadores = new ArrayList<>();

    class Amigue {
        String usuario;
        List<Mensaje> mensajes;
        ICliente conexion;
        SecretKey secreto;

        Amigue(String user) {
            this.mensajes = new ArrayList<>();
            this.usuario = user;
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
            // Obtenemos la llave pública del amigue
            byte[] pubkey = null;
            try {
                pubkey = conexion.pubkey();
            } catch (RemoteException e) {
                log("error al obtener la llave pública de " + usuario);
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
                debug("secreto generado con " + this.usuario + ": " + emojiFromHex(bytesToHex(sec)));

                this.secreto = new SecretKeySpec(sec, "AES");
            } catch (NoSuchAlgorithmException | InvalidKeySpecException | InvalidKeyException e) {
                e.printStackTrace();
            }
        }
    }

    class MensajeDesencriptado {
        String usuario;
        String mensaje;
        String hora;

        MensajeDesencriptado(String usuario, String mensaje, String hora) {
            this.usuario = usuario;
            this.mensaje = mensaje;
            this.hora = hora;
        }
    }

    ClienteImpl() throws RemoteException {
        super(puerto);
        this.amigues = new HashMap<>();

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
    public String getUsuario() throws RemoteException{
        if (usuario == null)
            throw new ConnectException("el usuario no está conectado");
        return usuario;
    }

    @Override
    @SuppressWarnings("unchecked")
    public void notificar(EventoConexion e, Object o) throws RemoteException {
        switch (e) {
            case CLIENTE_CONECTADO: { // ICliente
                ICliente c = (ICliente) o;
                String user = c.getUsuario();

                // Añadimos el usuario a clientes y mensajes si no estaba
                if (!amigues.containsKey(user))
                    amigues.put(user, new Amigue(user));

                // Conectamos al usuario
                if (amigues.get(user).estaConectado())
                    break;
                amigues.get(user).conectar(c);

                // Añadimos a la interfaz
                notificarObservadores(EventoConexion.CLIENTE_CONECTADO, user);

                log(user + " se ha conectado");
                break;
            }
            case CLIENTE_DESCONECTADO: { // String
                String user = (String) o;
                if (amigues.containsKey(user))
                    amigues.get(user).desconectar();

                // Añadimos a la interfaz
                notificarObservadores(EventoConexion.CLIENTE_DESCONECTADO, user);

                log(user + " se ha desconectado");
                break;
            }
            case LISTA_AMIGUES: { // Set<ICliente>
                // Nueva lista de clientes (filtramos para que no se incluya a si mismo)
                Set<ICliente> clientes = (Set<ICliente>) o;
                List<String> amigues_conectados = new ArrayList<>();
                clientes.remove(this);

                log("lista de amigues: " + clientes.stream().map(c -> c.toString()).collect(Collectors.joining(", ")));

                // Añadimos a amigues
                for (ICliente c : clientes) {
                    String user = c.getUsuario();
                    amigues_conectados.add(user);
                    if(!amigues.containsKey(user)){
                        amigues.put(user, new Amigue(user));
                        amigues.get(user).conectar(c);
                    }
                }

                // Mostramos los clientes conectados
                if (clientes.size() == 0)
                    log("no hay clientes conectados");
                else
                    log("clientes conectados: " + amigues_conectados.stream().collect(Collectors.joining(", ")));

                break;
            }
            case SOLICITUD_AMISTAD: { // String
                // Añadimos a la interfaz
                notificarObservadores(EventoConexion.SOLICITUD_AMISTAD, o);
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
    public void recibir(String usuario, Mensaje msg) throws RemoteException {
        if (!msg.encriptado()) {
            debug("mensaje sin encriptar recibido de " + usuario + "!", Color.ROJO);
        }

        Amigue amigue = amigues.get(usuario);
        if (amigue == null)
            throw new RemoteException("el usuario " + usuario + " no existe");
        amigue.mensajes.add(msg);

        MensajeDesencriptado msg_des = new MensajeDesencriptado(usuario, msg.toString() + " ", msg.getHora());

        if (msg.encriptado())
            try {
                msg_des.mensaje = msg.desencriptar(amigue.secreto) + " ";
            } catch (InvalidKeyException | IllegalBlockSizeException | BadPaddingException e) {
                msg_des.mensaje = "Error al desencriptar";
            }
        log("mensaje recibido de " + usuario + ": " + msg_des.mensaje);

        // Añadimos a la interfaz
        notificarObservadores(EventoConexion.MENSAJE_RECIBIDO, msg_des);
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

    public void registrar(String usuario, String clave) throws RemoteException{
        if (servidor == null)
            conectarServidor();
        
        servidor.registrar(usuario, clave);
        this.usuario = usuario;

        log("cliente registrado en el servidor " + ip_servidor + ":" + puerto_servidor);
    }

    public void iniciarSesion(String usuario, String clave) throws RemoteException {
        if (servidor == null)
            conectarServidor();
        
        this.usuario = usuario;
        servidor.conectar((ICliente) this, usuario, clave);

        log("cliente conectado al servidor " + ip_servidor + ":" + puerto_servidor);
    }

    public void cerrarSesion() throws RemoteException {
        servidor.salir(usuario);
        this.servidor = null;
        this.usuario = null;

        log("cliente desconectado del servidor");
    }

    public void enviar(String usuario, Mensaje msg) throws RemoteException {
        try {
            msg.setUsuario(this.usuario);

            try {
                msg.encriptar(amigues.get(usuario).secreto);
            } catch (InvalidKeyException | IllegalBlockSizeException | BadPaddingException e) {
                debug("No se ha podido encriptar el mensaje a" + usuario, Color.ROJO);
            }

            if (!msg.encriptado())
                debug("mensaje enviado sin encriptar a " + usuario + "!", Color.ROJO);

            Amigue amigue = amigues.get(usuario);

            if (amigue == null)
                throw new RemoteException("el usuario " + usuario + " no existe");
            if (!amigue.estaConectado())
                throw new RemoteException("el usuario " + usuario + " no está conectado");
            
            amigue.mensajes.add(msg);

            amigue.conexion.recibir(this.usuario, msg);

        } catch (RemoteException e) {
            debug("no se ha podido enviar el mensaje a:" + usuario, Color.ROJO);
        }
    }

    public void cambiarClave(String oldPassword, String newPassword) throws RemoteException{
        servidor.cambiarClave(usuario, oldPassword, newPassword);
    }

    public Map<String, Amigue> getAmiguesConectados() {
        return amigues.entrySet().stream()
                .filter(e -> e.getValue().conexion != null)
                .collect(Collectors.toMap(e -> e.getKey(), e -> e.getValue()));
    }

    // Observadores

    public void addObservador(IObserver o) {
        observadores.add(o);
    }

    public void removeObservador(IObserver o) {
        observadores.remove(o);
    }

    public void notificarObservadores(EventoConexion e, Object o) {
        for (IObserver obs : observadores)
            obs.push(e, o);
    }
}
