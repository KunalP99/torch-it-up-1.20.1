package torchplacer;

import net.minecraft.world.item.ItemStack;

/** Implemented by Wolf (via WolfMixin) to expose the torch EntityDataAccessor. */
public interface WolfTorchAccessor {
    ItemStack torchPlacer_getTorchStack();
    void torchPlacer_setTorchStack(ItemStack stack);
}
