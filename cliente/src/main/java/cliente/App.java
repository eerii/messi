package cliente;

import net.sourceforge.argparse4j.ArgumentParsers;
import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.ArgumentParserException;
import net.sourceforge.argparse4j.inf.Namespace;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

import atlantafx.base.theme.PrimerDark;

public class App extends Application {
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

        try {
            ClienteImpl.set(puerto_c, puerto_s, ip_s);
        } catch (Exception e) {
            System.out.println("[C]: error iniciando cliente " + e.getMessage());
            System.exit(2);
        }

        // Lanzamos JavaFX
        launch();
    }

    @Override
    public void start(Stage stage) throws IOException {
        // Application.setUserAgentStylesheet(new PrimerLight().getUserAgentStylesheet());
        Application.setUserAgentStylesheet(new PrimerDark().getUserAgentStylesheet());

        FXMLLoader fxmlLoader = new FXMLLoader(App.class.getResource("LoginView.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 600, 400);

        stage.setTitle("Messi");
        stage.setScene(scene);
        stage.show();
    }
}
