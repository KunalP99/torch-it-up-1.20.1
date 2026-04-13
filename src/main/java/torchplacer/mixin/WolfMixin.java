package torchplacer.mixin;

import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.animal.Wolf;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import torchplacer.WolfTorchAccessor;

@Mixin(Wolf.class)
public class WolfMixin implements WolfTorchAccessor {

    @Unique
    private static final EntityDataAccessor<ItemStack> TORCH_PLACER_TORCH =
            SynchedEntityData.defineId(Wolf.class, EntityDataSerializers.ITEM_STACK);

    @Inject(method = "defineSynchedData", at = @At("TAIL"))
    private void torchPlacer_initTorchData(CallbackInfo ci) {
        ((Wolf) (Object) this).getEntityData().define(TORCH_PLACER_TORCH, ItemStack.EMPTY);
    }

    @Override
    public ItemStack torchPlacer_getTorchStack() {
        return ((Wolf) (Object) this).getEntityData().get(TORCH_PLACER_TORCH);
    }

    @Override
    public void torchPlacer_setTorchStack(ItemStack stack) {
        ((Wolf) (Object) this).getEntityData().set(TORCH_PLACER_TORCH, stack);
    }
}
