package dev.keiragi.privatedimension.util;

/**
 * 実行時に Paper か Spigot かを判定するユーティリティ
 */
public final class PaperUtil {

    private static final boolean IS_PAPER;

    static {
        boolean paper = false;
        try {
            Class.forName("io.papermc.paper.event.player.AsyncChatEvent");
            paper = true;
        } catch (ClassNotFoundException ignored) {}
        IS_PAPER = paper;
    }

    public static boolean isPaper() {
        return IS_PAPER;
    }

    private PaperUtil() {}
}
