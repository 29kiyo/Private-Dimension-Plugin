package dev.kiyo.privatedimension.dimension;

import dev.kiyo.privatedimension.PrivateDimensionPlugin;
import dev.kiyo.privatedimension.util.PaperUtil;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.generator.ChunkGenerator;

import java.io.*;
import java.nio.file.Files;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * プライベート次元ワールドの生成・管理
 *
 * 構造物配置:
 *   Paper → StructureManager API（高速）
 *   Spigot → NBT 手動パースで setBlock() ループ
 */
public class DimensionManager {

    private final PrivateDimensionPlugin plugin;
    private World privateDimension;
    private final String worldName;

    public DimensionManager(PrivateDimensionPlugin plugin) {
        this.plugin = plugin;
        this.worldName = plugin.getConfig().getString("world-name", "private_dimension");
    }

    public void initDimension() {
        privateDimension = Bukkit.getWorld(worldName);
        if (privateDimension != null) {
            plugin.getLogger().info("既存のプライベート次元ワールドを読み込みました: " + worldName);
            applyWorldSettings();
            return;
        }

        WorldCreator creator = new WorldCreator(worldName)
            .environment(World.Environment.NORMAL)
            .generator(new VoidChunkGenerator())
            .generateStructures(false);

        privateDimension = creator.createWorld();

        if (privateDimension == null) {
            plugin.getLogger().severe("プライベート次元ワールドの作成に失敗しました！");
            return;
        }

        applyWorldSettings();
        plugin.getLogger().info("プライベート次元ワールドを新規作成しました: " + worldName);
    }

    private void applyWorldSettings() {
        if (privateDimension == null) return;
        privateDimension.setSpawnFlags(false, false);
        privateDimension.setDifficulty(Difficulty.PEACEFUL);
        privateDimension.setGameRule(GameRule.DO_MOB_SPAWNING,   false);
        privateDimension.setGameRule(GameRule.DO_DAYLIGHT_CYCLE, false);
        privateDimension.setGameRule(GameRule.DO_WEATHER_CYCLE,  false);
        privateDimension.setGameRule(GameRule.DO_FIRE_TICK,      false);
        privateDimension.setTime(6000);
        privateDimension.setStorm(false);
        privateDimension.setThundering(false);
    }

    public World getPrivateDimension() { return privateDimension; }

    public boolean isPrivateDimension(World world) {
        return world != null && world.getName().equals(worldName);
    }

    // ──────────────────────────────────────────────
    // 構造物配置（Paper / Spigot 分岐）
    // ──────────────────────────────────────────────

    public void placeStructure(Location origin) {
        byte[] nbtBytes = extractNbt();
        if (nbtBytes == null) return;

        if (PaperUtil.isPaper()) {
            placeStructurePaper(origin, nbtBytes);
        } else {
            placeStructureSpigot(origin, nbtBytes);
        }
    }

    /** NBT バイト列を取得（キャッシュファイルに書き出し、MD5で更新検知） */
    private byte[] extractNbt() {
        try (InputStream in = plugin.getResource("plot48x48.nbt")) {
            if (in == null) {
                plugin.getLogger().severe("plot48x48.nbt リソースが見つかりません！");
                return null;
            }
            return in.readAllBytes();
        } catch (IOException e) {
            plugin.getLogger().severe("plot48x48.nbt の読み込みに失敗: " + e.getMessage());
            return null;
        }
    }

    // ── Paper 用：StructureManager API ──

    private void placeStructurePaper(Location origin, byte[] nbtBytes) {
        try {
            File structureDir = new File(plugin.getServer().getWorldContainer(),
                worldName + "/generated/private_dimension/structures");
            structureDir.mkdirs();
            File structureFile = new File(structureDir, "plot48x48.nbt");

            if (!structureFile.exists() || !md5Matches(structureFile, nbtBytes)) {
                Files.write(structureFile.toPath(), nbtBytes);
                plugin.getLogger().info("plot48x48.nbt を展開しました。");
            }

            org.bukkit.structure.StructureManager sm = Bukkit.getServer().getStructureManager();
            org.bukkit.structure.Structure structure;
            try (InputStream is = new FileInputStream(structureFile)) {
                structure = sm.loadStructure(is);
            }
            structure.place(origin, true,
                org.bukkit.block.structure.StructureRotation.NONE,
                org.bukkit.block.structure.Mirror.NONE,
                0, 1.0f, new java.util.Random());

        } catch (IOException e) {
            plugin.getLogger().severe("構造物の配置に失敗(Paper): " + e.getMessage());
        }
    }

    // ── Spigot 用：NBT 手動パースで setBlock() ──

