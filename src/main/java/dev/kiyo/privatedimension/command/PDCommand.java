package dev.kiyo.privatedimension.command;

import dev.kiyo.privatedimension.PrivateDimensionPlugin;
import dev.kiyo.privatedimension.manager.LanguageManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
/**
 * /pd コマンド
 *  /pd give [player]  - アイテムを付与
 *  /pd reload         - 設定リロード
 *  /pd info           - 自分のプロット情報
 *  /pd debug          - デバッグ情報を表示（op限定）
 *
 * 表示文言はすべて LanguageManager（config.yml の language 設定）経由。
 */
public class PDCommand implements CommandExecutor {

    private final PrivateDimensionPlugin plugin;

    public PDCommand(PrivateDimensionPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender,
                             Command command,
                             String label,
                             String[] args) {

        LanguageManager lang = plugin.getLanguageManager();

        if (args.length == 0) {
            sendHelp(sender, lang);
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "give" -> {
                if (!sender.hasPermission("privatedimension.admin")) {
                    sender.sendMessage(lang.get("command.no-permission"));
                    return true;
                }
                Player target;
                if (args.length >= 2) {
                    target = plugin.getServer().getPlayer(args[1]);
                    if (target == null) {
                        sender.sendMessage(lang.get("command.give.player-not-found", "player", args[1]));
                        return true;
                    }
                } else if (sender instanceof Player p) {
                    target = p;
                } else {
                    sender.sendMessage(lang.get("command.give.need-player-name"));
                    return true;
                }
                target.getInventory().addItem(plugin.getDimensionBottleItem().createItem());
                sender.sendMessage(lang.get("command.give.success-sender", "player", target.getName()));
                if (!target.equals(sender)) {
                    target.sendMessage(lang.get("messages.give-item"));
                }
            }
            case "reload" -> {
                if (!sender.hasPermission("privatedimension.admin")) {
                    sender.sendMessage(lang.get("command.no-permission"));
                    return true;
                }
                plugin.reloadConfig();
                plugin.getPlotManager().reload();
                lang.reload();
                sender.sendMessage(lang.get("command.reload.success"));
            }
            case "info" -> {
                if (!(sender instanceof Player player)) {
                    sender.sendMessage(lang.get("command.info.players-only"));
                    return true;
                }
                var pdm = plugin.getPlayerDataManager();
                java.util.UUID uid = player.getUniqueId();
                if (pdm.hasPlot(uid)) {
                    int id = pdm.getPlotId(uid);
                    double[] pos = pdm.getPlotPos(uid);
                    player.sendMessage(lang.get("command.info.plot-id", "id", String.valueOf(id)));
                    if (pos != null) {
                        player.sendMessage(lang.get("command.info.last-position",
                            "x", String.format("%.1f", pos[0]),
                            "y", String.format("%.1f", pos[1]),
                            "z", String.format("%.1f", pos[2])));
                    }
                } else {
                    player.sendMessage(lang.get("command.info.no-plot-line1"));
                    player.sendMessage(lang.get("command.info.no-plot-line2"));
                }
            }
            case "debug" -> {
                if (!sender.hasPermission("privatedimension.debug")) {
                    sender.sendMessage(lang.get("command.no-permission"));
                    return true;
                }

                var pm = plugin.getPlotManager();
                String bypassTag = plugin.getConfig().getString("plot-bypass-tag", "pd_free");
                boolean borderEnforcement = plugin.getConfig().getBoolean("enable-border-enforcement", true);

                sender.sendMessage(lang.get("command.debug.header"));
                sender.sendMessage(lang.get("command.debug.border-enforcement", "value", String.valueOf(borderEnforcement)));
                sender.sendMessage(lang.get("command.debug.bypass-tag-config", "value", String.valueOf(bypassTag)));
                sender.sendMessage(lang.get("command.debug.plot-values",
                    "size", String.valueOf(pm.getPlotSize()),
                    "spacing", String.valueOf(pm.getPlotSpacing()),
                    "floorY", String.valueOf(pm.getFloorY()),
                    "height", String.valueOf(pm.getPlotHeight())));

                if (sender instanceof Player player) {
                    boolean inDimension = plugin.getDimensionManager().isPrivateDimension(player.getWorld());
                    boolean opBypass = player.hasPermission("privatedimension.debug") && player.isOp();
                    boolean tagBypass = bypassTag != null && !bypassTag.isEmpty()
                        && player.getScoreboardTags().contains(bypassTag);

                    sender.sendMessage(lang.get("command.debug.in-dimension", "value", String.valueOf(inDimension)));
                    sender.sendMessage(lang.get("command.debug.op-bypass", "value", String.valueOf(opBypass)));
                    sender.sendMessage(lang.get("command.debug.tag-bypass",
                        "tag", String.valueOf(bypassTag), "value", String.valueOf(tagBypass)));

                    var pdm = plugin.getPlayerDataManager();
                    java.util.UUID uid = player.getUniqueId();
                    if (pdm.hasPlot(uid)) {
                        int plotId = pdm.getPlotId(uid);
                        boolean inside = pm.isInsidePlot(plotId, player.getLocation());
                        sender.sendMessage(lang.get("command.debug.plot-status",
                            "id", String.valueOf(plotId), "inside", String.valueOf(inside)));
                    } else {
                        sender.sendMessage(lang.get("command.debug.no-plot"));
                    }
                } else {
                    sender.sendMessage(lang.get("command.debug.console-note"));
                }
            }
            default -> sendHelp(sender, lang);
        }
        return true;
    }

    private void sendHelp(CommandSender sender, LanguageManager lang) {
        sender.sendMessage(lang.get("command.help.header"));
        sender.sendMessage(lang.get("command.help.give"));
        sender.sendMessage(lang.get("command.help.reload"));
        sender.sendMessage(lang.get("command.help.info"));
        sender.sendMessage(lang.get("command.help.debug"));
    }
}
