package cliente;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class App {
    static ClienteImpl cliente;

    public static void main(String[] args) {
        // Argumentos: (web) [puerto_cliente] [puerto_servidor] [ip_servidor]
        List<String> a = new ArrayList<>(Arrays.asList(args));
        int puerto_c = 6900;
        int puerto_s = 6969;
        String ip_s = "localhost";

        if (a.size() > 0 && a.get(0).equals("web")) {
            a.remove(0);
            SpringApplication.run(App.class, args);
        }

        if (a.size() > 0) {
            try {
                puerto_c = Integer.parseInt(a.remove(0));
            } catch (NumberFormatException e) {
                System.out.println("el puerto del cliente debe ser un entero");
                System.exit(1);
            }
        }

        if (a.size() > 0) {
            try {
                puerto_s = Integer.parseInt(a.remove(0));
            } catch (NumberFormatException e) {
                System.out.println("el puerto del servidor debe ser un entero");
                System.exit(1);
            }
        }

        if (a.size() > 0) {
            ip_s = a.remove(0);
        }

        // Creamos el objeto cliente
        try {
            cliente = new ClienteImpl(puerto_c, puerto_s, ip_s);
        } catch (Exception e) {
            System.out.println("[C]: error iniciando cliente " + e.getMessage());
            System.exit(2);
        }
    }

    public static ClienteImpl get() {
        return cliente;
    }
}
