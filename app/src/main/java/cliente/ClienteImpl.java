package cliente;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import servidor.EventoConexion;
import servidor.IServidor;
import static utils.Utils.*;

public class ClienteImpl extends UnicastRemoteObject implements ICliente {
    int puerto;
    String ip;
    String user;
    IServidor servidor;
    Map<String, ICliente> clientes;
    Map<String, List<Mensaje>> mensajes;

    public ClienteImpl(int puerto_c, int puerto_s, String ip_s, String user) throws RemoteException {
        super(puerto_c);
        this.user = user;
        this.clientes = new HashMap<>();
        this.mensajes = new HashMap<>();

        // Obtenemos la ip
        try {
            this.ip = java.net.InetAddress.getLocalHost().getHostAddress();
            this.puerto = puerto_c;
        } catch (java.net.UnknownHostException e) {
            throw new RemoteException("error al obtener la ip");
        }

        // TODO: Mover conectar a una función login

        // Nos conectamos al servidor y pasamos la interfaz
        Registry registro = LocateRegistry.getRegistry(ip_s, puerto_s);
        try {
            servidor = (IServidor) registro.lookup("Servidor");
        } catch (NotBoundException e) {
            throw new RemoteException("error al buscar el servidor en el registro");
        }
        servidor.conectar((ICliente) this, user);
        log("cliente conectado al servidor " + ip_s + ":" + puerto_s);
    }

    // Funciones de interfaz

    @Override
    @SuppressWarnings("unchecked")
    public void notificar(EventoConexion e, Object o) throws RemoteException {
        switch (e) {
            case CLIENTE_CONECTADO: { // String
                String user = (String) o;
                ICliente c = servidor.buscar(user);

                // Añadimos el usuario a clientes y mensajes si no estaba
                if (!clientes.containsKey(user))
                    clientes.put(user, c);
                if (!mensajes.containsKey(user))
                    mensajes.put(user, new ArrayList<>());

                log(user + " se ha conectado");
                break;
            }
            case CLIENTE_DESCONECTADO: { // String
                String user = (String) o;
                if (clientes.containsKey(user)) {
                    clientes.remove(user);
                    log(user + " se ha desconectado");
                }
                break;
            }
            case LISTA_CLIENTES: { // Map<String, ICliente>
                // Nueva lista de clientes (filtramos para que no se incluya a si mismo)
                clientes = ((Map<String, ICliente>) o).entrySet().stream()
                        .filter(e1 -> !e1.getKey().equals(this.user))
                        .collect(Collectors.toMap(e1 -> e1.getKey(), e1 -> e1.getValue()));

                // Creamos una lista de mensajes si no existe
                for (String user : clientes.keySet()) {
                    if (!mensajes.containsKey(user))
                        mensajes.put(user, new ArrayList<>());
                }

                // Mostramos los clientes conectados
                if (clientes.size() == 0)
                    log("no hay clientes conectados");
                else
                    log("clientes conectados: " + clientes.keySet().stream().collect(Collectors.joining(", ")));

                break;
            }
            case PING: { // IServidor/ICliente
                if (o instanceof ICliente) {
                    // Obtenemos el nombre de usuario e imprimimos el ping
                    ICliente c = (ICliente) o;
                    String user = clientes.entrySet().stream()
                            .filter(e1 -> e1.getValue().equals(c))
                            .findFirst()
                            .get().getKey();
                    debug("ping de " + user);
                } else {
                    debug("ping del servidor");
                }
                break;
            }
            default:
                break;
        }
    }

    @Override
    public void enviar(String user, Mensaje msg) throws RemoteException {
        try {
            msg.setUsuario(this.user);
            mensajes.get(user).add(msg);
            clientes.get(user).recibir(this.user, msg);
        } catch (RemoteException e) {
            servidor.ping(user);
        }
    }

    @Override
    public void recibir(String user, Mensaje msg) throws RemoteException {
        mensajes.get(user).add(msg);
        log("mensaje recibido de " + user + " (" + msg.hora() + "): " + msg);
    }
}
