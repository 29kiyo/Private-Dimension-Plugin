package dev.kiyo.privatedimension.listener;

import dev.kiyo.privatedimension.PrivateDimensionPlugin;
import dev.kiyo.privatedimension.manager.PlayerDataManager;
import dev.kiyo.privatedimension.manager.PlotManager;
import dev.kiyo.privatedimension.util.TeleportHandler;
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
        if (player.hasPermission("privatedimension.debug") && player.isOp()) return;

        String bypassTag = plugin.getConfig().getString("plot-bypass-tag", "pd_free");
        if (bypassTag != null && !bypassTag.isEmpty() && player.getScoreboardTags().contains(bypassTag)) return;

        UUID uid = player.getUniqueId();
        if (!pdm.hasPlot(uid)) return;

        // handleUse によるテレポート処理中は強制送還しない（競合防止）
        if (teleportHandler.isTeleporting(uid)) return;

        int plotId = pdm.getPlotId(uid);
        if (plotManager.isInsidePlot(plotId, event.getTo())) return;

        long now = System.currentTimeMillis();
        Long last = cooldown.get(uid);
        if (last != null && now - last < COOLDOWN_MS) return;
        cooldown.put(uid, now);

        if (!plugin.getConfig().getBoolean("enable-border-enforcement", true)) return;

        player.sendMessage(colorize(plugin.getConfig().getString(
            "messages.border-forced", "&c[Private Dimension] プロットの外には出られません！")));

        // MOD版と同じ挙動: 元の世界には出さず、自分のプロットのスポーン地点へ押し戻す
        teleportHandler.pushBackToPlot(player, plotManager.getPlotSpawn(plotId, player.getWorld()));
    }

    private String colorize(String msg) {
        return msg == null ? "" : msg.replace("&", "§");
    }
}
