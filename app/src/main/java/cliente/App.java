package cliente;

public class App {
    public String getGreeting() {
        return "cliente";
    }

    public static void main(String[] args) {
        System.out.println(new App().getGreeting());
    }
}
