package cliente;

import java.io.Serializable;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

import utils.Utils;

public class Mensaje implements Serializable {
    String msg;
    String user;
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

    public void setUsuario(String user) {
        this.user = user;
    }

    public String getUsuario() {
        return user;
    }

    @Override
    public String toString() {
        return msg;
    }
}
