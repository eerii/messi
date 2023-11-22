package cliente;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.vaadin.flow.internal.Pair;

import servidor.EventoConexion;
import servidor.IServidor;
import static utils.Utils.*;

public class ClienteImpl extends UnicastRemoteObject implements ICliente {
    int puerto;
    String ip;
    IServidor servidor;
    HashMap<ICliente, List<Mensaje>> clientes;

    public ClienteImpl(int puerto_c, int puerto_s, String ip_s) throws RemoteException {
        super(puerto_c);
        this.clientes = new HashMap<>();

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
    }

    // Funciones de interfaz

    @Override
    public void notificar(EventoConexion e, Object o) throws RemoteException {
        switch (e) {
            case CLIENTE_CONECTADO: { // ICliente
                ICliente c = (ICliente) o;
                if (!clientes.containsKey(c))
                    clientes.put(c, new ArrayList<>());
                log(c.str() + " se ha conectado");
                break;
            }
            case CLIENTE_DESCONECTADO: { // ICliente
                Pair<ICliente, String> c = (Pair<ICliente, String>) o;
                if (clientes.containsKey(c.getFirst())) {
                    clientes.remove(c.getFirst());
                    log(c.getSecond() + " se ha desconectado");
                }
                break;
            }
            case LISTA_CLIENTES: { // Set<ICliente>
                Set<ICliente> sc = ((Set<ICliente>) o).stream()
                        .filter(c -> c.hashCode() != this.hashCode())
                        .collect(Collectors.toSet());

                if (sc.isEmpty())
                    log("no hay clientes conectados");
                else
                    log("clientes conectados: " + sc.stream()
                            .map(c -> {
                                try {
                                    return c.str();
                                } catch (RemoteException e1) {
                                    return "";
                                }
                            })
                            .collect(Collectors.toSet()));

                for (ICliente c : sc) {
                    if (!clientes.containsKey(c))
                        clientes.put(c, new ArrayList<>());
                }
                break;
            }
            case PING: { // IServidor/ICliente
                if (o instanceof IServidor)
                    debug("ping de " + ((IServidor) o).str());
                else if (o instanceof ICliente)
                    debug("ping de " + ((ICliente) o).str());
                break;
            }
            default:
                break;
        }
    }

    @Override
    public void enviar(ICliente c, Mensaje msg) throws RemoteException {
        try {
            if (!clientes.containsKey(c))
                clientes.put(c, new ArrayList<>());

            msg.setUsuario(this.str());
            clientes.get(c).add(msg);
            c.recibir(this, msg);
        } catch (RemoteException e) {
            servidor.ping(c);
        }
    }

    @Override
    public void recibir(ICliente c, Mensaje msg) throws RemoteException {
        if (!clientes.containsKey(c))
            clientes.put(c, new ArrayList<>());
        clientes.get(c).add(msg);
        log("mensaje recibido de " + c.str() + " (" + msg.hora() + "): " + msg);
    }

    @Override
    public String str() throws RemoteException {
        return ip + ":" + puerto;
    }
}
