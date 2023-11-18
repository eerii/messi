package cliente;

import java.io.Serializable;
import java.rmi.Remote;
import java.rmi.RemoteException;

import servidor.IServidor;

public interface ICliente extends Remote, Serializable {
    /**
     * Ping del servidor
     * 
     * @return true si el cliente está activo, false en caso contrario
     */
    public boolean ping(IServidor s) throws RemoteException;

    /**
     * Ping de otro cliente
     * 
     * @return true si el servidor está activo, false en caso contrario
     */
    public boolean ping(ICliente c) throws RemoteException;

    /**
     * Devuelve una representación del cliente
     * 
     * @return Representación del cliente
     */
    public String str() throws RemoteException;
}
