package shared;

import java.io.Serializable;
import java.rmi.Remote;
import java.rmi.RemoteException;

public interface ICliente extends Remote, Serializable {

    /*
     * Como verificamos que sea solo el servidor el que lo usa?
    */
    /**
     * Recibe una notificacion del servidor
     *
     * @param e Tipo del evento
     * @param o Datos de la notificacion
     */
    public void notificar(EventoConexion e, Object o) throws RemoteException;

    /**
     * Devuelve el nombre del usuario
     * 
     * @return Nombre del usuario
     * @throws RemoteException
     */
    public String getUsuario() throws RemoteException;
    
    /**
     * Recibe un mensaje de otro cliente
     *
     * @param user Usuario que envia el mensaje
     * @param msg  Mensaje
     */
    public void recibir(String user, Mensaje msg) throws RemoteException;

    /**
     * Devuelve la llave pública del cliente
     * 
     * @return Llave pública
     */
    public byte[] pubkey() throws RemoteException;
}
