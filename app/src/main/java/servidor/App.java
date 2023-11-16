package servidor;

public class App {
    public String getGreeting() {
        return "servidor";
    }

    public static void main(String[] args) {
        System.out.println(new App().getGreeting());
    }
}
