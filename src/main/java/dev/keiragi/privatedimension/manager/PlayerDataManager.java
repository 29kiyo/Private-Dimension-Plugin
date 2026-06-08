package dev.keiragi.privatedimension.manager;

import dev.keiragi.privatedimension.PrivateDimensionPlugin;
import org.bukkit.Location;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * プレイヤーごとのデータを管理する
 * - 割り当てられたプロットID
 * - 次元内での最後の座標
 * - 元世界の戻り座標
 */
public class PlayerDataManager {

    private final PrivateDimensionPlugin plugin;
    private final File dataFile;
    private YamlConfiguration data;

    // キャッシュ
    private final Map<UUID, Integer> plotIdCache = new HashMap<>();
    private final Map<UUID, double[]> plotPosCache = new HashMap<>(); // 次元内座標
    private final Map<UUID, Location> returnLocCache = new HashMap<>(); // 戻り座標

    public PlayerDataManager(PrivateDimensionPlugin plugin) {
        this.plugin = plugin;
        this.dataFile = new File(plugin.getDataFolder(), "playerdata.yml");
        load();
    }

    private void load() {
        if (!dataFile.exists()) {
            dataFile.getParentFile().mkdirs();
        }
        data = YamlConfiguration.loadConfiguration(dataFile);
    }

    public void saveAll() {
        // キャッシュをファイルに書き出し
        for (Map.Entry<UUID, Integer> entry : plotIdCache.entrySet()) {
            data.set("players." + entry.getKey() + ".plotId", entry.getValue());
        }
        for (Map.Entry<UUID, double[]> entry : plotPosCache.entrySet()) {
            double[] pos = entry.getValue();
            String base = "players." + entry.getKey() + ".plotPos.";
            data.set(base + "x", pos[0]);
            data.set(base + "y", pos[1]);
            data.set(base + "z", pos[2]);
        }
        for (Map.Entry<UUID, Location> entry : returnLocCache.entrySet()) {
            Location loc = entry.getValue();
            if (loc.getWorld() == null) continue;
            String base = "players." + entry.getKey() + ".returnLoc.";
            data.set(base + "world", loc.getWorld().getName());
            data.set(base + "x", loc.getX());
            data.set(base + "y", loc.getY());
            data.set(base + "z", loc.getZ());
            data.set(base + "yaw", loc.getYaw());
            data.set(base + "pitch", loc.getPitch());
        }
        try {
            data.save(dataFile);
        } catch (IOException e) {
            plugin.getLogger().severe("playerdata.yml の保存に失敗: " + e.getMessage());
        }
    }

    // ── プロットID ──

    public boolean hasPlot(UUID uuid) {
        if (plotIdCache.containsKey(uuid)) return plotIdCache.get(uuid) >= 0;
        int id = data.getInt("players." + uuid + ".plotId", -1);
        if (id >= 0) plotIdCache.put(uuid, id);
        return id >= 0;
    }

    public int getPlotId(UUID uuid) {
        if (plotIdCache.containsKey(uuid)) return plotIdCache.get(uuid);
        int id = data.getInt("players." + uuid + ".plotId", -1);
        plotIdCache.put(uuid, id);
        return id;
    }

    public void setPlotId(UUID uuid, int plotId) {
        plotIdCache.put(uuid, plotId);
        data.set("players." + uuid + ".plotId", plotId);
        trySave();
    }

    public int getNextPlotId() {
        int next = data.getInt("meta.nextPlotId", 0);
        data.set("meta.nextPlotId", next + 1);
        trySave();
        return next;
    }

    // ── 次元内の最後の座標 ──

    public double[] getPlotPos(UUID uuid) {
        if (plotPosCache.containsKey(uuid)) return plotPosCache.get(uuid);
        String base = "players." + uuid + ".plotPos.";
        if (!data.contains(base + "x")) return null;
        double[] pos = new double[]{
            data.getDouble(base + "x"),
            data.getDouble(base + "y"),
            data.getDouble(base + "z")
        };
        plotPosCache.put(uuid, pos);
        return pos;
    }

    public void setPlotPos(UUID uuid, double x, double y, double z) {
        plotPosCache.put(uuid, new double[]{x, y, z});
        // 即座に保存せずキャッシュに留める（onDisableで保存）
    }

    // ── 戻り座標 ──

    public Location getReturnLocation(UUID uuid) {
        if (returnLocCache.containsKey(uuid)) return returnLocCache.get(uuid);
        String base = "players." + uuid + ".returnLoc.";
        if (!data.contains(base + "world")) return null;
        String worldName = data.getString(base + "world");
        org.bukkit.World world = plugin.getServer().getWorld(worldName);
        if (world == null) return null;
        Location loc = new Location(
            world,
            data.getDouble(base + "x"),
            data.getDouble(base + "y"),
            data.getDouble(base + "z"),
            (float) data.getDouble(base + "yaw"),
            (float) data.getDouble(base + "pitch")
        );
        returnLocCache.put(uuid, loc);
        return loc;
    }

    public void setReturnLocation(UUID uuid, Location loc) {
        returnLocCache.put(uuid, loc);
    }

    public void clearReturnLocation(UUID uuid) {
        returnLocCache.remove(uuid);
    }

    private void trySave() {
        try {
            data.save(dataFile);
        } catch (IOException e) {
            plugin.getLogger().warning("playerdata.yml の保存に失敗: " + e.getMessage());
        }
    }
}
