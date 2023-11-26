package shared;

import static shared.Utils.log;

import java.io.Serializable;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

import javax.crypto.SecretKey;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

public class Mensaje implements Serializable {
    static final Cipher cipher;
    String msg;
    String user;
    LocalDateTime hora;
    boolean encriptado;

    public Mensaje(String msg) {
        this.msg = msg;
        this.hora = LocalDateTime.now();
        this.encriptado = false;
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

    public void encriptar(SecretKey key)
            throws InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
        cipher.init(Cipher.ENCRYPT_MODE, key);
        byte[] bytes = cipher.doFinal(msg.getBytes(StandardCharsets.UTF_8));
        this.msg = Base64.getEncoder().encodeToString(bytes);
        this.encriptado = true;
    }

    public String desencriptar(SecretKey key)
            throws InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
        cipher.init(Cipher.DECRYPT_MODE, key);
        byte[] bytes = Base64.getDecoder().decode(msg);
        return new String(cipher.doFinal(bytes), StandardCharsets.UTF_8);
    }

    public String getUsuario() {
        return user;
    }

    public boolean encriptado() {
        return encriptado;
    }

    @Override
    public String toString() {
        return msg;
    }

    static {
        try {
            cipher = Cipher.getInstance("AES");
        } catch (NoSuchAlgorithmException | NoSuchPaddingException e) {
            throw new RuntimeException("error creando el cifrado: ", e);
        }
    }
}
