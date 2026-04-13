package torchplacer;

import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.animal.Wolf;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class WolfTorchManager {

    /** UUIDs of wolves currently carrying a torch. */
    private static final Set<UUID> TRACKED_WOLVES = new HashSet<>();

    public static final Map<UUID, BlockPos> WOLF_LIGHT_POSITIONS = new HashMap<>();
    public static final Map<UUID, BlockPos> WOLF_DEFERRED_CLEARS = new HashMap<>();
    private static final Map<UUID, Integer> WOLF_LIGHT_LEVELS = new HashMap<>();

    public static boolean hasTorch(Wolf wolf) {
        return !((WolfTorchAccessor) wolf).torchPlacer_getTorchStack().isEmpty();
    }

    public static void attach(Wolf wolf, ItemStack torchStack, Player player) {
        ItemStack toGive = torchStack.copyWithCount(1);
        torchStack.shrink(1);
        ((WolfTorchAccessor) wolf).torchPlacer_setTorchStack(toGive);
        TRACKED_WOLVES.add(wolf.getUUID());
    }

    /** Called when a player right-clicks a torch-carrying wolf with a non-torch item. */
    public static void detach(Wolf wolf, ServerLevel world, Player player) {
        ItemStack torch = ((WolfTorchAccessor) wolf).torchPlacer_getTorchStack();
        if (!torch.isEmpty() && player != null) {
            if (!player.getInventory().add(torch.copy())) {
                player.drop(torch.copy(), false);
            }
        }
        clearWolf(wolf, world);
    }

    /** Called on wolf death — drops the torch at the death location. */
    public static void dropTorch(Wolf wolf, ServerLevel world) {
        ItemStack torch = ((WolfTorchAccessor) wolf).torchPlacer_getTorchStack();
        if (!torch.isEmpty()) {
            wolf.spawnAtLocation(torch.copy());
        }
        clearWolf(wolf, world);
    }

    private static void clearWolf(Wolf wolf, ServerLevel world) {
        ((WolfTorchAccessor) wolf).torchPlacer_setTorchStack(ItemStack.EMPTY);
        UUID uuid = wolf.getUUID();
        TRACKED_WOLVES.remove(uuid);
        BlockPos current = WOLF_LIGHT_POSITIONS.remove(uuid);
        if (current != null) TorchPlacerLogic.clearLightBlock(world, current);
        BlockPos deferred = WOLF_DEFERRED_CLEARS.remove(uuid);
        if (deferred != null) TorchPlacerLogic.clearLightBlock(world, deferred);
        WOLF_LIGHT_LEVELS.remove(uuid);
    }

    public static void tick(MinecraftServer server) {
        Map<UUID, BlockPos> toFlush = new HashMap<>(WOLF_DEFERRED_CLEARS);
        WOLF_DEFERRED_CLEARS.clear();

        Set<UUID> dead = new HashSet<>();

        for (UUID uuid : TRACKED_WOLVES) {
            Wolf wolf = findWolf(server, uuid);
            if (wolf == null) {
                dead.add(uuid);
                continue;
            }

            ServerLevel world = (ServerLevel) wolf.level();
            ItemStack torch = ((WolfTorchAccessor) wolf).torchPlacer_getTorchStack();
            if (torch.isEmpty()) {
                dead.add(uuid);
                continue;
            }

            int lightLevel = TorchBagItem.getTorchLightLevel(torch);
            BlockPos target = findLightPos(wolf, world);

            if (target != null) {
                BlockPos current = WOLF_LIGHT_POSITIONS.get(uuid);
                int currentLevel = WOLF_LIGHT_LEVELS.getOrDefault(uuid, -1);
                boolean posChanged   = !target.equals(current);
                boolean levelChanged = lightLevel != currentLevel;
                if (posChanged || levelChanged) {
                    TorchPlacerLogic.placeLightBlock(world, target, lightLevel);
                    if (posChanged && current != null) WOLF_DEFERRED_CLEARS.put(uuid, current);
                    WOLF_LIGHT_POSITIONS.put(uuid, target);
                    WOLF_LIGHT_LEVELS.put(uuid, lightLevel);
                }
            }
            toFlush.remove(uuid);
        }

        // Clean up any wolves that disappeared
        dead.forEach(uuid -> {
            TRACKED_WOLVES.remove(uuid);
            WOLF_LIGHT_POSITIONS.remove(uuid);
            WOLF_LIGHT_LEVELS.remove(uuid);
            WOLF_DEFERRED_CLEARS.remove(uuid);
        });

        // Flush old light positions after all new ones have been placed
        toFlush.forEach((uuid, pos) -> {
            for (ServerLevel world : server.getAllLevels()) {
                TorchPlacerLogic.clearLightBlock(world, pos);
            }
        });
    }

    private static Wolf findWolf(MinecraftServer server, UUID uuid) {
        for (ServerLevel world : server.getAllLevels()) {
            Entity entity = world.getEntity(uuid);
            if (entity instanceof Wolf wolf) return wolf;
        }
        return null;
    }

    private static BlockPos findLightPos(Wolf wolf, ServerLevel world) {
        BlockPos feet = wolf.blockPosition();
        var feetState = world.getBlockState(feet);
        if (feetState.isAir() || feetState.is(Blocks.LIGHT)) return feet;
        BlockPos head = feet.above();
        var headState = world.getBlockState(head);
        if (headState.isAir() || headState.is(Blocks.LIGHT)) return head;
        return null;
    }
}
