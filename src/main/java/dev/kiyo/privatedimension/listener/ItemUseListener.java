package dev.kiyo.privatedimension.listener;

import dev.kiyo.privatedimension.PrivateDimensionPlugin;
import dev.kiyo.privatedimension.item.DimensionBottleItem;
import dev.kiyo.privatedimension.util.PaperUtil;
import dev.kiyo.privatedimension.util.TeleportHandler;
import org.bukkit.entity.Player;
import org.bukkit.entity.ThrownPotion;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.PotionSplashEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

/**
 * Dimension in a Bottle の使用検知
 *
 * LingeringPotionSplashEvent は Paper 専用のため、
 * Paper の場合のみ LingeringListener（内部クラス）を登録する。
 * Spigot では PotionSplashEvent のみで代替。
 */
public class ItemUseListener implements Listener {

    private final PrivateDimensionPlugin plugin;
    private final DimensionBottleItem bottleItem;
    private final TeleportHandler teleportHandler;

    public ItemUseListener(PrivateDimensionPlugin plugin) {
        this.plugin = plugin;
        this.bottleItem = plugin.getDimensionBottleItem();
        this.teleportHandler = new TeleportHandler(plugin);

        // LingeringPotionSplashEvent は Paper 専用クラスのため
        // Spigot では ClassNotFoundException になるので条件付きで登録
        if (PaperUtil.isPaper()) {
            plugin.getServer().getPluginManager()
                .registerEvents(new LingeringListener(bottleItem), plugin);
        }
    }

    public TeleportHandler getTeleportHandler() {
        return teleportHandler;
    }

    // ── 層1b: PlayerInteractAtEntityEvent ────────────────────
    // エンティティ（mob等）にカーソルを合わせて右クリックしたとき
    // PlayerInteractEvent が発火しないケースがある（特にSpigot）ため
    // こちらも監視してテレポートを起動する

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = false)
    public void onInteractAtEntity(PlayerInteractAtEntityEvent event) {
        // OFF_HAND は二重実行防止（RIGHT_CLICK_BLOCK と同じ理由）
        if (event.getHand() == EquipmentSlot.OFF_HAND) return;

        Player player = event.getPlayer();
        ItemStack main = player.getInventory().getItemInMainHand();
        ItemStack off  = player.getInventory().getItemInOffHand();

        boolean hasDimensionBottle = bottleItem.isDimensionBottle(main)
                                  || bottleItem.isDimensionBottle(off);
        if (!hasDimensionBottle) return;

        event.setCancelled(true);

        if (!player.hasPermission("privatedimension.use")) {
            player.sendMessage("§c[PrivateDimension] 使用権限がありません。");
            return;
        }

        teleportHandler.handleUse(player);
    }

    // ── 層1: PlayerInteractEvent ──────────────────────────────

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = false)
    public void onInteract(PlayerInteractEvent event) {
        Action action = event.getAction();
        if (action != Action.RIGHT_CLICK_AIR && action != Action.RIGHT_CLICK_BLOCK) return;

        Player player = event.getPlayer();
        ItemStack main = player.getInventory().getItemInMainHand();
        ItemStack off  = player.getInventory().getItemInOffHand();

        boolean hasDimensionBottle = bottleItem.isDimensionBottle(main)
                                  || bottleItem.isDimensionBottle(off);
        if (!hasDimensionBottle) return;

        event.setCancelled(true);
        event.setUseItemInHand(Event.Result.DENY);
        event.setUseInteractedBlock(Event.Result.DENY);

        // RIGHT_CLICK_BLOCK + OFF_HAND: HAND で処理済み → スキップ
        // RIGHT_CLICK_AIR  + OFF_HAND: メインが空でオフハンドが主役 → 処理する
        EquipmentSlot hand = event.getHand();
        if (hand == EquipmentSlot.OFF_HAND && action == Action.RIGHT_CLICK_BLOCK) return;

        if (!player.hasPermission("privatedimension.use")) {
            player.sendMessage("§c[PrivateDimension] 使用権限がありません。");
            return;
        }

        teleportHandler.handleUse(player);
    }

    // ── 層2: ProjectileLaunchEvent ────────────────────────────

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = false)
    public void onProjectileLaunch(ProjectileLaunchEvent event) {
        if (!(event.getEntity() instanceof ThrownPotion thrown)) return;
        if (!(thrown.getShooter() instanceof Player player)) return;
        if (!bottleItem.isDimensionBottle(thrown.getItem())) return;

        ItemStack main = player.getInventory().getItemInMainHand();
        if (bottleItem.isDimensionBottle(main)) {
            thrown.remove();
        } else {
            thrown.remove();
            event.setCancelled(true);
        }
    }

    // ── 層3: PotionSplashEvent（Java / Spigot 共通） ──────────

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = false)
    public void onPotionSplash(PotionSplashEvent event) {
        if (!(event.getEntity() instanceof ThrownPotion thrown)) return;
        if (!(thrown.getShooter() instanceof Player)) return;
        if (!bottleItem.isDimensionBottle(thrown.getItem())) return;
        event.setCancelled(true);
    }

    // ── 層3追加: LingeringPotionSplashEvent（Paper 専用、動的登録） ──

    /**
     * Paper 環境でのみ登録される内部リスナー。
     * Spigot でこのクラスをロードすると ClassNotFoundException になるため、
     * PaperUtil.isPaper() が true のときだけインスタンス化する。
     */
    static class LingeringListener implements Listener {
        private final DimensionBottleItem bottleItem;

        LingeringListener(DimensionBottleItem bottleItem) {
            this.bottleItem = bottleItem;
        }

        @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = false)
        public void onLingeringSplash(org.bukkit.event.entity.LingeringPotionSplashEvent event) {
            if (!(event.getEntity() instanceof org.bukkit.entity.LingeringPotion lp)) return;
            if (!(lp.getShooter() instanceof Player)) return;
            if (!bottleItem.isDimensionBottle(lp.getItem())) return;
            event.setCancelled(true);
        }
    }
}
