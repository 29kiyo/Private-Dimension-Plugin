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

    public PlotManager(PrivateDimensionPlugin plugin) {
        this.plugin = plugin;
        this.plotSize    = plugin.getConfig().getInt("plot-size",    48);
        this.plotSpacing = plugin.getConfig().getInt("plot-spacing", 128);
        this.floorY      = plugin.getConfig().getInt("plot-floor-y", 64);
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
            && y >= (floorY - 1) && y <= (floorY + 46);
    }

    public int getFloorY()      { return floorY; }
    public int getPlotSize()    { return plotSize; }
    public int getPlotSpacing() { return plotSpacing; }
}
