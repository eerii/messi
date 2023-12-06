package shared;

import java.io.Serializable;
import java.rmi.Remote;
import java.rmi.RemoteException;

public interface ICliente extends Remote, Serializable {


    /*
     * Como verificamos que sea solo el servidor el que lo usa?
     * 
    */
    /**
     * Recibe una notificacion del servidor
     *
     * @param e Tipo del evento
     * @param o Datos de la notificacion
     */
    public void notificar(EventoConexion e, Object o) throws RemoteException;

    
    /*
     * Realmente tiene que estar en al interfaz la función de enviar?
     * 
     * Puede ser fruto de inseguridad, y que otro usuario haga uso de esta función.
     * 
    */
    /**
     * Envía un mensaje a otro cliente
     *
     * @param user Usuario al que enviar el mensaje
     * @param msg  Mensaje
     */
    public void enviar(String user, Mensaje msg) throws RemoteException;

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
