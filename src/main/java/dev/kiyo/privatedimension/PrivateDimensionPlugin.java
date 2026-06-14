package dev.kiyo.privatedimension;

import dev.kiyo.privatedimension.command.PDCommand;
import dev.kiyo.privatedimension.dimension.DimensionManager;
import dev.kiyo.privatedimension.item.DimensionBottleItem;
import dev.kiyo.privatedimension.listener.CraftListener;
import dev.kiyo.privatedimension.listener.ItemEntityListener;
import dev.kiyo.privatedimension.listener.ItemUseListener;
import dev.kiyo.privatedimension.listener.PlayerDeathListener;
import dev.kiyo.privatedimension.listener.PlayerMoveListener;
import dev.kiyo.privatedimension.manager.PlayerDataManager;
import dev.kiyo.privatedimension.manager.PlotManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Objects;

public class PrivateDimensionPlugin extends JavaPlugin {

    private static PrivateDimensionPlugin instance;
    private DimensionManager dimensionManager;
    private PlotManager plotManager;
    private PlayerDataManager playerDataManager;
    private DimensionBottleItem dimensionBottleItem;

    @Override
    public void onEnable() {
        instance = this;

        saveDefaultConfig();

        playerDataManager = new PlayerDataManager(this);
        dimensionManager  = new DimensionManager(this);
        plotManager       = new PlotManager(this);
        dimensionBottleItem = new DimensionBottleItem(this);

        // リスナー登録
        ItemUseListener itemUseListener = new ItemUseListener(this);
        PlayerMoveListener playerMoveListener = new PlayerMoveListener(this);
        playerMoveListener.setTeleportHandler(itemUseListener.getTeleportHandler());

        getServer().getPluginManager().registerEvents(itemUseListener, this);
        getServer().getPluginManager().registerEvents(new PlayerDeathListener(this), this);
        getServer().getPluginManager().registerEvents(playerMoveListener, this);
        getServer().getPluginManager().registerEvents(new CraftListener(this), this);
        getServer().getPluginManager().registerEvents(new ItemEntityListener(dimensionBottleItem), this);

        // コマンド登録
        Objects.requireNonNull(getCommand("pd")).setExecutor(new PDCommand(this));
        Objects.requireNonNull(getCommand("privatedim")).setExecutor(new PDCommand(this));

        // ワールド初期化（1tick後）
        new BukkitRunnable() {
            @Override
            public void run() {
                dimensionManager.initDimension();
                getLogger().info("PrivateDimension が有効化されました！");
                getLogger().info("プライベート次元ワールド: " + getConfig().getString("world-name"));
            }
        }.runTaskLater(this, 1L);
    }

    @Override
    public void onDisable() {
        if (playerDataManager != null) {
            playerDataManager.saveAll();
        }
        // log フィールドではなく getLogger() を直接使用（onEnable 前に呼ばれても NPE しない）
        getLogger().info("PrivateDimension が無効化されました。");
    }

    public static PrivateDimensionPlugin getInstance() { return instance; }
    public DimensionManager getDimensionManager()      { return dimensionManager; }
    public PlotManager getPlotManager()                { return plotManager; }
    public PlayerDataManager getPlayerDataManager()    { return playerDataManager; }
    public DimensionBottleItem getDimensionBottleItem(){ return dimensionBottleItem; }
}
