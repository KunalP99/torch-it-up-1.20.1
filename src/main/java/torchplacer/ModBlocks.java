package torchplacer;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.TorchBlock;
import net.minecraft.world.level.block.WallTorchBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;

import java.util.EnumMap;

public class ModBlocks {
    public static final EnumMap<WoodTorchVariant, TorchBlock>    FLOOR = new EnumMap<>(WoodTorchVariant.class);
    public static final EnumMap<WoodTorchVariant, WallTorchBlock> WALL  = new EnumMap<>(WoodTorchVariant.class);

    public static void register() {
        BlockBehaviour.Properties props = BlockBehaviour.Properties.copy(Blocks.TORCH);

        for (WoodTorchVariant v : WoodTorchVariant.values()) {
            TorchBlock     floor = new TorchBlock(props, ParticleTypes.FLAME);
            WallTorchBlock wall  = new WallTorchBlock(props, ParticleTypes.FLAME);

            Registry.register(BuiltInRegistries.BLOCK,
                    new ResourceLocation(TorchPlacer.MOD_ID, v.id + "_torch"), floor);
            Registry.register(BuiltInRegistries.BLOCK,
                    new ResourceLocation(TorchPlacer.MOD_ID, v.id + "_wall_torch"), wall);

            FLOOR.put(v, floor);
            WALL.put(v, wall);
        }
    }
}
