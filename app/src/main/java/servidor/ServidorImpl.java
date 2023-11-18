package servidor;

import java.rmi.AlreadyBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

import java.util.HashSet;
import java.util.Set;

import cliente.ICliente;

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

        System.out.println("[S]: servidor listo en el puerto " + puerto);
    }

    @Override
    public void conectar(ICliente c) throws RemoteException {
        if (conexiones.contains(c))
            throw new RemoteException("la conexion " + c.str() + " ya existe");

        conexiones.add(c);
        System.out.println("[S]: " + c.str() + " se ha conectado");
    }

    @Override
    public void salir(ICliente c) throws RemoteException {
        if (!conexiones.contains(c))
            throw new RemoteException("la conexion " + c.str() + " no existe");

        conexiones.remove(c);
        System.out.println("[S]: " + c.str() + " se ha desconectado");
    }

    @Override
    public boolean ping(ICliente c) throws RemoteException {
        System.out.println("[S]: ping de " + c.str());
        c.ping(this);
        return true;
    }

    @Override
    public String str() throws RemoteException {
        return "S" + ip + ":" + puerto;
    }
}
