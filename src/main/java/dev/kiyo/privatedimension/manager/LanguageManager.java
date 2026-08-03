package dev.kiyo.privatedimension.manager;

import dev.kiyo.privatedimension.PrivateDimensionPlugin;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * config.yml の language 設定（デフォルト "en"）に応じて
 * lang/en.yml または lang/ja.yml を読み込み、文言を提供する。
 *
 * 対応していない language 値が指定された場合は "en" にフォールバックする。
 *
 * lang/*.yml は初回のみ plugins/PrivateDimension/lang/ に展開され、
 * ユーザーが直接編集してカスタマイズすることもできる
 * （config.yml の saveDefaultConfig と同じ考え方）。
 * ユーザーファイルに無いキーは jar 同梱のデフォルト値にフォールバックする。
 */
public class LanguageManager {

    private static final List<String> SUPPORTED = List.of("en", "ja");
    private static final String DEFAULT_LANGUAGE = "en";

    private final PrivateDimensionPlugin plugin;
    private YamlConfiguration lang;
    private String currentLanguage;

    public LanguageManager(PrivateDimensionPlugin plugin) {
        this.plugin = plugin;
        reload();
    }

    /**
     * config.yml から language を再読み込みし、対応する lang ファイルをロードする。
     * plugin.reloadConfig() の後に必ず呼ぶこと。
     */
    public void reload() {
        String configured = plugin.getConfig().getString("language", DEFAULT_LANGUAGE);
        if (configured == null || !SUPPORTED.contains(configured)) {
            plugin.getLogger().warning("[PrivateDimension] language '" + configured
                + "' is not supported (supported: " + SUPPORTED + "). Falling back to '"
                + DEFAULT_LANGUAGE + "'.");
            configured = DEFAULT_LANGUAGE;
        }
        currentLanguage = configured;

        File langDir = new File(plugin.getDataFolder(), "lang");
        if (!langDir.exists()) langDir.mkdirs();

        File file = new File(langDir, currentLanguage + ".yml");
        if (!file.exists()) {
            plugin.saveResource("lang/" + currentLanguage + ".yml", false);
        }

        lang = YamlConfiguration.loadConfiguration(file);

        // jar 同梱のデフォルトをフォールバックとして重ねる
        // （ユーザーがファイルを編集していて、新バージョンで追加されたキーが
        //   無い場合でも動くようにするため）
        try (InputStream defStream = plugin.getResource("lang/" + currentLanguage + ".yml")) {
            if (defStream != null) {
                YamlConfiguration defaults = YamlConfiguration.loadConfiguration(
                    new InputStreamReader(defStream, StandardCharsets.UTF_8));
                lang.setDefaults(defaults);
            }
        } catch (Exception e) {
            plugin.getLogger().warning("[PrivateDimension] lang デフォルト読み込み失敗: " + e.getMessage());
        }
    }

    /** チャット用: '&' を '§' に変換して返す */
    public String get(String path) {
        return colorize(lang.getString(path, path));
    }

    /**
     * チャット用: プレースホルダー付き。
     * 例: get("command.give.player-not-found", "player", "Steve")
     * → "%player%" を "Steve" に置換
     */
    public String get(String path, String... placeholders) {
        String raw = lang.getString(path, path);
        for (int i = 0; i + 1 < placeholders.length; i += 2) {
            raw = raw.replace("%" + placeholders[i] + "%", placeholders[i + 1]);
        }
        return colorize(raw);
    }

    /** アイテムlore等のリスト取得（色変換込み） */
    public List<String> getList(String path) {
        List<String> result = new ArrayList<>();
        for (String s : lang.getStringList(path)) {
            result.add(colorize(s));
        }
        return result;
    }

    /** ファイル書き出し用など、色コード変換をしない生テキストを取得 */
    public String getRaw(String path) {
        return lang.getString(path, "");
    }

    private String colorize(String s) {
        return s == null ? "" : s.replace("&", "§");
    }

    public String getCurrentLanguage() {
        return currentLanguage;
    }
}
