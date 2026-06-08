package dev.keiragi.privatedimension.command;

import dev.keiragi.privatedimension.PrivateDimensionPlugin;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
/**
 * /pd コマンド
 *  /pd give [player]  - アイテムを付与
 *  /pd reload         - 設定リロード
 *  /pd info           - 自分のプロット情報
 *  /pd debug          - プロット境界を無視する（op限定）
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
                sender.sendMessage("§e[PrivateDimension] デバッグ: プロット境界チェックはop権限で無効化されます。");
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
    }

    private String colorize(String msg) {
        return msg == null ? "" : msg.replace("&", "§");
    }
}
