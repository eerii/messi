package servidor;

import java.rmi.RemoteException;
import java.util.Map;

import cliente.ClienteImpl;
import cliente.ICliente;
import cliente.Mensaje;

public class App {
    public static void main(String[] args) {
        // Argumentos: [puerto_servidor]
        int puerto = 6969;
        if (args.length > 0) {
            try {
                puerto = Integer.parseInt(args[0]);
            } catch (NumberFormatException e) {
                System.out.println("el puerto debe ser un entero");
                System.exit(1);
            }
        }

        // Creamos el objeto servidor
        ServidorImpl s = null;
        try {
            s = new ServidorImpl(puerto);
        } catch (RemoteException e) {
            System.out.println("error iniciando servidor " + e.getMessage());
            System.exit(2);
        }

        // FIX: Creamos un cliente de prueba
        // Esto está implementado mal y poco seguro
        try {
            Thread.sleep(1000);
            String user = "heybot";
            ICliente c = new ClienteImpl(puerto + 1, s.puerto, s.ip, user);

            while (true) {
                for (String u : s.conexiones.keySet()) {
                    if (u.equals(user))
                        continue;
                    try {
                        c.enviar(u, new Mensaje("hey"));
                    } catch (Exception ex) {
                        s.ping(u);
                    }
                }
                Thread.sleep(15000);
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }
}
