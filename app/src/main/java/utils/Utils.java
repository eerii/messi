package utils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Utils {
    static DateTimeFormatter fmt = DateTimeFormatter.ofPattern("HH:mm:ss");

    public static String hora() {
        return LocalDateTime.now().format(fmt);
    }

    public static void log(String msg) {
        System.out.println("[" + hora() + "] " + msg);
    }

    public static void debug(String msg) {
        // System.out.println("[" + hora() + "] " + msg);
    }
}
