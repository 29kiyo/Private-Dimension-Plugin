package dev.kiyo.privatedimension.listener;

import dev.kiyo.privatedimension.PrivateDimensionPlugin;
import dev.kiyo.privatedimension.manager.PlayerDataManager;
import dev.kiyo.privatedimension.manager.PlotManager;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerRespawnEvent;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * 次元内で死亡した場合の処理
 *
 * ── 設計 ──
 * onPlayerDeath  : 次元内死亡フラグを立てる（plotPos は更新しない）
 * onPlayerRespawn: フラグが立っているときだけリスポーン先を元の世界に変更し、
 *                  plotPos をプロットスポーンにリセットする
 *
 * returnLoc は "入室時に記録した元の世界座標" であり死亡とは無関係。
 * 死亡判定には別フラグ（diedInDimension）を使う。
 */
public class PlayerDeathListener implements Listener {

    private final PrivateDimensionPlugin plugin;
    private final PlayerDataManager pdm;
    private final PlotManager plotManager;

    // 次元内で死亡したプレイヤーのUUID（リスポーンイベントまで保持）
    private final Set<UUID> diedInDimension = new HashSet<>();

    public PlayerDeathListener(PrivateDimensionPlugin plugin) {
        this.plugin = plugin;
        this.pdm = plugin.getPlayerDataManager();
        this.plotManager = plugin.getPlotManager();
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        if (!plugin.getDimensionManager().isPrivateDimension(player.getWorld())) return;

        // 次元内死亡フラグを立てる
        // plotPos はここで更新しない（死亡座標は危険な場所の可能性があるため）
        diedInDimension.add(player.getUniqueId());
    }

    @EventHandler
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        Player player = event.getPlayer();
        UUID uid = player.getUniqueId();

        // 次元内死亡フラグがないなら無視
        if (!diedInDimension.remove(uid)) return;

        Location returnLoc = pdm.getReturnLocation(uid);
        if (returnLoc == null || returnLoc.getWorld() == null) {
            returnLoc = plugin.getServer().getWorlds().get(0).getSpawnLocation();
        }

        // リスポーン先を元の世界に設定
        event.setRespawnLocation(returnLoc);
        pdm.clearReturnLocation(uid);

        // plotPos を次回入室時のスポーン地点（プロット中央）にリセット
        // これにより死亡座標ではなく安全なスポーン地点から再入室できる
        if (pdm.hasPlot(uid)) {
            int plotId = pdm.getPlotId(uid);
            Location safeSpawn = plotManager.getPlotSpawn(plotId,
                plugin.getDimensionManager().getPrivateDimension());
            if (safeSpawn != null) {
                pdm.setPlotPos(uid, safeSpawn.getX(), safeSpawn.getY(), safeSpawn.getZ());
            }
        }
    }
}
