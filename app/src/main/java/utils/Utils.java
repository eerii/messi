package utils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Utils {
    public static DateTimeFormatter fmt = DateTimeFormatter.ofPattern("HH:mm:ss");

    public static String hora() {
        return LocalDateTime.now().format(fmt);
    }

    public static void log(String msg, Color... color) {
        String fmt = "";
        for (Color col : color)
            fmt += col;
        if (fmt.length() == 0)
            fmt += Color.AMARILLO;
        System.out.format("%s[%s] %s%s%s\n", fmt, hora(), msg, " ".repeat(Math.max(80 - msg.length(), 1)), Color.RESET);
    }

    public static void debug(String msg, Color... color) {
        Color c = color.length > 0 ? color[0] : Color.VERDE;
        log(msg, Color.ITALIC, c);
    }

    public enum Color {
        ROJO("\u001b[31m"),
        VERDE("\u001b[32m"),
        AMARILLO("\u001b[33m"),
        AZUL("\u001b[34m"),
        MAGENTA("\u001b[35m"),
        CYAN("\u001b[36m"),

        RESET("\u001b[0m"),
        BOLD("\u001b[1m"),
        DIM("\u001b[2m"),
        ITALIC("\u001b[3m"),
        UNDERLINE("\u001b[4m"),
        BLINK("\u001b[5m"),
        INVERSE("\u001b[7m"),
        HIDDEN("\u001b[8m"),
        STRIKE("\u001b[9m"),

        NONE("");

        public final String ansi;

        Color(String ansi) {
            this.ansi = ansi;
        }

        @Override
        public String toString() {
            return ansi;
        }
    }
}
