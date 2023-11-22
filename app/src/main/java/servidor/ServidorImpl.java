package servidor;

import java.rmi.AlreadyBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import com.vaadin.flow.internal.Pair;

import cliente.ICliente;
import static utils.Utils.*;

public class ServidorImpl extends UnicastRemoteObject implements IServidor {
    Registry registro;
    int puerto;
    String ip;

    Map<ICliente, String> conexiones;

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
    public void conectar(ICliente c) throws RemoteException {
        comprobarCliente(c);
        if (conexiones.containsKey(c))
            throw new RemoteException("la conexion " + conexiones.get(c) + " ya existe");

        // Notificar al resto de clientes
        for (ICliente cc : conexiones.keySet()) {
            try {
                cc.notificar(EventoConexion.CLIENTE_CONECTADO, c);
            } catch (RemoteException e) {
                eliminarCliente(cc);
            }
        }

        // Añadir la conexión
        conexiones.put(c, c.str());
        log(c.str() + " se ha conectado", Color.AZUL);

        // Notificar al nuevo cliente de la lista de usuarios
        c.notificar(EventoConexion.LISTA_CLIENTES, new HashSet<>(conexiones.keySet()));
    }

    @Override
    public void salir(ICliente c) throws RemoteException {
        if (!conexiones.containsKey(c))
            throw new RemoteException("la conexion " + conexiones.get(c) + " no existe");

        eliminarCliente(c);
    }

    @Override
    public boolean ping(ICliente c) throws RemoteException {
        debug("ping de " + conexiones.get(c));
        try {
            c.notificar(EventoConexion.PING, null);
        } catch (RemoteException e) {
            eliminarCliente(c);
            return false;
        }
        return true;
    }

    @Override
    public String str() throws RemoteException {
        return "S" + ip + ":" + puerto;
    }

    // Funciones propias

    void eliminarCliente(ICliente c) {
        // Notificar al resto de clientes
        for (ICliente cc : conexiones.keySet()) {
            if (c.equals(cc))
                continue;
            try {
                cc.notificar(EventoConexion.CLIENTE_DESCONECTADO, new Pair<ICliente, String>(c, conexiones.get(c)));
            } catch (RemoteException e) {
                eliminarCliente(cc);
            }
        }

        // Eliminar la conexión
        if (conexiones.containsKey(c)) {
            log(conexiones.get(c) + " se ha desconectado", Color.AZUL);
            conexiones.remove(c);
        }
    }

    void comprobarCliente(ICliente c) {
        if (conexiones.containsKey(c)) {
            log("comprobando " + conexiones.get(c), Color.AZUL);
            try {
                c.notificar(EventoConexion.PING, null);
            } catch (RemoteException e) {
                eliminarCliente(c);
            }
        }
    }
}
