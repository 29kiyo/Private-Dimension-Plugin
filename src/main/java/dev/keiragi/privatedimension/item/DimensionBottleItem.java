package dev.keiragi.privatedimension.item;

import dev.keiragi.privatedimension.PrivateDimensionPlugin;
import dev.keiragi.privatedimension.util.PaperUtil;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.Arrays;

/**
 * "Dimension in a Bottle" アイテム
 *
 * displayName(Component) / lore(List<Component>) は spigot-api に存在しないため
 * リフレクションで呼び出す。spigot-api でコンパイルしつつ
 * Paper 実行時のみ Adventure メソッドを使う。
 */
public class DimensionBottleItem {

    public static final String ITEM_ID = "dimension_in_a_bottle";

    private final PrivateDimensionPlugin plugin;
    private final NamespacedKey itemKey;
    private final NamespacedKey recipeKey;

    public DimensionBottleItem(PrivateDimensionPlugin plugin) {
        this.plugin    = plugin;
        this.itemKey   = new NamespacedKey(plugin, "item_id");
        this.recipeKey = new NamespacedKey(plugin, "dimension_in_a_bottle");
        registerRecipe();
    }

    public ItemStack createItem() {
        ItemStack item = new ItemStack(Material.LINGERING_POTION);
        PotionMeta meta = (PotionMeta) item.getItemMeta();

        meta.setColor(Color.fromRGB(0x40, 0xBF, 0xFF));
        meta.getPersistentDataContainer().set(itemKey, PersistentDataType.STRING, ITEM_ID);

        if (PaperUtil.isPaper()) {
            applyPaperMeta(meta);
        } else {
            // Spigot: legacy カラーコード
            meta.setDisplayName("§bDimension in a Bottle");
            meta.setLore(Arrays.asList(
                "",
                "§f使用すると、別世界の空間へと移動する。",
                "§f その世界で再び使用すると、元の世界へと戻る。",
                "",
                "§7\"この小さな丸い瓶の中には、",
                "§7 どういうわけか別の世界が詰まっている\""
            ));
        }

        item.setItemMeta(meta);
        return item;
    }

    /**
     * Paper 専用メソッド（displayName/lore/setEnchantmentGlintOverride）を
     * リフレクションで呼び出す。
     * spigot-api でコンパイルしているためこれらはコンパイル時に解決できないが、
     * Paper 実行時には確実に存在する。
     */
    private void applyPaperMeta(PotionMeta meta) {
        try {
            // net.kyori.adventure.text.Component を取得
            Class<?> componentClass = Class.forName("net.kyori.adventure.text.Component");
            Class<?> textColorClass = Class.forName("net.kyori.adventure.text.format.NamedTextColor");
            Class<?> decorationClass = Class.forName("net.kyori.adventure.text.format.TextDecoration");

            // NamedTextColor.AQUA / WHITE / GRAY
            Object aqua  = textColorClass.getField("AQUA").get(null);
            Object white = textColorClass.getField("WHITE").get(null);
            Object gray  = textColorClass.getField("GRAY").get(null);

            // TextDecoration.ITALIC
            Object italic = decorationClass.getField("ITALIC").get(null);

            // Component.text(String) → .color(color) → .decoration(deco, false)
            java.lang.reflect.Method textMethod      = componentClass.getMethod("text", String.class);
            java.lang.reflect.Method colorMethod     = componentClass.getMethod("color",
                Class.forName("net.kyori.adventure.text.format.TextColor"));
            java.lang.reflect.Method decoMethod      = componentClass.getMethod("decoration",
                decorationClass, boolean.class);
            java.lang.reflect.Method emptyMethod     = componentClass.getMethod("empty");

            // タイトル: "Dimension in a Bottle" (AQUA, italic=false)
            Object title = decoMethod.invoke(
                colorMethod.invoke(textMethod.invoke(null, "Dimension in a Bottle"), aqua),
                italic, false);

            // ロア行
            Object l1 = emptyMethod.invoke(null);
            Object l2 = decoMethod.invoke(colorMethod.invoke(
                textMethod.invoke(null, "使用すると、別世界の空間へと移動する。"), white), italic, false);
            Object l3 = decoMethod.invoke(colorMethod.invoke(
                textMethod.invoke(null, "その世界で再び使用すると、元の世界へと戻る。"), white), italic, false);
            Object l4 = emptyMethod.invoke(null);
            Object l5 = decoMethod.invoke(colorMethod.invoke(
                textMethod.invoke(null, "\"この小さな丸い瓶の中には、"), gray), italic, false);
            Object l6 = decoMethod.invoke(colorMethod.invoke(
                textMethod.invoke(null, " どういうわけか別の世界が詰まっている\""), gray), italic, false);

            // ItemMeta.displayName(Component)
            meta.getClass().getMethod("displayName", componentClass).invoke(meta, title);

            // ItemMeta.lore(List<Component>)
            meta.getClass().getMethod("lore", java.util.List.class)
                .invoke(meta, Arrays.asList(l1, l2, l3, l4, l5, l6));

            // ItemMeta.setEnchantmentGlintOverride(Boolean)
            try {
                meta.getClass().getMethod("setEnchantmentGlintOverride", Boolean.class)
                    .invoke(meta, true);
            } catch (NoSuchMethodException ignored) {
                // 古い Paper バージョンでは存在しない場合がある
            }

        } catch (Exception e) {
            // Paper メソッドの呼び出し失敗時は legacy にフォールバック
            plugin.getLogger().warning("Adventure API の適用に失敗、legacy フォールバック: " + e.getMessage());
            meta.setDisplayName("§bDimension in a Bottle");
            meta.setLore(Arrays.asList(
                "",
                "§f使用すると、別世界の空間へと移動する。",
                "§f その世界で再び使用すると、元の世界へと戻る。",
                "",
                "§7\"この小さな丸い瓶の中には、",
                "§7 どういうわけか別の世界が詰まっている\""
            ));
        }
    }

    public boolean isDimensionBottle(ItemStack item) {
        if (item == null || item.getType() != Material.LINGERING_POTION) return false;
        if (!item.hasItemMeta()) return false;
        String val = item.getItemMeta().getPersistentDataContainer()
            .get(itemKey, PersistentDataType.STRING);
        return ITEM_ID.equals(val);
    }

    private void registerRecipe() {
        plugin.getServer().removeRecipe(recipeKey);

        ShapedRecipe recipe = new ShapedRecipe(recipeKey, createItem());
        recipe.shape("#E#", "#D#", "#L#");
        recipe.setIngredient('#', Material.GLASS);
        recipe.setIngredient('E', Material.ENDER_EYE);
        recipe.setIngredient('D', Material.DIAMOND);
        recipe.setIngredient('L', Material.LODESTONE);

        plugin.getServer().addRecipe(recipe);
    }

    public NamespacedKey getItemKey()   { return itemKey; }
    public NamespacedKey getRecipeKey() { return recipeKey; }
}
