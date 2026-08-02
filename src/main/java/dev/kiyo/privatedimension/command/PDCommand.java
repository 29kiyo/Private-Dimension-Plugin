package dev.kiyo.privatedimension.command;

import dev.kiyo.privatedimension.PrivateDimensionPlugin;
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

        if (args.length == 0) {
            sendHelp(sender);
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "give" -> {
                if (!sender.hasPermission("privatedimension.admin")) {
                    sender.sendMessage("§c権限がありません。");
                    return true;
                }
                Player target;
                if (args.length >= 2) {
                    target = plugin.getServer().getPlayer(args[1]);
                    if (target == null) {
                        sender.sendMessage("§cプレイヤーが見つかりません: " + args[1]);
                        return true;
                    }
                } else if (sender instanceof Player p) {
                    target = p;
                } else {
                    sender.sendMessage("§cプレイヤー名を指定してください。");
                    return true;
                }
                target.getInventory().addItem(plugin.getDimensionBottleItem().createItem());
                sender.sendMessage("§a[PrivateDimension] " + target.getName() + " にアイテムを付与しました。");
                if (!target.equals(sender)) {
                    target.sendMessage(colorize(plugin.getConfig().getString("messages.give-item",
                        "&a[Private Dimension] アイテムを付与しました。")));
                }
            }
            case "reload" -> {
                if (!sender.hasPermission("privatedimension.admin")) {
                    sender.sendMessage("§c権限がありません。");
                    return true;
                }
                plugin.reloadConfig();
                plugin.getPlotManager().reload();
                sender.sendMessage("§a[PrivateDimension] 設定をリロードしました。");
            }
            case "info" -> {
                if (!(sender instanceof Player player)) {
                    sender.sendMessage("§cプレイヤーのみ実行可能です。");
                    return true;
                }
                var pdm = plugin.getPlayerDataManager();
                java.util.UUID uid = player.getUniqueId();
                if (pdm.hasPlot(uid)) {
                    int id = pdm.getPlotId(uid);
                    double[] pos = pdm.getPlotPos(uid);
                    player.sendMessage("§b[PrivateDimension] あなたのプロットID: §f" + id);
                    if (pos != null) {
                        player.sendMessage(String.format("§b次元内最終座標: §f%.1f, %.1f, %.1f", pos[0], pos[1], pos[2]));
                    }
                } else {
                    player.sendMessage("§b[PrivateDimension] まだプロットを持っていません。");
                    player.sendMessage("§bDimension in a Bottle を使うと割り当てられます。");
                }
            }
            case "debug" -> {
                if (!sender.hasPermission("privatedimension.debug")) {
                    sender.sendMessage("§c権限がありません。");
                    return true;
                }

                var pm = plugin.getPlotManager();
                String bypassTag = plugin.getConfig().getString("plot-bypass-tag", "pd_free");
                boolean borderEnforcement = plugin.getConfig().getBoolean("enable-border-enforcement", true);

                sender.sendMessage("§e[PrivateDimension] デバッグ情報:");
                sender.sendMessage("§7enable-border-enforcement: §f" + borderEnforcement);
                sender.sendMessage("§7plot-bypass-tag: §f" + bypassTag);
                sender.sendMessage(String.format(
                    "§7plot-size=%d plot-spacing=%d plot-floor-y=%d plot-height=%d",
                    pm.getPlotSize(), pm.getPlotSpacing(), pm.getFloorY(), pm.getPlotHeight()));

                if (sender instanceof Player player) {
                    boolean inDimension = plugin.getDimensionManager().isPrivateDimension(player.getWorld());
                    boolean opBypass = player.hasPermission("privatedimension.debug") && player.isOp();
                    boolean tagBypass = bypassTag != null && !bypassTag.isEmpty()
                        && player.getScoreboardTags().contains(bypassTag);

                    sender.sendMessage("§7次元内にいるか: §f" + inDimension);
                    sender.sendMessage("§7OPによる境界バイパス: §f" + opBypass);
                    sender.sendMessage("§7タグ(" + bypassTag + ")による境界バイパス: §f" + tagBypass);

                    var pdm = plugin.getPlayerDataManager();
                    java.util.UUID uid = player.getUniqueId();
                    if (pdm.hasPlot(uid)) {
                        int plotId = pdm.getPlotId(uid);
                        boolean inside = pm.isInsidePlot(plotId, player.getLocation());
                        sender.sendMessage("§7自分のplotId: §f" + plotId + " §7自分のプロット内か: §f" + inside);
                    } else {
                        sender.sendMessage("§7まだプロットを持っていません。");
                    }
                } else {
                    sender.sendMessage("§7(コンソール実行のためプレイヤー固有の情報はありません)");
                }
            }
            default -> sendHelp(sender);
        }
        return true;
    }

    private void sendHelp(CommandSender sender) {
        sender.sendMessage("§b[PrivateDimension] コマンド一覧:");
        sender.sendMessage("§f/pd give [player] §7- アイテムを付与");
        sender.sendMessage("§f/pd reload §7- 設定をリロード");
        sender.sendMessage("§f/pd info §7- プロット情報を表示");
        sender.sendMessage("§f/pd debug §7- プロット境界チェックの状態を表示");
    }

    private String colorize(String msg) {
        return msg == null ? "" : msg.replace("&", "§");
    }
}
