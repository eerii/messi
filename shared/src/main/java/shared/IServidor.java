package shared;

import java.io.Serializable;
import java.rmi.Remote;
import java.rmi.RemoteException;

// ? por qué extiende a Serializable?
public interface IServidor extends Remote, Serializable {
    /**
     * Añade un nuevo cliente al serividor
     * 
     * @param c    Interfaz remota del cliente
     * @param user Nombre del usuario
     * @param pass Contraseña del usuario (encriptada con bcrypt)
     */
    public void conectar(ICliente c, String user, String pass) throws RemoteException;

    /**
     * Elimina un cliente del servidor
     * 
     * @param user Nombre del usuario
     */
    public void salir(String user) throws RemoteException;

    /**
     * ? Hace falta esta función en la interfaz ?
     * Obtiene un cliente mediante su nombre
     * 
     * @param user Nombre del usuario
     * @return null si el usuario no está conectado, su interfaz si sí lo está
     */
    public ICliente buscar(String user) throws RemoteException;

    /**
     * ? Verdaderamente debería ser una función de la interfaz
     * Comprueba si el servidor está activo
     * Manda un ping de vuelta al cliente
     * 
     * @param user Nombre del usuario
     * @return true si el servidor está activo, false en caso contrario
     */
    public boolean ping(String user) throws RemoteException;

    /**
     * Solicita a un usuario ser su amigo
     *
     * @param user  Nombre del usuario
     * @param amigo Nombre del amigo
     */
    public void solicitarAmistad(String user, String amigo) throws RemoteException;

    /**
     * Responde a una solicitud de amistad
     *
     * @param user      Nombre del usuario
     * @param amigo     Nombre del amigo
     * @param respuesta true si acepta la solicitud, false en caso contrario
     */
    public void responderSolicitud(String user, String amigo, boolean respuesta) throws RemoteException;
}
