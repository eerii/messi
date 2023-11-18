package servidor;

import java.rmi.RemoteException;

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
        try {
            new ServidorImpl(puerto);
        } catch (RemoteException e) {
            System.out.println("error iniciando servidor " + e.getMessage());
            System.exit(2);
        }
    }
}
