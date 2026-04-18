package torchplacer;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class TorchStats extends SavedData {
    private static final String DATA_NAME = "torch-placer-stats";
    private final Map<UUID, Long> counts = new HashMap<>();

    public static TorchStats get(MinecraftServer server) {
        return server.overworld().getDataStorage()
                .computeIfAbsent(TorchStats::load, TorchStats::new, DATA_NAME);
    }

    private static TorchStats load(CompoundTag tag) {
        TorchStats stats = new TorchStats();
        CompoundTag players = tag.getCompound("players");
        for (String key : players.getAllKeys())
            stats.counts.put(UUID.fromString(key), players.getLong(key));
        return stats;
    }

    @Override
    public CompoundTag save(CompoundTag tag) {
        CompoundTag players = new CompoundTag();
        counts.forEach((uuid, n) -> players.putLong(uuid.toString(), n));
        tag.put("players", players);
        return tag;
    }

    public void increment(UUID uuid) {
        counts.merge(uuid, 1L, Long::sum);
        setDirty();
    }

    public long getCount(UUID uuid) {
        return counts.getOrDefault(uuid, 0L);
    }
}
