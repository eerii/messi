package cliente;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

import servidor.IServidor;

public class ClienteImpl extends UnicastRemoteObject implements ICliente {
    int puerto;
    String ip;
    IServidor servidor;

    protected ClienteImpl(int puerto_c, int puerto_s, String ip_s) throws RemoteException {
        super(puerto_c);

        // Obtenemos la ip
        try {
            this.ip = java.net.InetAddress.getLocalHost().getHostAddress();
            this.puerto = puerto_c;
        } catch (java.net.UnknownHostException e) {
            throw new RemoteException("error al obtener la ip");
        }

        // Nos conectamos al servidor y pasamos la interfaz
        Registry registro = LocateRegistry.getRegistry(ip_s, puerto_s);
        try {
            servidor = (IServidor) registro.lookup("Servidor");
        } catch (NotBoundException e) {
            throw new RemoteException("error al buscar el servidor en el registro");
        }
        servidor.conectar((ICliente) this);
        System.out.println("[C]: cliente conectado al servidor " + ip_s + ":" + puerto_s);

        servidor.ping(this);
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
        }
        servidor.salir(this);
        unexportObject(this, true);
        System.exit(0);
    }

    @Override
    public boolean ping(IServidor s) throws RemoteException {
        System.out.println("[C]: ping de " + s.str());
        return true;
    }

    @Override
    public boolean ping(ICliente c) throws RemoteException {
        System.out.println("[C]: ping de " + c.str());
        return true;
    }

    @Override
    public String str() throws RemoteException {
        return ip + ":" + puerto;
    }
}
