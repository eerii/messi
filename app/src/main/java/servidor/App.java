package servidor;

import java.rmi.RemoteException;

import cliente.ClienteImpl;
import cliente.ICliente;
import cliente.ICliente.Mensaje;

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
            Thread.sleep(3000);
            ICliente c = new ClienteImpl(puerto + 1, s.puerto, s.ip);

            while (true) {
                for (ICliente cc : s.conexiones.keySet()) {
                    try {
                        if (cc.str().equals(c.str()))
                            continue;
                        c.enviar(cc, new Mensaje("^-^"));
                    } catch (Exception e) {
                        s.ping(cc);
                    }
                }
                Thread.sleep(15000);
            }
        } catch (

        Exception e) {
            System.out.println(e.getMessage());
        }
    }
}
