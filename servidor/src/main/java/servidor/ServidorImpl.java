package servidor;

import static shared.Utils.debug;
import static shared.Utils.log;

import java.rmi.ConnectException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import servidor.controller.UsuarioService;
import shared.EventoConexion;
import shared.ICliente;
import shared.IServidor;
import shared.Utils.Color;


public class ServidorImpl extends UnicastRemoteObject implements IServidor {
    private int puerto;
    private String ip;
    private Map<String, ICliente> usuarios;
    private UsuarioService servicio;

    ServidorImpl(int puerto, UsuarioService servicio) throws RemoteException {
        super();
        usuarios = new HashMap<>();
        this.servicio = servicio;

        // Obtenemos la ip
        try {
            this.ip = java.net.InetAddress.getLocalHost().getHostAddress();
            this.puerto = puerto;
        } catch (java.net.UnknownHostException e) {
            throw new RemoteException("error al obtener la ip");
        }

    }

    // getters
    public int getPuerto() {
        return puerto;
    }

    public String getIp() {
        return ip;
    }

    // Funciones de la interfaz

    @Override
    public void registrar(String user, String pass) throws RemoteException{
        if (servicio.signup(user, pass))
            throw new RemoteException("el usuario " + user + " ya está registrado");
    }

    @Override
    public void eliminar(String user, String pass) throws RemoteException{
        if (!servicio.unsubscribe(user))
            throw new RemoteException("el usuario " + user + " no existe");
    }

    @Override
    public void conectar(ICliente c, String user, String pass) throws RemoteException {
        if (!servicio.existsUser(user))
            throw new RemoteException("el usuario " + user + " no existe");

        if (notificar(user, EventoConexion.PING, null))
            throw new RemoteException("el usuario " + user + " ya está conectado");

        if (!servicio.login(user, pass))
            throw new RemoteException("password del usuario " + user + " incorrecta" );

        usuarios.put(user, c);
        log(user + " se ha conectado", Color.AZUL);

        // Notificar a los usuarios que son sus amigos
        notificarAmigos(user, EventoConexion.CLIENTE_CONECTADO, user);

        // Notificar al nuevo cliente de la lista de sus amigos que están conectados
        notificar(user, EventoConexion.LISTA_CLIENTES, getAmigosConectados(user));

        // Notificar al cliente de las solicitudes de amistad pendientes
        servicio.getSolicitudes(user).forEach(s ->
            notificar(user, EventoConexion.SOLICITUD_AMISTAD, s));
    }

    @Override
    public void salir(String user) throws RemoteException {
        if (!servicio.existsUser(user))
            throw new RemoteException("el usuario " + user + " no existe");
        if (!usuarios.containsKey(user))
            throw new RemoteException("el usuario " + user + " no está conectado");
        desconectar(user);
    }

    @Override
    public ICliente buscar(String user) throws RemoteException {
        if (!usuarios.containsKey(user))
            return null;

        return usuarios.get(user);
    }

    @Override
    public boolean ping(String user) throws RemoteException {
        return notificar(user, EventoConexion.PING, "servidor");
    }

    @Override
    public void solicitarAmistad(String user, String amigo) throws RemoteException {
        if (!servicio.existsUser(user))
            throw new RemoteException("el usuario " + user + " no existe");
        if (!servicio.existsUser(amigo))
            throw new RemoteException("el usuario " + amigo + " no existe");
        
        // * Gestionar seguridad
        if (!usuarios.containsKey(user))
            throw new RemoteException("el usuario " + user + " no está conectado");
        
        if(!servicio.addSolicitud(amigo, user))
            throw new RemoteException("el usuario " + user + " ya es amigo del usuario " + amigo);
        
        log(user + " ha solicitado amistad a " + amigo, Color.AZUL);
        notificar(amigo, EventoConexion.SOLICITUD_AMISTAD, user);
    }

    @Override
    public void responderSolicitud(String user, String amigo, boolean respuesta) throws RemoteException {
        if (!servicio.existsUser(user))
            throw new RemoteException("el usuario " + user + " no existe");
        if (!servicio.existsUser(amigo))
            throw new RemoteException("el usuario " + amigo + " no existe");
        // * Gestionar seguridad
        if (!usuarios.containsKey(user))
            throw new RemoteException("el usuario " + user + " no está conectado");
        if (servicio.replySolicitud(user, amigo, respuesta))
            throw new RemoteException("no hay ninguna solicitud de amistad de " + amigo + " a " + user);

        if (respuesta){
            log(user + " y " + amigo + " ahora son amigos", Color.AZUL);
            notificar(amigo, EventoConexion.CLIENTE_CONECTADO, user);
            notificar(user, EventoConexion.CLIENTE_CONECTADO, amigo);
        }
    }

    // Funciones propias

    void desconectar(String user) {
        // Eliminar la conexión
        if (usuarios.containsKey(user)) {
            log(user + " se ha desconectado", Color.AZUL);
            usuarios.remove(user);
        }
        // Notificar a sus amigos
        notificarAmigos(user, EventoConexion.CLIENTE_DESCONECTADO, user);
    }

    void notificarAmigos(String user, EventoConexion e, Object o){
        Set<String> amigosConectados = getAmigosConectados(user);
        amigosConectados.forEach(amigo -> notificar(amigo, e, o));
    }

    Set<String> getAmigosConectados(String user){
        Set<String> amigosConectados = servicio.getAmigos(user);
        amigosConectados.retainAll(usuarios.keySet());
        return amigosConectados;
    }

    boolean notificar (String user, EventoConexion e, Object o){
        if (!usuarios.containsKey(user))
            return false;
        debug("notificando a  " + user + ": " + e, Color.AZUL);
        try {
            usuarios.get(user).notificar(e, o);
            debug(user + " ha sido notificado: " + e, Color.AZUL);
            return true;
        } catch (ConnectException ex ) {
            desconectar(user);
            debug(user + " no está conectado" + e, Color.ROJO);
            //log(ex.getMessage(), Color.ROJO);
            return false;
        } catch (RemoteException ex){
            desconectar(user);
            debug("Fallo notificando a  " + user + " de: " + e, Color.ROJO);
            //log(ex.getMessage(), Color.ROJO);
            return false;
        }
    }
}