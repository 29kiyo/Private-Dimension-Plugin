package dev.kiyo.privatedimension.manager;

import dev.kiyo.privatedimension.PrivateDimensionPlugin;
import org.bukkit.Location;
import org.bukkit.World;

/**
 * プロットの座標計算を担当する
 *
 * レイアウト：Z軸方向に128ブロック間隔で並ぶ
 *   plotId=0 → Z=0
 *   plotId=1 → Z=128
 *   ...
 *
 * ── Y座標の設計 ──
 * config: plot-floor-y: 64  (= structOrigin の Y 基準)
 *
 * 構造物 NBT 内部レイアウト（Y=0 が origin 基準）:
 *   Y=0〜3 : bedrock / dirt 層
 *   Y=4    : grass_block（地面表面）
 *   Y=5〜  : air（空間）
 *   Y=47   : barrier / reinforced_deepslate（天井）
 *
 * structOrigin.Y = floorY - 1 = 63
 *   → grass_block world Y = 63 + 4 = 67
 *   → プレイヤースポーン = 草上2ブロック = 67 + 2 = 69  ← 要求値
 *   → getPlotSpawn = floorY + 5 = 64 + 5 = 69  ✓
 *
 * isInsidePlot の Y 範囲:
 *   下限 = structOrigin.Y = floorY - 1 = 63
 *   上限 = structOrigin.Y + plotHeight
 *   → Y: [floorY-1, floorY-1+plotHeight]
 *
 * X/Z 範囲は plot-size に連動する（half = plot-size / 2）。
 * デフォルト同梱の plot48x48.nbt は物理的に48x48固定なので、
 * plot-size を48以外にする場合は、その幅に合ったカスタムNBTを用意すること。
 */
public class PlotManager {

    private final PrivateDimensionPlugin plugin;

    // /pd reload で再読み込みできるよう final を外している
    private int plotSize;    // 48
    private int plotSpacing; // 128
    private int floorY;      // 64
    private int plotHeight;  // 47 (構造物のY方向サイズ。境界判定・セーフスポーン探索に使用)

    private int safeSpawnSearchRadius;
    private int safeSpawnSearchHeight;

    public PlotManager(PrivateDimensionPlugin plugin) {
        this.plugin = plugin;
        reload();
    }

    /**
     * config.yml から値を再読み込みする。
     * plugin.reloadConfig() の後に必ず呼ぶこと（呼ばないと古い値のまま動作してしまう）。
     */
    public void reload() {
        this.plotSize    = plugin.getConfig().getInt("plot-size",    48);
        this.plotSpacing = plugin.getConfig().getInt("plot-spacing", 128);
        this.floorY      = plugin.getConfig().getInt("plot-floor-y", 64);
        this.plotHeight  = plugin.getConfig().getInt("plot-height",  47);
        this.safeSpawnSearchRadius = plugin.getConfig().getInt("safe-spawn-search-radius", 8);
        this.safeSpawnSearchHeight = plugin.getConfig().getInt("safe-spawn-search-height", 12);
    }

    /** プロットID → Z原点座標 */
    public int getPlotOriginZ(int plotId) {
        return plotId * plotSpacing;
    }

    /**
     * プロットID → スポーン地点
     *
     * grass_block world Y = (floorY-1) + 4 = floorY+3
     * スポーン             = floorY+3 + 2  = floorY+5 = 69 (config default)
     */
    public Location getPlotSpawn(int plotId, World world) {
        int originZ = getPlotOriginZ(plotId);
        return new Location(world, 0.5, floorY + 5, originZ + 0.5);
    }

    /**
     * 構造物配置の南西コーナー（X=-plotSize/2, Y=floorY-1, Z=originZ-plotSize/2）
     *
     * NBT Y=0 がここに対応する。
     * 注意: デフォルト同梱の plot48x48.nbt は物理的に48x48固定。
     * plot-size を48以外にする場合は、その幅に合ったカスタムNBTを用意すること
     * （そうしないと構造物と境界がズレる）。
     */
    public Location getPlotStructureOrigin(int plotId, World world) {
        int originZ = getPlotOriginZ(plotId);
        int half = plotSize / 2;
        return new Location(world, -half, floorY - 1, originZ - half);
    }

