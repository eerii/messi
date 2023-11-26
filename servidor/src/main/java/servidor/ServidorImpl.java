package servidor;

import shared.ICliente;
import shared.IServidor;
import shared.EventoConexion;
import static shared.Utils.*;

import java.rmi.AlreadyBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ServidorImpl extends UnicastRemoteObject implements IServidor {
    Registry registro;
    int puerto;
    String ip;
    Map<String, Usuario> usuarios;

    class Usuario {
        ICliente conexion;
        List<String> amigos;
        List<String> solicitudes;

        Usuario() {
            this.amigos = new ArrayList<>();
            this.solicitudes = new ArrayList<>();
        }

        void conectar(ICliente c) {
            this.conexion = c;
        }

        void desconectar() {
            this.conexion = null;
        }

        boolean estaConectado() {
            return this.conexion != null;
        }
    }

    // TODO: Bases de datos
    // https://www.baeldung.com/spring-boot-h2-database

    ServidorImpl(int puerto) throws RemoteException {
        super();
        usuarios = new HashMap<>();

        // Obtenemos la ip
        try {
            this.ip = java.net.InetAddress.getLocalHost().getHostAddress();
            this.puerto = puerto;
        } catch (java.net.UnknownHostException e) {
            throw new RemoteException("error al obtener la ip");
        }

        // Registramos el servidor en el registro rmi
        registro = LocateRegistry.createRegistry(puerto);
        try {
            registro.bind("Servidor", (IServidor) this);
        } catch (AlreadyBoundException e) {
            throw new RemoteException("error al vincular el servidor al registro");
        }
        log("servidor iniciado en " + ip + ":" + puerto, Color.AZUL);
    }

    // Funciones de la interfaz

    @Override
    public void conectar(ICliente c, String user) throws RemoteException {
        comprobarCliente(user);
        if (!usuarios.containsKey(user))
            usuarios.put(user, new Usuario());

        if (usuarios.get(user).estaConectado())
            throw new RemoteException("el usuario " + user + " ya está conectado");

        // Añadir la conexión
        usuarios.get(user).conectar(c);
        log(user + " se ha conectado", Color.AZUL);

        // FIX: Temporal, solicitar amistad a todos los usuarios conextados
        for (String u : usuarios.keySet()) {
            if (u.equals(user))
                continue;
            try {
                solicitarAmistad(user, u);
            } catch (RemoteException e) {
                log("error solicitando amistad a " + u + ": " + e.getMessage(), Color.ROJO);
            }
        }

        // Notificar a los usuarios que son sus amigos
        for (String u : usuarios.get(user).amigos) {
            try {
                usuarios.get(u).conexion.notificar(EventoConexion.CLIENTE_CONECTADO, user);
            } catch (RemoteException e) {
                eliminarCliente(u);
            }
        }

        // Notificar al nuevo cliente de la lista de sus amigos que están conectados
        c.notificar(EventoConexion.LISTA_CLIENTES, usuarios.get(user).amigos.stream()
                .filter(u -> usuarios.get(u).estaConectado())
                .collect(Collectors.toList()));
    }

    @Override
    public void salir(String user) throws RemoteException {
        if (!usuarios.containsKey(user))
            throw new RemoteException("el usuario " + user + " no existe");

        eliminarCliente(user);
    }

    @Override
    public ICliente buscar(String user) throws RemoteException {
        if (!usuarios.containsKey(user))
            return null;

        return usuarios.get(user).conexion;
    }

    @Override
    public boolean ping(String user) throws RemoteException {
        try {
            usuarios.get(user).conexion.notificar(EventoConexion.PING, "servidor");
            debug("ping de " + user);
        } catch (RemoteException e) {
            debug("ping fallido de " + user, Color.ROJO);
            eliminarCliente(user);
            return false;
        }
        return true;
    }

    @Override
    public void solicitarAmistad(String user, String amigo) throws RemoteException {
        if (!usuarios.containsKey(user))
            throw new RemoteException("el usuario " + user + " no existe");

        if (!usuarios.containsKey(amigo))
            throw new RemoteException("el usuario " + amigo + " no existe");

        usuarios.get(amigo).solicitudes.add(user);
        log(user + " ha solicitado amistad a " + amigo, Color.AZUL);

        usuarios.get(amigo).conexion.notificar(EventoConexion.SOLICITUD_AMISTAD, user);
    }

    @Override
    public void responderSolicitud(String user, String amigo, boolean respuesta) throws RemoteException {
        if (!usuarios.containsKey(user))
            throw new RemoteException("el usuario " + user + " no existe");

        if (!usuarios.containsKey(amigo))
            throw new RemoteException("el usuario " + amigo + " no existe");

        if (!usuarios.get(user).solicitudes.contains(amigo))
            throw new RemoteException("no hay ninguna solicitud de amistad de " + amigo + " a " + user);
        usuarios.get(user).solicitudes.remove(amigo);

        if (respuesta) {
            log(user + " y " + amigo + " ahora son amigos", Color.AZUL);
            usuarios.get(user).amigos.add(amigo);
            usuarios.get(amigo).amigos.add(user);

            if (usuarios.get(amigo).estaConectado()) {
                usuarios.get(amigo).conexion.notificar(EventoConexion.CLIENTE_CONECTADO, user);
            }
            if (usuarios.get(user).estaConectado()) {
                usuarios.get(user).conexion.notificar(EventoConexion.CLIENTE_CONECTADO, amigo);
            }
        }
    }

    // Funciones propias

    void eliminarCliente(String user) {
        // Notificar al resto de clientes
        for (String u : usuarios.keySet()) {
            if (u.equals(user))
                continue;

            try {
                usuarios.get(u).conexion.notificar(EventoConexion.CLIENTE_DESCONECTADO, user);
            } catch (RemoteException e) {
                eliminarCliente(u);
            }
        }

        // Eliminar la conexión
        if (usuarios.containsKey(user)) {
            log(user + " se ha desconectado", Color.AZUL);
            usuarios.remove(user);
        }
    }

    void comprobarCliente(String user) {
        if (usuarios.containsKey(user)) {
            log("comprobando " + user, Color.AZUL);
            try {
                usuarios.get(user).conexion.notificar(EventoConexion.PING, null);
            } catch (RemoteException e) {
                eliminarCliente(user);
            }
        }
    }
}
