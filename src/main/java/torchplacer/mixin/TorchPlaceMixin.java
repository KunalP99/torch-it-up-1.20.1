package torchplacer.mixin;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerPlayerGameMode;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import torchplacer.TorchBagItem;
import torchplacer.TorchStats;

@Mixin(ServerPlayerGameMode.class)
public class TorchPlaceMixin {

    @Inject(method = "useItemOn", at = @At("RETURN"))
    private void onUseItemOn(ServerPlayer player, Level level, ItemStack stack,
                             InteractionHand hand, BlockHitResult hitResult,
                             CallbackInfoReturnable<InteractionResult> cir) {
        if (!level.isClientSide() && TorchBagItem.isTorch(stack) && cir.getReturnValue().consumesAction()) {
            TorchStats.get(player.getServer()).increment(player.getUUID());
        }
    }
}
