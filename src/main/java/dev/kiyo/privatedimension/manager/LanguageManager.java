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
 * lang/<language>.yml を読み込み、文言を提供する。
 *
 * 対応言語は "en" / "ja" だけに限定されない。
 * 1. jar に同梱されている言語（現状 en, ja）は自動的に
 *    plugins/PrivateDimension/lang/ に展開される。
 * 2. それ以外の言語コードでも、ユーザーが自分で
 *    plugins/PrivateDimension/lang/<コード>.yml を作成し（lang/en.yml を
 *    コピーして翻訳するのが手軽）、config.yml の language をそのコードに
 *    設定すれば、そのファイルがそのまま読み込まれる。
 * 3. 該当する言語ファイルが（同梱・ユーザー作成どちらにも）存在しない場合は
 *    警告を出して "en" にフォールバックする。
 * 4. どの言語でも、キーが見つからない場合は同梱の en.yml の値に
 *    フォールバックする（カスタム言語ファイルの翻訳漏れ対策）。
 */
public class LanguageManager {

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
        if (configured == null || configured.isBlank()) {
            configured = DEFAULT_LANGUAGE;
        }

        File langDir = new File(plugin.getDataFolder(), "lang");
        if (!langDir.exists()) langDir.mkdirs();

        File file = new File(langDir, configured + ".yml");
        boolean bundled = plugin.getResource("lang/" + configured + ".yml") != null;

        if (!file.exists()) {
            if (bundled) {
                // en / ja など、jar に同梱されている言語 → 自動展開
                plugin.saveResource("lang/" + configured + ".yml", false);
            } else {
                // 同梱もされておらず、ユーザーファイルも存在しないカスタム言語コード
                plugin.getLogger().warning("[PrivateDimension] language '" + configured
                    + "' 用のファイルが見つかりません"
                    + "（jar同梱にも plugins/PrivateDimension/lang/ にもありません）。"
                    + "'" + DEFAULT_LANGUAGE + "' にフォールバックします。"
                    + " カスタム言語を追加するには、"
                    + "lang/en.yml をコピーして plugins/PrivateDimension/lang/" + configured + ".yml"
                    + " を作成し、翻訳してから language を再設定してください。");
                configured = DEFAULT_LANGUAGE;
                file = new File(langDir, configured + ".yml");
                if (!file.exists()) {
                    plugin.saveResource("lang/" + configured + ".yml", false);
                }
            }
        }

        currentLanguage = configured;
        lang = YamlConfiguration.loadConfiguration(file);

        // どの言語であっても、同梱の en.yml を最終フォールバックとして重ねる
        // （カスタム言語ファイルに一部キーが無くても英語で表示され、
        //   生のキー名がそのまま表示されるのを防ぐ）
        try (InputStream defStream = plugin.getResource("lang/" + DEFAULT_LANGUAGE + ".yml")) {
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
