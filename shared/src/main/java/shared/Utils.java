package shared;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Set;
import java.util.ArrayList;

import net.fellbaum.jemoji.Emoji;
import net.fellbaum.jemoji.EmojiManager;

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

    public static String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes)
            sb.append(String.format("%02x", b));
        return sb.toString();
    }

    static final List<Emoji> emojis;
    static {
        Set<Emoji> set = EmojiManager.getAllEmojis();
        emojis = new ArrayList<>(set);
        emojis.sort((a, b) -> a.getUnicode().compareTo(b.getUnicode()));
    }

    public static String emojiFromHex(String hex) {
        StringBuilder sb = new StringBuilder();
        for (int i = 10; i < 30; i += 4) {
            int n = Integer.parseInt(hex.substring(i, i + 4), 16);
            Emoji emoji = emojis.get(n % emojis.size());
            sb.append(emoji.getUnicode() + " ");
        }
        return sb.toString();
    }

    public static String printKey(byte[] key) {
        String hex = bytesToHex(key);
        return hex.substring(0, 8) + "..." + hex.substring(hex.length() - 8);
    }
}
