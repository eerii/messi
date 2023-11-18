package cliente;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.Set;
import java.util.stream.Collectors;

import servidor.EventoConexion;
import servidor.IServidor;
import static utils.Utils.*;

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
        log("cliente conectado al servidor " + ip_s + ":" + puerto_s);

        servidor.ping(this);
        try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {
        }

        servidor.salir(this);
        unexportObject(this, true);
        System.exit(0);
    }

    @Override
    public void notificar(EventoConexion e, Object o) throws RemoteException {
        switch (e) {
            case CLIENTE_CONECTADO: // ICliente
                log(((ICliente) o).str() + " se ha conectado");
                break;
            case CLIENTE_DESCONECTADO: // ICliente
                log(((ICliente) o).str() + " se ha desconectado");
                break;
            case LISTA_CLIENTES: // Set<ICliente>
                @SuppressWarnings("unchecked")
                Set<ICliente> l = (Set<ICliente>) o;

                if (l.isEmpty())
                    log("no hay clientes conectados");
                else
                    log("clientes conectados: " + l.stream().map(c -> {
                        try {
                            return c.str();
                        } catch (RemoteException e1) {
                            return "";
                        }
                    }).collect(Collectors.toSet()));
                break;
            case PING: // IServidor/ICliente
                if (o instanceof IServidor)
                    log("ping de " + ((IServidor) o).str());
                else if (o instanceof ICliente)
                    log("ping de " + ((ICliente) o).str());
                break;
            default:
                break;
        }
    }

    @Override
    public String str() throws RemoteException {
        return ip + ":" + puerto;
    }
}
