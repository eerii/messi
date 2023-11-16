package cliente;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class App {
    public static void main(String[] args) {
        if (args.length > 0 && args[0].equals("web")) {
            SpringApplication.run(App.class, args);
        }
        
        System.out.println("hola");
    }
}
