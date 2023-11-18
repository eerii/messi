package utils;

import java.text.SimpleDateFormat;
import java.util.Date;

public class Utils {
    public static String hora() {
        return new SimpleDateFormat("HH:mm:ss").format(new Date());
    }

    public static void log(String msg) {
        System.out.println("[" + hora() + "] " + msg);
    }
}
