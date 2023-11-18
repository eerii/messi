package cliente;

import java.io.Serializable;
import java.rmi.Remote;
import java.rmi.RemoteException;

import servidor.EventoConexion;

public interface ICliente extends Remote, Serializable {
    /**
     * Recibe una notificacion del servidor
     *
     * @param e Tipo del evento
     * @param o Datos de la notificacion
     */
    public void notificar(EventoConexion e, Object o) throws RemoteException;

    /**
     * Devuelve una representación del cliente
     * 
     * @return Representación del cliente
     */
    public String str() throws RemoteException;
}
