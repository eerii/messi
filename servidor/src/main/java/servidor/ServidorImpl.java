package servidor;

import static shared.Utils.debug;
import static shared.Utils.log;

import java.rmi.ConnectException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashMap;
import java.util.HashSet;
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
        if (!servicio.signup(user, pass))
            throw new RemoteException("el usuario " + user + " ya está registrado");
    }

    @Override
    public void eliminar(String user, String pass) throws RemoteException{
        if (!servicio.unsubscribe(user))
            throw new RemoteException("el usuario " + user + " no existe");
    }

    @Override
    public void conectar(ICliente c, String usuario, String clave) throws RemoteException {
        if (!servicio.existsUser(usuario))
            throw new RemoteException("el usuario " + usuario + " no existe");
        if (notificar(usuario, EventoConexion.PING, null))
            throw new RemoteException("el usuario " + usuario + " ya está conectado");
        if (!servicio.login(usuario, clave))
            throw new RemoteException("contraseña del usuario " + usuario + " incorrecta" );

        usuarios.put(usuario, c);
        log(usuario + " se ha conectado", Color.AZUL);

        // Notificar a los usuarios que son sus amigues
        notificarAmigues(usuario, EventoConexion.CLIENTE_CONECTADO, c);

        // Notificar al nuevo cliente de la lista de sus amigues que están conectados
        Set <ICliente> amigues = new HashSet<>();
        getAmiguesConectados(usuario).forEach(a -> amigues.add(usuarios.get(a)));
        notificar(usuario, EventoConexion.LISTA_AMIGUES, amigues);

        // Notificar al cliente de las solicitudes de amistad pendientes
        servicio.getSolicitudes(usuario).forEach(s ->
            notificar(usuario, EventoConexion.SOLICITUD_AMISTAD, s));
    }

    @Override
    public void salir(String usuario) throws RemoteException {
        if (!servicio.existsUser(usuario))
            throw new RemoteException("el usuario " + usuario + " no existe");
        if (!usuarios.containsKey(usuario))
            throw new RemoteException("el usuario " + usuario + " no está conectado");
        desconectar(usuario);
    }

    @Override
    public void solicitarAmistad(String usuario, String amigue) throws RemoteException {
        if (!servicio.existsUser(usuario))
            throw new RemoteException("el usuario " + usuario + " no existe");
        if (!servicio.existsUser(amigue))
            throw new RemoteException("el usuario " + amigue + " no existe");
        // * Gestionar seguridad
        if (!usuarios.containsKey(usuario))
            throw new RemoteException("el usuario " + usuario + " no está conectado");
        if(!servicio.addSolicitud(amigue, usuario))
            throw new RemoteException("el usuario " + usuario + " ya es amigue del usuario " + amigue);
        
        log(usuario + " ha solicitado amistad a " + amigue, Color.AZUL);
        notificar(amigue, EventoConexion.SOLICITUD_AMISTAD, usuario);
    }

    @Override
    public void responderSolicitud(String usuario, String amigue, boolean respuesta) throws RemoteException {
        if (!servicio.existsUser(usuario))
            throw new RemoteException("el usuario " + usuario + " no existe");
        if (!servicio.existsUser(amigue))
            throw new RemoteException("el usuario " + amigue + " no existe");
        if (!usuarios.containsKey(usuario)) {
            log("no se puede responder, el usuario " + usuario + " no está conectado", Color.ROJO);
            //throw new RemoteException("el usuario " + usuario + " no está conectado");
            return;
        }
        if (!servicio.replySolicitud(usuario, amigue, respuesta))
            throw new RemoteException("no hay ninguna solicitud de amistad de " + amigue + " a " + usuario);

        if (respuesta){
            log(usuario + " y " + amigue + " ahora son amigues", Color.AZUL);
            // Notificar solo si el usuario está conectado
            if (usuarios.containsKey(amigue) && usuarios.containsKey(usuario)) {
                notificar(amigue, EventoConexion.CLIENTE_CONECTADO, usuarios.get(usuario));
                notificar(usuario, EventoConexion.CLIENTE_CONECTADO, usuarios.get(amigue));
            }
        }
    }

    @Override
    public void cambiarClave(String user, String antigua, String nueva) throws RemoteException{
        if (!servicio.existsUser(user))
            throw new RemoteException("el usuario " + user + " no existe");
        if (!usuarios.containsKey(user))
            throw new RemoteException("el usuario " + user + " no está conectado");
            
        servicio.changePassword(user, antigua, nueva);
    }

    @Override
    public void eliminarAmigo(String user, String antiguoAmigo) throws RemoteException{
        if (!servicio.existsUser(user))
            throw new RemoteException("el usuario " + user + " no existe");
        if (!servicio.existsUser(antiguoAmigo))
            throw new RemoteException("el usuario " + antiguoAmigo + " no existe");
        if (!usuarios.containsKey(user))
            throw new RemoteException("el usuario " + user + " no está conectado");
        
        if(!servicio.removeAmigo(user, antiguoAmigo))
            throw new RemoteException("el usuario " + user + "no era amigo de " + antiguoAmigo);
        if(usuarios.containsKey(antiguoAmigo))
            notificar(antiguoAmigo, EventoConexion.CLIENTE_DESCONECTADO, user);
    }

    // Funciones propias

    void desconectar(String usuario) {
        // Eliminar la conexión
        if (usuarios.containsKey(usuario)) {
            log(usuario + " se ha desconectado", Color.AZUL);
            usuarios.remove(usuario);
        }
        // Notificar a sus amigues
        notificarAmigues(usuario, EventoConexion.CLIENTE_DESCONECTADO, usuario);
    }

    void notificarAmigues(String usuario, EventoConexion e, Object o){
        Set<String> amiguesConectados = getAmiguesConectados(usuario);
        amiguesConectados.forEach(amigue -> notificar(amigue, e, o));
    }

    Set<String> getAmiguesConectados(String usuario){
        Set<String> amiguesConectados = servicio.getAmigos(usuario);
        amiguesConectados.retainAll(usuarios.keySet());
        return amiguesConectados;
    }

    boolean notificar (String usuario, EventoConexion e, Object o){
        if (!usuarios.containsKey(usuario))
            return false;
        debug("mandando " + e + " a '" + usuario + "'", Color.AZUL);
        try {
            usuarios.get(usuario).notificar(e, o);
            return true;
        } catch (ConnectException ex) {
            desconectar(usuario);
            debug("'" + usuario + "' no está conectado" + e, Color.ROJO);
            log(ex.getMessage(), Color.ROJO);
            return false;
        } catch (RemoteException ex){
            desconectar(usuario);
            debug("fallo notificando a '" + usuario + "' de: " + e, Color.ROJO);
            log(ex.getMessage(), Color.ROJO);
            return false;
        }
    }
}