    /**
     * 座標がプロット内かチェック
     *
     * X: [-plotSize/2, plotSize/2]
     * Z: [originZ-plotSize/2, originZ+plotSize/2]
     * Y: [floorY-1, floorY-1+plotHeight]  (structOrigin Y 〜 structOrigin Y + 高さ)
     */
    public boolean isInsidePlot(int plotId, Location loc) {
        int originZ = getPlotOriginZ(plotId);
        int half = plotSize / 2;
        double x = loc.getX();
        double y = loc.getY();
        double z = loc.getZ();
        return x >= -half && x <= half
            && z >= (originZ - half) && z <= (originZ + half)
            && y >= (floorY - 1) && y <= (floorY - 1 + plotHeight);
    }

    public int getFloorY()      { return floorY; }
    public int getPlotSize()    { return plotSize; }
    public int getPlotSpacing() { return plotSpacing; }
    public int getPlotHeight()  { return plotHeight; }

    // ──────────────────────────────────────────────
    // セーフスポーン探索（カスタムNBT構造物対応）
    // ──────────────────────────────────────────────

    /**
     * guess 地点を中心に、安全な立ち位置（足元が固体、本体と頭上が空気）を
     * X・Y・Z 全方向へ同時に、半径を1ずつ広げながら探索する（3次元シェル探索）。
     *
     * 各半径 r では、dx・dy・dz のうち最大値がちょうど r になるセルだけを調べるため、
     * 真上/真下/左右だけでなく斜め方向も含めて、guess に近い順にまんべんなく走査する。
     * カスタムNBT構造物は床の高さやサイズが plot48x48.nbt と異なる場合があるため、
     * 固定オフセットのスポーンではなく実際にブロックを確認して安全地点を決める。
     * プロット範囲かどうかは問わない（NBTサイズと plot-size がズレていても、
     * そこに実在する床を見つけられるようにするため）。
     * 見つからなければ guess をそのまま返す（フォールバック）。
     */
    public Location findSafeSpawn(Location guess) {
        World world = guess.getWorld();
        if (world == null) {
            plugin.getLogger().info("[PrivateDimension] findSafeSpawn: guessのworldがnullのためそのまま返す");
            return guess;
        }

        int cx = guess.getBlockX();
        int cy = guess.getBlockY();
        int cz = guess.getBlockZ();

        plugin.getLogger().info("[PrivateDimension] findSafeSpawn 開始: guess=" + cx + "," + cy + "," + cz
            + " (radius=" + safeSpawnSearchRadius + ", height=" + safeSpawnSearchHeight + ")");

        int maxR = Math.max(safeSpawnSearchRadius, safeSpawnSearchHeight);

        for (int r = 0; r <= maxR; r++) {
            for (int dx = -r; dx <= r; dx++) {
                if (Math.abs(dx) > safeSpawnSearchRadius) continue;
                for (int dz = -r; dz <= r; dz++) {
                    if (Math.abs(dz) > safeSpawnSearchRadius) continue;
                    for (int dy = -r; dy <= r; dy++) {
                        if (Math.abs(dy) > safeSpawnSearchHeight) continue;
                        // このシェル(半径r)で初めて到達するセルだけを見る（内側は既に探索済み）
                        if (Math.max(Math.abs(dx), Math.max(Math.abs(dy), Math.abs(dz))) != r) continue;

                        int x = cx + dx, y = cy + dy, z = cz + dz;
                        if (isSafeStanding(world, x, y, z)) {
                            plugin.getLogger().info("[PrivateDimension] findSafeSpawn 成功: "
                                + x + "," + y + "," + z + " (guessから半径" + r + ")");
                            return new Location(world, x + 0.5, y, z + 0.5);
                        }
                    }
                }
            }
        }

        plugin.getLogger().warning("[PrivateDimension] セーフスポーンが見つからなかったため、"
            + "計算上のスポーン地点をそのまま使用します: " + guess);
        return guess;
    }

    /** 足元が固体で、本体・頭上が空気（通行可能）かを判定 */
    private boolean isSafeStanding(World world, int x, int y, int z) {
        var floor = world.getBlockAt(x, y - 1, z);
        var feet  = world.getBlockAt(x, y, z);
        var head  = world.getBlockAt(x, y + 1, z);
        return floor.getType().isSolid()
            && !feet.getType().isSolid()
            && !head.getType().isSolid();
    }
}
