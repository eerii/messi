package shared;

import java.io.Serializable;
import java.rmi.Remote;
import java.rmi.RemoteException;


// ? por qué extiende a Serializable?
public interface IServidor extends Remote, Serializable {

    /**
     * Registra un nuevo usuario al servidor
     * 
     * @param user Nombre del usuario
     * @param pass Contraseña del usuario (encriptada con bcrypt)
     */
    public void registrar(String user, String pass) throws RemoteException;

    /**
     * Un usuario registrado se da de baja del servidor
     * 
     * @param user Nombre del usuario
     * @param pass Contraseña el usuario
     */
    public void eliminar(String user, String pass) throws RemoteException;

    /**
     * Un cliente registrado se conecta al servidor
     * 
     * @param c    Interfaz remota del cliente
     * @param user Nombre del usuario
     * @param pass Contraseña del usuario (encriptada con bcrypt)
     */
    public void conectar(ICliente c, String user, String pass) throws RemoteException;

    /**
     * Un cliente registrado se desconecta del servidor
     * 
     * @param user Nombre del usuario
     */
    public void salir(String user) throws RemoteException;

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

    /**
     * Un usuario registrado puede cambiar su contraseña
     * 
     * @param user             Nombre del usuario
     * @param oldPassword      Antigua contraseña del usuario
     * @param newPassword      Nueva contraseña del usuario
     * @throws RemoteException
     */
    public void cambiarPassword(String user, String oldPassword, String newPassword) throws RemoteException;
}
