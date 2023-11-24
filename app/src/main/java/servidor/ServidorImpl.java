package servidor;

import java.rmi.AlreadyBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashMap;
import java.util.Map;

import cliente.ICliente;
import static utils.Utils.*;

public class ServidorImpl extends UnicastRemoteObject implements IServidor {
    Registry registro;
    int puerto;
    String ip;

    Map<String, ICliente> conexiones;

    ServidorImpl(int puerto) throws RemoteException {
        super();
        conexiones = new HashMap<>();

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
        if (conexiones.containsKey(user))
            throw new RemoteException("el usuario " + user + " ya está conectado");

        // Añadir la conexión
        conexiones.put(user, c);
        log(user + " se ha conectado", Color.AZUL);

        // Notificar al resto de clientes
        for (Map.Entry<String, ICliente> e : conexiones.entrySet()) {
            if (e.getKey().equals(user))
                continue;
            try {
                e.getValue().notificar(EventoConexion.CLIENTE_CONECTADO, user);
            } catch (RemoteException _e) {
                eliminarCliente(e.getKey());
            }
        }

        // Notificar al nuevo cliente de la lista de usuarios
        c.notificar(EventoConexion.LISTA_CLIENTES, conexiones);
    }

    @Override
    public void salir(String user) throws RemoteException {
        if (!conexiones.containsKey(user))
            throw new RemoteException("el usuario " + user + " no existe");

        eliminarCliente(user);
    }

    @Override
    public ICliente buscar(String user) throws RemoteException {
        if (!conexiones.containsKey(user))
            return null;

        return conexiones.get(user);
    }

    @Override
    public boolean ping(String user) throws RemoteException {
        try {
            conexiones.get(user).notificar(EventoConexion.PING, null);
            debug("ping de " + user);
        } catch (RemoteException e) {
            debug("ping fallido de " + user, Color.ROJO);
            eliminarCliente(user);
            return false;
        }
        return true;
    }

    // Funciones propias

    void eliminarCliente(String user) {
        // Notificar al resto de clientes
        for (Map.Entry<String, ICliente> e : conexiones.entrySet()) {
            if (e.getKey().equals(user))
                continue;

            try {
                e.getValue().notificar(EventoConexion.CLIENTE_DESCONECTADO, user);
            } catch (RemoteException _e) {
                eliminarCliente(e.getKey());
            }
        }

        // Eliminar la conexión
        if (conexiones.containsKey(user)) {
            log(user + " se ha desconectado", Color.AZUL);
            conexiones.remove(user);
        }
    }

    void comprobarCliente(String user) {
        if (conexiones.containsKey(user)) {
            log("comprobando " + user, Color.AZUL);
            try {
                conexiones.get(user).notificar(EventoConexion.PING, null);
            } catch (RemoteException e) {
                eliminarCliente(user);
            }
        }
    }
}