    /**
     * gzip 圧縮された Vanilla Structure File（.nbt）を手動で読み込み、
     * Bukkit の setBlockData() でブロックを配置する。
     *
     * NBT 構造:
     *   size: [X, Y, Z]
     *   palette: [{Name, Properties?}, ...]
     *   blocks:  [{state(int), pos:[x,y,z]}, ...]
     */
    private void placeStructureSpigot(Location origin, byte[] nbtBytes) {
        try (DataInputStream dis = new DataInputStream(
                new java.util.zip.GZIPInputStream(new ByteArrayInputStream(nbtBytes)))) {

            NbtCompound root = NbtReader.readCompound(dis);

            NbtCompound rootData = (NbtCompound) root.get("");
            if (rootData == null) rootData = root;

            NbtList paletteList = (NbtList) rootData.get("palette");
            NbtList blockList   = (NbtList) rootData.get("blocks");

            if (paletteList == null || blockList == null) {
                plugin.getLogger().severe("NBT の palette/blocks が読めません");
                return;
            }

            // パレット → BlockData のキャッシュ
            BlockData[] palette = new BlockData[paletteList.size()];
            for (int i = 0; i < paletteList.size(); i++) {
                NbtCompound entry = (NbtCompound) paletteList.get(i);
                String name = (String) entry.get("Name");
                StringBuilder bd = new StringBuilder(name);
                NbtCompound props = (NbtCompound) entry.get("Properties");
                if (props != null && !props.isEmpty()) {
                    bd.append('[');
                    boolean first = true;
                    for (java.util.Map.Entry<String, Object> p : props.entrySet()) {
                        if (!first) bd.append(',');
                        bd.append(p.getKey()).append('=').append(p.getValue());
                        first = false;
                    }
                    bd.append(']');
                }
                try {
                    palette[i] = Bukkit.createBlockData(bd.toString());
                } catch (IllegalArgumentException e) {
                    palette[i] = Bukkit.createBlockData(Material.AIR);
                    plugin.getLogger().warning("不明なブロック: " + bd + " → air で代替");
                }
            }

            World world = origin.getWorld();
            int ox = origin.getBlockX();
            int oy = origin.getBlockY();
            int oz = origin.getBlockZ();

            // air はスキップしてパフォーマンス向上
            Material air = Material.AIR;

            for (Object obj : blockList) {
                NbtCompound blockEntry = (NbtCompound) obj;
                int state = ((Number) blockEntry.get("state")).intValue();
                NbtList pos = (NbtList) blockEntry.get("pos");
                int bx = ((Number) pos.get(0)).intValue();
                int by = ((Number) pos.get(1)).intValue();
                int bz = ((Number) pos.get(2)).intValue();

                BlockData bd = palette[state];
                if (bd.getMaterial() == air) continue;

                Block block = world.getBlockAt(ox + bx, oy + by, oz + bz);
                block.setBlockData(bd, false);
            }

        } catch (IOException e) {
            plugin.getLogger().severe("構造物の配置に失敗(Spigot): " + e.getMessage());
        }
    }

    private boolean md5Matches(File file, byte[] resourceBytes) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] fileBytes = Files.readAllBytes(file.toPath());
            return java.util.Arrays.equals(md.digest(fileBytes), md.digest(resourceBytes));
        } catch (NoSuchAlgorithmException | IOException e) {
            return false;
        }
    }

    public static class VoidChunkGenerator extends ChunkGenerator {}

    // ──────────────────────────────────────────────
    // 最小限の NBT リーダー（外部ライブラリ不要）
    // ──────────────────────────────────────────────

    static class NbtCompound extends java.util.LinkedHashMap<String, Object> {}
    static class NbtList extends java.util.ArrayList<Object> {}

    static class NbtReader {
        static final int TAG_END = 0, TAG_BYTE = 1, TAG_SHORT = 2, TAG_INT = 3,
            TAG_LONG = 4, TAG_FLOAT = 5, TAG_DOUBLE = 6, TAG_BYTE_ARRAY = 7,
            TAG_STRING = 8, TAG_LIST = 9, TAG_COMPOUND = 10,
            TAG_INT_ARRAY = 11, TAG_LONG_ARRAY = 12;

        static NbtCompound readCompound(DataInputStream dis) throws IOException {
            int type = dis.readByte() & 0xFF;
            if (type != TAG_COMPOUND) throw new IOException("Root tag is not TAG_Compound");
            readString(dis); // root name
            return readCompoundPayload(dis);
        }

        static NbtCompound readCompoundPayload(DataInputStream dis) throws IOException {
            NbtCompound map = new NbtCompound();
            while (true) {
                int type = dis.readByte() & 0xFF;
                if (type == TAG_END) break;
                String name = readString(dis);
                map.put(name, readPayload(dis, type));
            }
            return map;
        }

        static Object readPayload(DataInputStream dis, int type) throws IOException {
            return switch (type) {
                case TAG_BYTE       -> dis.readByte();
                case TAG_SHORT      -> dis.readShort();
                case TAG_INT        -> dis.readInt();
                case TAG_LONG       -> dis.readLong();
                case TAG_FLOAT      -> dis.readFloat();
                case TAG_DOUBLE     -> dis.readDouble();
                case TAG_STRING     -> readString(dis);
                case TAG_COMPOUND   -> readCompoundPayload(dis);
                case TAG_LIST       -> readListPayload(dis);
                case TAG_BYTE_ARRAY -> { int len = dis.readInt(); byte[] b = new byte[len]; dis.readFully(b); yield b; }
                case TAG_INT_ARRAY  -> { int len = dis.readInt(); int[] a = new int[len]; for (int i = 0; i < len; i++) a[i] = dis.readInt(); yield a; }
                case TAG_LONG_ARRAY -> { int len = dis.readInt(); long[] a = new long[len]; for (int i = 0; i < len; i++) a[i] = dis.readLong(); yield a; }
                default -> throw new IOException("Unknown tag type: " + type);
            };
        }

        static NbtList readListPayload(DataInputStream dis) throws IOException {
            int elemType = dis.readByte() & 0xFF;
            int size = dis.readInt();
            NbtList list = new NbtList();
            for (int i = 0; i < size; i++) {
                list.add(readPayload(dis, elemType));
            }
            return list;
        }

        static String readString(DataInputStream dis) throws IOException {
            int len = dis.readUnsignedShort();
            byte[] bytes = new byte[len];
            dis.readFully(bytes);
            return new String(bytes, java.nio.charset.StandardCharsets.UTF_8);
        }
    }
}
