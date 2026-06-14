package dev.kiyo.privatedimension.util;

import dev.kiyo.privatedimension.PrivateDimensionPlugin;
import dev.kiyo.privatedimension.dimension.DimensionManager;
import dev.kiyo.privatedimension.manager.PlayerDataManager;
import dev.kiyo.privatedimension.manager.PlotManager;
import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

public class TeleportHandler {

    private final PrivateDimensionPlugin plugin;
    private final DimensionManager dim;
    private final PlotManager plotManager;
    private final PlayerDataManager pdm;

    // テレポート処理中プレイヤー（二重実行防止 + PlayerMoveListener の誤判定防止）
    // teleport() 完了後も1tick維持することで、同tick内の PlayerMoveEvent の誤介入を防ぐ
    private final Set<UUID> teleporting = Collections.synchronizedSet(new HashSet<>());

    public TeleportHandler(PrivateDimensionPlugin plugin) {
        this.plugin = plugin;
        this.dim = plugin.getDimensionManager();
        this.plotManager = plugin.getPlotManager();
        this.pdm = plugin.getPlayerDataManager();
    }

    public void handleUse(Player player) {
        UUID uid = player.getUniqueId();
        if (!teleporting.add(uid)) return;

        World privateDim = dim.getPrivateDimension();
        if (privateDim == null) {
            releaseNextTick(uid);
            player.sendMessage("§c[PrivateDimension] 次元ワールドが準備できていません。");
            return;
        }

        try {
            if (dim.isPrivateDimension(player.getWorld())) {
                // 帰還時: gotoBaseWorld 内で現在地（次元内）の周囲エンティティを収集
                gotoBaseWorld(player);
            } else {
                // 往路時: 元の世界の周囲エンティティを収集して連れていく
                gotoPrivate(player, collectBringEntities(player));
            }
        } catch (Exception e) {
            releaseNextTick(uid);
            plugin.getLogger().severe("handleUse で例外: " + e.getMessage());
        }
    }

    // ──────────────────────────────────────────────
    // 元の世界 → プライベート次元
    // ──────────────────────────────────────────────

    private void gotoPrivate(Player player, List<Entity> bringEntities) {
        pdm.setReturnLocation(player.getUniqueId(), player.getLocation().clone());
        playVfx(player.getLocation());
        player.addPotionEffect(new org.bukkit.potion.PotionEffect(
            org.bukkit.potion.PotionEffectType.BLINDNESS, 40, 0, true, false));

        if (pdm.hasPlot(player.getUniqueId())) {
            gotoMyPlot(player, bringEntities);
        } else {
            claimPlot(player, bringEntities);
        }
    }

    private void gotoMyPlot(Player player, List<Entity> bringEntities) {
        UUID uid = player.getUniqueId();
        World pdWorld = dim.getPrivateDimension();

        double[] saved = pdm.getPlotPos(uid);
        Location dest = (saved != null)
            ? new Location(pdWorld, saved[0], saved[1], saved[2])
            : plotManager.getPlotSpawn(pdm.getPlotId(uid), pdWorld);

        try {
            player.teleport(dest);
            pullEntities(dest, bringEntities);
            playVfx(dest);
        } catch (Exception e) {
            plugin.getLogger().severe("gotoMyPlot で例外: " + e.getMessage());
        } finally {
            // teleport() 直後に PlayerMoveEvent が同tick内で誤発火するのを防ぐため
            // releaseNextTick で1tick後に teleporting を解放する
            releaseNextTick(uid);
        }
    }

    private void claimPlot(Player player, List<Entity> bringEntities) {
        UUID uid = player.getUniqueId();
        int plotId = pdm.getNextPlotId();
        pdm.setPlotId(uid, plotId);

        World pdWorld = dim.getPrivateDimension();
        Location structOrigin = plotManager.getPlotStructureOrigin(plotId, pdWorld);
        Location spawnLoc     = plotManager.getPlotSpawn(plotId, pdWorld);

        // BukkitRunnable は次tick以降に実行されるため、
        // 構造物配置・teleport 完了後に releaseNextTick を呼ぶ
        new BukkitRunnable() {
            @Override
            public void run() {
                try {
                    spawnLoc.getChunk().load(true);
                    structOrigin.getChunk().load(true);
                    dim.placeStructure(structOrigin);

                    player.addPotionEffect(new org.bukkit.potion.PotionEffect(
                        org.bukkit.potion.PotionEffectType.SLOW_FALLING, 20, 0, true, false));

                    player.teleport(spawnLoc);
                    pdm.setPlotPos(uid, spawnLoc.getX(), spawnLoc.getY(), spawnLoc.getZ());
                    pullEntities(spawnLoc, bringEntities);
                    playVfx(spawnLoc);
                } catch (Exception e) {
                    plugin.getLogger().severe("claimPlot で例外: " + e.getMessage());
                } finally {
                    releaseNextTick(uid);
                }
            }
        }.runTask(plugin);
    }

