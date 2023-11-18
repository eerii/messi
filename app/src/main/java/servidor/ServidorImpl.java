package servidor;

import java.rmi.AlreadyBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashSet;
import java.util.Set;

import cliente.ICliente;
import static utils.Utils.*;

public class ServidorImpl extends UnicastRemoteObject implements IServidor {
    Registry registro;
    int puerto;
    String ip;

    Set<ICliente> conexiones;

    protected ServidorImpl(int puerto) throws RemoteException {
        super();
        conexiones = new HashSet<>();

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
        log("servidor iniciado en " + ip + ":" + puerto);
    }

    @Override
    public void conectar(ICliente c) throws RemoteException {
        if (conexiones.contains(c))
            throw new RemoteException("la conexion " + c.str() + " ya existe");

        // Notificar al resto de clientes
        for (ICliente cc : conexiones) {
            cc.notificar(EventoConexion.CLIENTE_CONECTADO, c);
        }

        // Notificar al nuevo cliente de la lista de usuarios
        c.notificar(EventoConexion.LISTA_CLIENTES, conexiones);

        // Añadir la conexión
        conexiones.add(c);
        log(c.str() + " se ha conectado");
    }

    @Override
    public void salir(ICliente c) throws RemoteException {
        if (!conexiones.contains(c))
            throw new RemoteException("la conexion " + c.str() + " no existe");

        // Eliminar la conexión
        conexiones.remove(c);
        log(c.str() + " se ha desconectado");

        // Notificar al resto de clientes
        for (ICliente cc : conexiones) {
            cc.notificar(EventoConexion.CLIENTE_DESCONECTADO, c);
        }
    }

    @Override
    public boolean ping(ICliente c) throws RemoteException {
        log("ping de " + c.str());
        c.notificar(EventoConexion.PING, this);
        return true;
    }

    @Override
    public String str() throws RemoteException {
        return "S" + ip + ":" + puerto;
    }
}
