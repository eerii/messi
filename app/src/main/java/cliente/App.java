package cliente;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import com.vaadin.flow.component.page.AppShellConfigurator;
import com.vaadin.flow.component.page.Push;
import com.vaadin.flow.server.PWA;
import com.vaadin.flow.theme.Theme;

import net.sourceforge.argparse4j.ArgumentParsers;
import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.ArgumentParserException;
import net.sourceforge.argparse4j.inf.Namespace;

@SpringBootApplication
@PWA(name = "Mess", shortName = "Mess")
@Theme("theme")
@Push
public class App implements AppShellConfigurator {
    static ClienteImpl cliente;

    public static void main(String[] args) {
        // Argumentos
        ArgumentParser parser = ArgumentParsers.newFor("Mess").build()
                .defaultHelp(true)
                .description("Cliente de mensajes decentralizado");

        parser.addArgument("-c", "--puerto-cliente")
                .type(Integer.class)
                .setDefault(6900)
                .help("Puerto del cliente");

        parser.addArgument("-s", "--puerto-servidor")
                .type(Integer.class)
                .setDefault(6969)
                .help("Puerto del servidor");

        parser.addArgument("-i", "--ip-servidor")
                .setDefault("localhost")
                .help("IP del servidor");

        parser.addArgument("-u", "--user")
                .help("Nombre de usuario");

        parser.addArgument("-w", "--web")
                .action(net.sourceforge.argparse4j.impl.Arguments.storeTrue())
                .help("Iniciar interfaz web");

        Namespace ns = null;
        try {
            ns = parser.parseArgs(args);
        } catch (ArgumentParserException e) {
            parser.handleError(e);
            System.exit(1);
        }

        int puerto_c = ns.getInt("puerto_cliente");
        int puerto_s = ns.getInt("puerto_servidor");
        String ip_s = ns.getString("ip_servidor");

        if (ns.getBoolean("web")) {
            SpringApplication.run(App.class, args);
        }

        // Creamos el objeto cliente
        try {
            cliente = new ClienteImpl(puerto_c, puerto_s, ip_s);

            // Si ha pasado credenciales, iniciar sesión automáticamente
            if (ns.getString("user") != null) {
                cliente.iniciarSesion(ns.getString("user"));
            }
        } catch (Exception e) {
            System.out.println("[C]: error iniciando cliente " + e.getMessage());
            System.exit(2);
        }
    }

    public static ClienteImpl get() {
        return cliente;
    }
}
