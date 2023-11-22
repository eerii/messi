package cliente;

import java.io.Serializable;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

import servidor.EventoConexion;
import utils.Utils;

public interface ICliente extends Remote, Serializable {
    public class Mensaje implements Serializable {
        String msg;
        String usuario;
        LocalDateTime hora;

        public Mensaje(String msg) {
            this.msg = msg;
            this.hora = LocalDateTime.now();
        }

        public String hora() {
            return hora.format(Utils.fmt);
        }

        public Instant instant() {
            return hora.toInstant(ZoneOffset.UTC);
        }

        public void setUsuario(String usuario) {
            this.usuario = usuario;
        }

        public String getUsuario() {
            return usuario;
        }

        @Override
        public String toString() {
            return msg;
        }
    }

    /**
     * Recibe una notificacion del servidor
     *
     * @param e Tipo del evento
     * @param o Datos de la notificacion
     */
    public void notificar(EventoConexion e, Object o) throws RemoteException;

    /**
     * Envía un mensaje a otro cliente
     *
     * @param c   Cliente al que enviar el mensaje
     * @param msg Mensaje
     */
    public void enviar(ICliente c, Mensaje msg) throws RemoteException;

    /**
     * Recibe un mensaje de otro cliente
     *
     * @param c   Cliente que envia el mensaje
     * @param msg Mensaje
     */
    public void recibir(ICliente c, Mensaje msg) throws RemoteException;

    /**
     * Devuelve una representación del cliente
     * 
     * @return Representación del cliente
     */
    public String str() throws RemoteException;
}