    // ──────────────────────────────────────────────
    // プライベート次元 → 元の世界
    // ──────────────────────────────────────────────

    /**
     * プライベート次元 → 元の世界
     * handleUse 経由と PlayerMoveListener 経由の両方から呼ばれる。
     * エンティティの収集は内部で行う（帰還時は次元内の周囲エンティティを収集）。
     */
    public void gotoBaseWorld(Player player) {
        UUID uid = player.getUniqueId();
        teleporting.add(uid);

        // 帰還時: 現在地（次元内）の周囲エンティティを収集して一緒に連れ戻す
        List<Entity> bringEntities = collectBringEntities(player);

        Location current = player.getLocation();
        pdm.setPlotPos(uid, current.getX(), current.getY(), current.getZ());

        Location returnLoc = pdm.getReturnLocation(uid);
        if (returnLoc == null || returnLoc.getWorld() == null) {
            returnLoc = plugin.getServer().getWorlds().get(0).getSpawnLocation();
        }

        playVfx(current);
        player.addPotionEffect(new org.bukkit.potion.PotionEffect(
            org.bukkit.potion.PotionEffectType.BLINDNESS, 40, 0, true, false));

        try {
            final Location dest = returnLoc;
            player.teleport(dest);
            pullEntities(dest, bringEntities);
            pdm.clearReturnLocation(uid);
            playVfx(dest);
        } catch (Exception e) {
            plugin.getLogger().severe("gotoBaseWorld で例外: " + e.getMessage());
        } finally {
            releaseNextTick(uid);
        }
    }

    // ──────────────────────────────────────────────
    // ユーティリティ
    // ──────────────────────────────────────────────

    /**
     * teleporting を1tick後に解放する。
     * teleport() 直後に同tick内で PlayerMoveEvent が誤発火するのを防ぐ。
     */
    private void releaseNextTick(UUID uid) {
        new BukkitRunnable() {
            @Override
            public void run() {
                teleporting.remove(uid);
            }
        }.runTaskLater(plugin, 1L);
    }

    private List<Entity> collectBringEntities(Player player) {
        if (!player.isSneaking()) return Collections.emptyList();
        double radius = plugin.getConfig().getDouble("pull-entity-radius", 3.0);
        int limit = plugin.getConfig().getInt("pull-entity-limit", 10);
        List<Entity> result = new ArrayList<>();
        for (Entity e : player.getNearbyEntities(radius, radius, radius)) {
            // Player・敵モブ・Item・ArmorStand は除外
            // 連れていけるのは飼いならしたペット等の友好的エンティティのみ
            if (e instanceof org.bukkit.entity.Monster) continue;
            if (e instanceof Player) continue;
            if (e instanceof Item) continue;
            if (e instanceof ArmorStand) continue;
            result.add(e);
            if (result.size() >= limit) break;
        }
        return result;
    }

    private void pullEntities(Location dest, List<Entity> entities) {
        for (Entity e : entities) e.teleport(dest);
    }

    public void playVfx(Location loc) {
        World world = loc.getWorld();
        if (world == null) return;
        world.spawnParticle(Particle.GLOW, loc.clone().add(0, 1, 0), 50, 0.2, 0.5, 0.2, 1);
        world.spawnParticle(Particle.DUST_COLOR_TRANSITION,
            loc.clone().add(0, 1, 0), 100, 0.2, 0.5, 0.2, 1,
            new Particle.DustTransition(
                Color.fromRGB(0x00, 0xB2, 0xFF),
                Color.fromRGB(0x99, 0xFF, 0xFF), 1.0f));
        world.playSound(loc, Sound.ENTITY_ENDERMAN_TELEPORT, 2f, 0.8f);
        world.playSound(loc, Sound.ENTITY_ALLAY_ITEM_TAKEN, 2f, 0.8f);
        world.playSound(loc, Sound.BLOCK_AMETHYST_CLUSTER_BREAK, 2f, 1.2f);
    }

    public boolean isTeleporting(UUID uid) {
        return teleporting.contains(uid);
    }

}
