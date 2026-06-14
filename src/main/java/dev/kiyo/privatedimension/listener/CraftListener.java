package dev.kiyo.privatedimension.listener;

import dev.kiyo.privatedimension.PrivateDimensionPlugin;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.inventory.ItemStack;

/**
 * クラフト時のエフェクト再生
 */
public class CraftListener implements Listener {

    private final PrivateDimensionPlugin plugin;

    public CraftListener(PrivateDimensionPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onCraft(CraftItemEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;

        // getCurrentItem() は null の可能性があるため null チェック必須
        ItemStack result = event.getCurrentItem();
        if (result == null) return;
        if (!plugin.getDimensionBottleItem().isDimensionBottle(result)) return;

        player.playSound(player.getLocation(), Sound.ENTITY_ENDER_EYE_DEATH, 1f, 1f);
        player.playSound(player.getLocation(), Sound.ITEM_BOTTLE_FILL_DRAGONBREATH, 1f, 2f);

        player.sendMessage(colorize(plugin.getConfig().getString("messages.craft-success",
            "&a[Private Dimension] Dimension in a Bottle をクラフトしました！")));
    }

    private String colorize(String msg) {
        return msg == null ? "" : msg.replace("&", "§");
    }
}
