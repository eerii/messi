package servidor;

import shared.ICliente;
import shared.IServidor;
import shared.Utils.Color;
import shared.EventoConexion;
import static shared.Utils.*;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.rmi.ConnectException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.checkerframework.checker.units.qual.s;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;
import org.springframework.stereotype.Service;

import servidor.controller.UsuarioService;
import servidor.model.Usuario;
import servidor.repository.UsuarioRepository;


public class ServidorImpl extends UnicastRemoteObject implements IServidor {
    private int puerto;
    private String ip;

    /*
     * Esta clase gestiona las conexiones rmi, separandolo de la lógica de guardado
     */
    private Map<String, ICliente> usuarios;


    UsuarioService servicio;


    // TODO: Bases de datos
    // https://www.baeldung.com/spring-boot-h2-database

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
    public void conectar(ICliente c, String user, String pass) throws RemoteException {

        if (estaConectado(user))
            throw new RemoteException("el usuario " + user + " ya está conectado");

        if (!servicio.login(user, pass))
            throw new RemoteException("password del usuario " + user + " incorrecta" );

        usuarios.put(user, c);
        log(user + " se ha conectado", Color.AZUL);

        // Notificar a los usuarios que son sus amigos
        notificarAmigos(user, EventoConexion.CLIENTE_CONECTADO, user);

        // Notificar al nuevo cliente de la lista de sus amigos que están conectados
        c.notificar(EventoConexion.LISTA_CLIENTES, getAmigosConectados(user));

        // Notificar al cliente de las solicitudes de amistad pendientes
        for (String s : servicio.getSolicitudes(user)) {
            c.notificar(EventoConexion.SOLICITUD_AMISTAD, s);
        }
    }


    @Override
    public void salir(String user) throws RemoteException {
        if (!usuarios.containsKey(user))
            throw new RemoteException("el usuario " + user + " no está conectado");
        servicio.logout(user);
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
        try {
            usuarios.get(user).notificar(EventoConexion.PING, "servidor");
            debug("ping de " + user);
        } catch (RemoteException e) {
            debug("ping fallido de " + user, Color.ROJO);
            desconectar(user);
            return false;
        }
        return true;
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
        if(estaConectado(amigo))
            usuarios.get(amigo).notificar(EventoConexion.SOLICITUD_AMISTAD, user);
        
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

        if (respuesta) {
            log(user + " y " + amigo + " ahora son amigos", Color.AZUL);

            if (estaConectado(amigo)) {
                usuarios.get(amigo).notificar(EventoConexion.CLIENTE_CONECTADO, user);
            }
            if (estaConectado(user)) {
                usuarios.get(user).notificar(EventoConexion.CLIENTE_CONECTADO, amigo);
            }
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
        amigosConectados.forEach(amigo -> {
            try {
                usuarios.get(amigo).notificar(e, o);
            } catch (RemoteException ex) {
                log(ex.getMessage(), Color.ROJO);

            }
        });
    }

    Set<String> getAmigosConectados(String user){
        Set<String> amigosConectados = servicio.getAmigos(user);
        amigosConectados.retainAll(usuarios.keySet());
        return amigosConectados;
    }

    boolean estaConectado (String user){
        if (!usuarios.containsKey(user))
            return false;
        log("comprobando conexion " + user, Color.AZUL);
        try {
            usuarios.get(user).notificar(EventoConexion.PING, null);
            log(user + " está conectado", Color.AZUL);
            return true;
        } catch (ConnectException e) {
            desconectar(user);
            return false;
        } catch (RemoteException e){
            desconectar(user);
            return false;
        }
    }
}
