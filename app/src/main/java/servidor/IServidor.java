package servidor;

import java.io.Serializable;
import java.rmi.Remote;
import java.rmi.RemoteException;

import cliente.ICliente;

public interface IServidor extends Remote, Serializable {
    /**
     * Añade un nuevo cliente al serividor
     * 
     * @param c Interfaz remota del cliente
     */
    public void conectar(ICliente c) throws RemoteException;

    /**
     * Elimina un cliente del servidor
     * 
     * @param c Interfaz remota del cliente
     */
    public void salir(ICliente c) throws RemoteException;

    /**
     * Comprueba si el servidor está activo
     * Manda un ping de vuelta al cliente
     * 
     * @param c Interfaz remota del cliente
     * @return true si el servidor está activo, false en caso contrario
     */
    public boolean ping(ICliente c) throws RemoteException;

    /**
     * Devuelve una representación del servidor
     * 
     * @return Representación del servidor
     */
    public String str() throws RemoteException;
}
