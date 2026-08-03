package dev.kiyo.privatedimension.item;

import dev.kiyo.privatedimension.PrivateDimensionPlugin;
import dev.kiyo.privatedimension.manager.LanguageManager;
import dev.kiyo.privatedimension.util.PaperUtil;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.tag.DamageTypeTags;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.List;

/**
 * "Dimension in a Bottle" アイテム
 *
 * 表示名・説明文（lore）は LanguageManager（config.yml の language 設定）から取得する。
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

        // エンチャント風の光沢（実際の効果は付与しない見た目だけの演出）
        // Paper/Spigot どちらでも確実に効く方法: 隠しエンチャントを付けて表示だけ隠す
        try {
            meta.addEnchant(org.bukkit.enchantments.Enchantment.LURE, 1, true);
            meta.addItemFlags(org.bukkit.inventory.ItemFlag.HIDE_ENCHANTS);
        } catch (Exception e) {
            plugin.getLogger().warning("エンチャント光沢の付与に失敗: " + e.getMessage());
        }

        // 炎・溶岩耐性（IS_FIRE タグ）
        try {
            meta.setDamageResistant(DamageTypeTags.IS_FIRE);
        } catch (Exception e) {
            plugin.getLogger().warning("damage_resistant(IS_FIRE) 設定失敗: " + e.getMessage());
        }

        LanguageManager lang = plugin.getLanguageManager();

        if (PaperUtil.isPaper()) {
            applyPaperMeta(meta, lang);
        } else {
            meta.setDisplayName(lang.get("item.name"));
            meta.setLore(lang.getList("item.lore"));
        }

        item.setItemMeta(meta);
        return item;
    }

    /**
     * Paper環境向け: Adventure Component経由で表示名・loreを設定する。
     * LanguageManager が返す「§」付きレガシー文字列を LegacyComponentSerializer で
     * Componentへ変換し、italic装飾を明示的にOFFにする
     * （legacy文字列で直接setLoreすると自動でイタリック体になってしまうのを防ぐため）。
     * 失敗した場合は legacy な setDisplayName/setLore にフォールバックする。
     */
    private void applyPaperMeta(PotionMeta meta, LanguageManager lang) {
        try {
            Class<?> componentClass = Class.forName("net.kyori.adventure.text.Component");
            Class<?> decorationClass = Class.forName("net.kyori.adventure.text.format.TextDecoration");
            Class<?> legacySerializerClass =
                Class.forName("net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer");

            Object legacySection = legacySerializerClass.getMethod("legacySection").invoke(null);
            java.lang.reflect.Method deserializeMethod =
                legacySerializerClass.getMethod("deserialize", String.class);

            Object italic = decorationClass.getField("ITALIC").get(null);
            java.lang.reflect.Method decoMethod =
                componentClass.getMethod("decoration", decorationClass, boolean.class);

            Object nameComponent = deserializeMethod.invoke(legacySection, lang.get("item.name"));
            nameComponent = decoMethod.invoke(nameComponent, italic, false);

            List<Object> loreComponents = new ArrayList<>();
            for (String line : lang.getList("item.lore")) {
                Object lineComponent = deserializeMethod.invoke(legacySection, line);
                lineComponent = decoMethod.invoke(lineComponent, italic, false);
                loreComponents.add(lineComponent);
            }

            meta.getClass().getMethod("displayName", componentClass).invoke(meta, nameComponent);
            meta.getClass().getMethod("lore", List.class).invoke(meta, loreComponents);

            try {
                meta.getClass().getMethod("setEnchantmentGlintOverride", Boolean.class)
                    .invoke(meta, true);
            } catch (NoSuchMethodException ignored) {}

        } catch (Exception e) {
            plugin.getLogger().warning("Adventure API 適用失敗、legacy フォールバック: " + e.getMessage());
            meta.setDisplayName(lang.get("item.name"));
            meta.setLore(lang.getList("item.lore"));
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
