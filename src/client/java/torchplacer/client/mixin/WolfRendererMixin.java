package torchplacer.client.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.Wolf;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import torchplacer.WolfTorchAccessor;

@Mixin(LivingEntityRenderer.class)
public class WolfRendererMixin {

    @Inject(method = "render", at = @At("TAIL"))
    private void torchPlacer_renderWolfTorch(LivingEntity entity, float entityYaw, float partialTick,
                                              PoseStack poseStack, MultiBufferSource buffer,
                                              int packedLight, CallbackInfo ci) {
        if (!(entity instanceof Wolf wolf)) return;
        ItemStack torch = ((WolfTorchAccessor) wolf).torchPlacer_getTorchStack();
        if (torch.isEmpty()) return;

        poseStack.pushPose();
        // Position the torch on top of the wolf's back, centred
        poseStack.translate(0.0, 0.7, 0.0);
        poseStack.scale(0.5f, 0.5f, 0.5f);
        Minecraft.getInstance().getItemRenderer().renderStatic(
                torch,
                ItemDisplayContext.GROUND,
                packedLight,
                OverlayTexture.NO_OVERLAY,
                poseStack,
                buffer,
                null,
                wolf.getId());
        poseStack.popPose();
    }
}
