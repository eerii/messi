package servidor;

import java.rmi.RemoteException;

import cliente.ClienteImpl;
import cliente.Mensaje;

import net.sourceforge.argparse4j.ArgumentParsers;
import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.ArgumentParserException;
import net.sourceforge.argparse4j.inf.Namespace;

public class App {
    public static void main(String[] args) {
        // Argumentos: [-p puerto_servidor]
        ArgumentParser parser = ArgumentParsers.newFor("Mess").build()
                .defaultHelp(true)
                .description("Servidor de mensajes decentralizado");

        parser.addArgument("-p", "--puerto-servidor")
                .type(Integer.class)
                .setDefault(6969)
                .help("Puerto del servidor");

        Namespace ns = null;
        try {
            ns = parser.parseArgs(args);
        } catch (ArgumentParserException e) {
            parser.handleError(e);
            System.exit(1);
        }

        int puerto = ns.getInt("puerto_servidor");

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
            ClienteImpl c = new ClienteImpl(puerto + 1, s.puerto, s.ip);
            c.iniciarSesion("heybot");

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
                Thread.sleep(10000);
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }
}
