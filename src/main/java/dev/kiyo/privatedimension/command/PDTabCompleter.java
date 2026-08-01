package dev.kiyo.privatedimension.command;

import dev.kiyo.privatedimension.PrivateDimensionPlugin;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * /pd, /privatedim のタブ補完
 *
 *  /pd <give|reload|info|debug>
 *  /pd give <player>   ← オンラインプレイヤー名を補完
 *
 * 権限を持たないサブコマンドは候補から除外する。
 */
public class PDTabCompleter implements TabCompleter {

    private final PrivateDimensionPlugin plugin;

    public PDTabCompleter(PrivateDimensionPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            List<String> subCommands = new ArrayList<>();
            subCommands.add("info");
            if (sender.hasPermission("privatedimension.admin")) {
                subCommands.add("give");
                subCommands.add("reload");
            }
            if (sender.hasPermission("privatedimension.debug")) {
                subCommands.add("debug");
            }
            return filter(subCommands, args[0]);
        }

        if (args.length == 2 && args[0].equalsIgnoreCase("give")) {
            if (!sender.hasPermission("privatedimension.admin")) {
                return Collections.emptyList();
            }
            List<String> names = new ArrayList<>();
            for (Player p : plugin.getServer().getOnlinePlayers()) {
                names.add(p.getName());
            }
            return filter(names, args[1]);
        }

        return Collections.emptyList();
    }

    private List<String> filter(List<String> candidates, String token) {
        List<String> result = new ArrayList<>();
        StringUtil.copyPartialMatches(token, candidates, result);
        Collections.sort(result);
        return result;
    }
}
