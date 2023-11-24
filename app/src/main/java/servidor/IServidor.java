package servidor;

import java.io.Serializable;
import java.rmi.Remote;
import java.rmi.RemoteException;

import cliente.ICliente;

public interface IServidor extends Remote, Serializable {
    /**
     * Añade un nuevo cliente al serividor
     * 
     * @param c    Interfaz remota del cliente
     * @param user Nombre del usuario
     */
    public void conectar(ICliente c, String user) throws RemoteException;

    /**
     * Elimina un cliente del servidor
     * 
     * @param user Nombre del usuario
     */
    public void salir(String user) throws RemoteException;

    /**
     * Obtiene un cliente mediante su nombre
     * 
     * @param user Nombre del usuario
     * @return null si el usuario no está conectado, su interfaz si sí lo está
     */
    public ICliente buscar(String user) throws RemoteException;

    /**
     * Comprueba si el servidor está activo
     * Manda un ping de vuelta al cliente
     * 
     * @param user Nombre del usuario
     * @return true si el servidor está activo, false en caso contrario
     */
    public boolean ping(String user) throws RemoteException;
}
