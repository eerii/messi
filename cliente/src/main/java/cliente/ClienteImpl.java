package cliente;

import cliente.views.MainView;
import shared.*;
import static shared.Utils.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

import com.vaadin.flow.component.UI;

public class ClienteImpl extends UnicastRemoteObject implements ICliente {
    int puerto;
    int puerto_servidor;
    String ip_servidor;
    IServidor servidor;

    String user;
    Map<String, Amigo> amigos;

    UI ui;
    MainView view;

    class Amigo {
        List<Mensaje> mensajes;
        ICliente conexion;

        Amigo() {
            this.mensajes = new ArrayList<>();
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

    public ClienteImpl(int puerto_c, int puerto_s, String ip_s) throws RemoteException {
        super(puerto_c);
        this.amigos = new HashMap<>();

        this.puerto = puerto_c;
        this.puerto_servidor = puerto_s;
        this.ip_servidor = ip_s;
    }

    // Getters y setters

    public void setUI(UI ui, MainView view) {
        this.ui = ui;
        this.view = view;

        ui.access(() -> {
            Map<String, Amigo> conectados = getAmigosConectados();
            view.actualizarClientes(conectados.keySet());
            for (String user : conectados.keySet()) {
                view.actualizarMensajes(user, conectados.get(user).mensajes);
            }
        });
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
                if (!amigos.containsKey(user))
                    amigos.put(user, new Amigo());

                // Conectamos al usuario
                if (amigos.get(user).estaConectado())
                    break;
                amigos.get(user).conectar(c);

                // Actualizamos la vista
                if (ui != null) {
                    ui.access(() -> {
                        view.actualizarClientes(getAmigosConectados().keySet());
                    });
                }

                log(user + " se ha conectado");
                break;
            }
            case CLIENTE_DESCONECTADO: { // String
                String user = (String) o;
                if (amigos.containsKey(user))
                    amigos.get(user).desconectar();

                // Actualizamos la vista
                if (ui != null) {
                    ui.access(() -> {
                        view.actualizarClientes(getAmigosConectados().keySet());
                    });
                }

                log(user + " se ha desconectado");
                break;
            }
            case LISTA_CLIENTES: { // List<String>
                // Nueva lista de clientes (filtramos para que no se incluya a si mismo)
                List<String> clientes = ((List<String>) o).stream()
                        .filter(u -> !u.equals(this.user))
                        .collect(Collectors.toList());

                // Añadimos a amigos
                for (String user : clientes) {
                    if (!amigos.containsKey(user))
                        amigos.put(user, new Amigo());
                    ICliente c = servidor.buscar(user);
                    amigos.get(user).conectar(c);
                }

                // Actualizamos la vista
                if (ui != null) {
                    ui.access(() -> {
                        view.actualizarClientes(getAmigosConectados().keySet());
                    });
                }

                // Mostramos los clientes conectados
                if (clientes.size() == 0)
                    log("no hay clientes conectados");
                else
                    log("clientes conectados: " + clientes.stream().collect(Collectors.joining(", ")));

                break;
            }
            case SOLICITUD_AMISTAD: { // String
                // FIX: Aceptar solicitud de amistad automáticamente
                servidor.responderSolicitud(this.user, (String) o, true);
            }
            case PING: { // String
                debug("ping de " + o);
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

            Amigo amigo = amigos.get(user);
            if (amigo == null)
                throw new RemoteException("el usuario " + user + " no existe");
            if (!amigo.estaConectado())
                throw new RemoteException("el usuario " + user + " no está conectado");

            amigo.mensajes.add(msg);
            amigo.conexion.recibir(this.user, msg);

            // Actualizamos la vista
            if (ui != null) {
                ui.access(() -> {
                    view.actualizarMensajes(user, amigo.mensajes);
                });
            }
        } catch (RemoteException e) {
            servidor.ping(user);
        }
    }

    @Override
    public void recibir(String user, Mensaje msg) throws RemoteException {
        Amigo amigo = amigos.get(user);
        if (amigo == null)
            throw new RemoteException("el usuario " + user + " no existe");

        amigo.mensajes.add(msg);

        // Actualizamos la vista
        if (ui != null) {
            ui.access(() -> {
                view.actualizarMensajes(user, amigo.mensajes);
            });
        }

        log("mensaje recibido de " + user + ": " + msg);
    }

    // Funciones propias

    public void iniciarSesion(String user) throws RemoteException {
        this.user = user;

        // Nos conectamos al servidor y pasamos la interfaz
        Registry registro = LocateRegistry.getRegistry(ip_servidor, puerto_servidor);
        try {
            servidor = (IServidor) registro.lookup("Servidor");
        } catch (NotBoundException e) {
            throw new RemoteException("error al buscar el servidor en el registro");
        }
        servidor.conectar((ICliente) this, user);
        log("cliente conectado al servidor " + ip_servidor + ":" + puerto_servidor);
    }

    public void cerrarSesion() throws RemoteException {
        servidor.salir(user);
        servidor = null;
        log("cliente desconectado del servidor");
    }

    public boolean estaConectado() {
        return servidor != null;
    }

    public Map<String, Amigo> getAmigosConectados() {
        return amigos.entrySet().stream()
                .filter(e -> e.getValue().conexion != null)
                .collect(Collectors.toMap(e -> e.getKey(), e -> e.getValue()));
    }
}
