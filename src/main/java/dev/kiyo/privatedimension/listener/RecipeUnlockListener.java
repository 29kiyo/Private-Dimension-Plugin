package dev.kiyo.privatedimension.listener;

import dev.kiyo.privatedimension.PrivateDimensionPlugin;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import java.util.Collections;

/**
 * プレイヤーがサーバーに参加した際、Dimension in a Bottle のレシピを
 * 自動的に「発見済み」にする（クラフトブックに表示され、既知の状態になる）。
 *
 * 通常のMinecraftではレシピは条件を満たして初めて発見扱いになるが、
 * このプラグインの場合は最初から使えるようにしたいため参加時に付与する。
 */
public class RecipeUnlockListener implements Listener {

    private final PrivateDimensionPlugin plugin;

    public RecipeUnlockListener(PrivateDimensionPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        player.discoverRecipes(Collections.singletonList(
            plugin.getDimensionBottleItem().getRecipeKey()));
    }
}
