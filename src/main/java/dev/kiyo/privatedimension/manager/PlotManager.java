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
 *   上限 = structOrigin.Y + 47 = 63 + 47 = 110
 *   → Y: [floorY-1, floorY+46]
 */
public class PlotManager {

    private final PrivateDimensionPlugin plugin;

    private final int plotSize;    // 48
    private final int plotSpacing; // 128
    private final int floorY;      // 64
    private final int plotHeight;  // 47 (構造物のY方向サイズ。境界判定・セーフスポーン探索に使用)

    private final int safeSpawnSearchRadius;
    private final int safeSpawnSearchHeight;

    public PlotManager(PrivateDimensionPlugin plugin) {
        this.plugin = plugin;
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
     * 構造物配置の南西コーナー（X=-24, Y=floorY-1, Z=originZ-24）
     *
     * NBT Y=0 がここに対応する
     */
    public Location getPlotStructureOrigin(int plotId, World world) {
        int originZ = getPlotOriginZ(plotId);
        return new Location(world, -24, floorY - 1, originZ - 24);
    }

    /**
     * 座標がプロット内かチェック
     *
     * X: [-24, 24]
     * Z: [originZ-24, originZ+24]
     * Y: [floorY-1, floorY+46]  (structOrigin Y 〜 structOrigin Y + NBT高さ47)
     */
    public boolean isInsidePlot(int plotId, Location loc) {
        int originZ = getPlotOriginZ(plotId);
        double x = loc.getX();
        double y = loc.getY();
        double z = loc.getZ();
        return x >= -24 && x <= 24
            && z >= (originZ - 24) && z <= (originZ + 24)
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
     * guess 地点を起点に、安全な立ち位置（足元固体・本体と頭上が空気）を
     * 多方向（まず真上に伸びる柱、次に外側へ広がるリング状）に探索する。
     *
     * カスタムNBT構造物は床の高さやサイズが plot48x48.nbt と異なる場合があるため、
     * 固定オフセットのスポーンではなく実際にブロックを確認して安全地点を決める。
     * 見つからなければ guess をそのまま返す（フォールバック）。
     */
    public Location findSafeSpawn(Location guess) {
        World world = guess.getWorld();
        if (world == null) return guess;

        int cx = guess.getBlockX();
        int cz = guess.getBlockZ();
        int cy = guess.getBlockY();

        Location found = searchColumn(world, cx, cz, cy);
        if (found != null) return found;

        for (int r = 1; r <= safeSpawnSearchRadius; r++) {
            for (int dx = -r; dx <= r; dx++) {
                for (int dz = -r; dz <= r; dz++) {
                    // リングの外周のみ走査（内側は既に探索済み半径でカバー）
                    if (Math.max(Math.abs(dx), Math.abs(dz)) != r) continue;
                    found = searchColumn(world, cx + dx, cz + dz, cy);
                    if (found != null) return found;
                }
            }
        }

        plugin.getLogger().warning("[PrivateDimension] セーフスポーンが見つからなかったため、"
            + "計算上のスポーン地点をそのまま使用します: " + guess);
        return guess;
    }

    /** 指定 (x, z) の列を中心 y から上下に探索し、最初に見つかった安全地点を返す */
    private Location searchColumn(World world, int x, int z, int centerY) {
        for (int dy = 0; dy <= safeSpawnSearchHeight; dy++) {
            int yUp = centerY + dy;
            if (isSafeStanding(world, x, yUp, z)) {
                return new Location(world, x + 0.5, yUp, z + 0.5);
            }
            if (dy == 0) continue;
            int yDown = centerY - dy;
            if (isSafeStanding(world, x, yDown, z)) {
                return new Location(world, x + 0.5, yDown, z + 0.5);
            }
        }
        return null;
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
