package dev.kiyo.privatedimension.item;

import dev.kiyo.privatedimension.PrivateDimensionPlugin;
import dev.kiyo.privatedimension.util.PaperUtil;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.tag.DamageTypeTags;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.Arrays;

/**
 * "Dimension in a Bottle" アイテム
 *
 * ── アイテム耐性 ──
 * setDamageResistant() は1つの Tag しか設定できない制約があるため、
 * 炎・溶岩（IS_FIRE）のみここで設定する。
 * サボテン・爆発は ItemEntityListener でドロップアイテムの
 * ダメージイベントをキャンセルすることで対応する。
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

        // 炎・溶岩耐性（IS_FIRE タグ）
        try {
            meta.setDamageResistant(DamageTypeTags.IS_FIRE);
        } catch (Exception e) {
            plugin.getLogger().warning("damage_resistant(IS_FIRE) 設定失敗: " + e.getMessage());
        }

        if (PaperUtil.isPaper()) {
            applyPaperMeta(meta);
        } else {
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

    private void applyPaperMeta(PotionMeta meta) {
        try {
            Class<?> componentClass  = Class.forName("net.kyori.adventure.text.Component");
            Class<?> textColorClass  = Class.forName("net.kyori.adventure.text.format.NamedTextColor");
            Class<?> decorationClass = Class.forName("net.kyori.adventure.text.format.TextDecoration");

            Object aqua   = textColorClass.getField("AQUA").get(null);
            Object white  = textColorClass.getField("WHITE").get(null);
            Object gray   = textColorClass.getField("GRAY").get(null);
            Object italic = decorationClass.getField("ITALIC").get(null);

            java.lang.reflect.Method textMethod  = componentClass.getMethod("text", String.class);
            java.lang.reflect.Method colorMethod = componentClass.getMethod("color",
                Class.forName("net.kyori.adventure.text.format.TextColor"));
            java.lang.reflect.Method decoMethod  = componentClass.getMethod("decoration",
                decorationClass, boolean.class);
            java.lang.reflect.Method emptyMethod = componentClass.getMethod("empty");

            Object title = decoMethod.invoke(
                colorMethod.invoke(textMethod.invoke(null, "Dimension in a Bottle"), aqua),
                italic, false);

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

            meta.getClass().getMethod("displayName", componentClass).invoke(meta, title);
            meta.getClass().getMethod("lore", java.util.List.class)
                .invoke(meta, Arrays.asList(l1, l2, l3, l4, l5, l6));

            try {
                meta.getClass().getMethod("setEnchantmentGlintOverride", Boolean.class)
                    .invoke(meta, true);
            } catch (NoSuchMethodException ignored) {}

        } catch (Exception e) {
            plugin.getLogger().warning("Adventure API 適用失敗、legacy フォールバック: " + e.getMessage());
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
