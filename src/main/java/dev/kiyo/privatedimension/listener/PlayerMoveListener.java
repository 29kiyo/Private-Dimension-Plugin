package dev.kiyo.privatedimension.listener;

import dev.kiyo.privatedimension.PrivateDimensionPlugin;
import dev.kiyo.privatedimension.manager.PlayerDataManager;
import dev.kiyo.privatedimension.manager.PlotManager;
import dev.kiyo.privatedimension.util.TeleportHandler;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PlayerMoveListener implements Listener {

    private final PrivateDimensionPlugin plugin;
    private final PlayerDataManager pdm;
    private final PlotManager plotManager;
    private TeleportHandler teleportHandler;

    private final Map<UUID, Long> cooldown = new HashMap<>();
    private static final long COOLDOWN_MS = 2000;

    public PlayerMoveListener(PrivateDimensionPlugin plugin) {
        this.plugin = plugin;
        this.pdm = plugin.getPlayerDataManager();
        this.plotManager = plugin.getPlotManager();
    }

    public void setTeleportHandler(TeleportHandler handler) {
        this.teleportHandler = handler;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerMove(PlayerMoveEvent event) {
        if (event.getFrom().getBlockX() == event.getTo().getBlockX()
            && event.getFrom().getBlockY() == event.getTo().getBlockY()
            && event.getFrom().getBlockZ() == event.getTo().getBlockZ()) return;

        Player player = event.getPlayer();
        if (!plugin.getDimensionManager().isPrivateDimension(player.getWorld())) return;

        boolean debugLogging = plugin.getConfig().getBoolean("debug-logging", false);

        if (player.hasPermission("privatedimension.debug") && player.isOp()) {
            if (debugLogging) plugin.getLogger().info("[PrivateDimension] border-debug: OPバイパスで終了 " + player.getName());
            return;
        }

        String bypassTag = plugin.getConfig().getString("plot-bypass-tag", "pd_free");
        if (bypassTag != null && !bypassTag.isEmpty() && player.getScoreboardTags().contains(bypassTag)) {
            if (debugLogging) plugin.getLogger().info("[PrivateDimension] border-debug: タグバイパスで終了 " + player.getName());
            return;
        }

        UUID uid = player.getUniqueId();
        if (!pdm.hasPlot(uid)) {
            if (debugLogging) plugin.getLogger().info("[PrivateDimension] border-debug: hasPlot=false で終了 " + player.getName());
            return;
        }

        // handleUse によるテレポート処理中は強制送還しない（競合防止）
        if (teleportHandler.isTeleporting(uid)) {
            if (debugLogging) plugin.getLogger().info("[PrivateDimension] border-debug: isTeleporting=true で終了 " + player.getName());
            return;
        }

        int plotId = pdm.getPlotId(uid);
        if (plotManager.isInsidePlot(plotId, event.getTo())) return;

        if (debugLogging) {
            plugin.getLogger().info("[PrivateDimension] border-debug: 境界外を検知 plotId=" + plotId
                + " to=" + event.getTo().getBlockX() + "," + event.getTo().getBlockY() + "," + event.getTo().getBlockZ());
        }

        long now = System.currentTimeMillis();
        Long last = cooldown.get(uid);
        if (last != null && now - last < COOLDOWN_MS) {
            if (debugLogging) plugin.getLogger().info("[PrivateDimension] border-debug: クールダウン中で終了");
            return;
        }
        cooldown.put(uid, now);

        if (!plugin.getConfig().getBoolean("enable-border-enforcement", true)) {
            if (debugLogging) plugin.getLogger().info("[PrivateDimension] border-debug: enable-border-enforcement=false で終了");
            return;
        }

        player.sendMessage(colorize(plugin.getConfig().getString(
            "messages.border-forced", "&c[Private Dimension] プロットの外には出られません！")));

        // MOD版と同じ挙動: 元の世界には出さず、自分のプロットのスポーン地点（安全地点）へ押し戻す
        if (debugLogging) plugin.getLogger().info("[PrivateDimension] border-debug: pushBackToPlot 呼び出し plotId=" + plotId);
        Location pushBackDest = plotManager.findSafeSpawn(plotManager.getPlotSpawn(plotId, player.getWorld()));
        teleportHandler.pushBackToPlot(player, pushBackDest);
    }

    private String colorize(String msg) {
        return msg == null ? "" : msg.replace("&", "§");
    }
}
