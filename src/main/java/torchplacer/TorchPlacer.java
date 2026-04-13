package torchplacer;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.animal.Wolf;
import net.minecraft.world.item.ItemStack;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TorchPlacer implements ModInitializer {
    public static final String MOD_ID = "torch-placer";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        TorchBagMenu.TYPE = new ExtendedScreenHandlerType<>(TorchBagMenu::new);
        Registry.register(BuiltInRegistries.MENU, new ResourceLocation(MOD_ID, "torch_bag"), TorchBagMenu.TYPE);
        ModBlocks.register();
        ModItems.register();
        TorchPlacerNetwork.registerServerReceiver();
        ServerTickEvents.END_SERVER_TICK.register(TorchPlacerLogic::tick);

        // Wolf torch: right-click a tamed wolf with a torch to give/take
        UseEntityCallback.EVENT.register((player, world, hand, entity, hitResult) -> {
            if (world.isClientSide) return InteractionResult.PASS;
            if (!(entity instanceof Wolf wolf)) return InteractionResult.PASS;
            if (!wolf.isTame()) return InteractionResult.PASS;

            ItemStack held = player.getItemInHand(hand);
            ServerLevel serverLevel = (ServerLevel) world;

            if (TorchBagItem.isTorch(held)) {
                if (WolfTorchManager.hasTorch(wolf)) {
                    WolfTorchManager.detach(wolf, serverLevel, player);
                    player.displayClientMessage(
                            Component.literal(getWolfName(wolf) + " dropped its torch"), true);
                } else {
                    WolfTorchManager.attach(wolf, held, player);
                    player.displayClientMessage(
                            Component.literal(getWolfName(wolf) + " is now carrying a torch"), true);
                }
                return InteractionResult.sidedSuccess(false);
            } else if (held.isEmpty() && WolfTorchManager.hasTorch(wolf)) {
                WolfTorchManager.detach(wolf, serverLevel, player);
                player.displayClientMessage(
                        Component.literal(getWolfName(wolf) + " dropped its torch"), true);
                return InteractionResult.sidedSuccess(false);
            }
            return InteractionResult.PASS;
        });

        // Wolf death: drop the torch at the death location
        ServerLivingEntityEvents.AFTER_DEATH.register((entity, source) -> {
            if (entity instanceof Wolf wolf && WolfTorchManager.hasTorch(wolf)) {
                WolfTorchManager.dropTorch(wolf, (ServerLevel) entity.level());
            }
        });

        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> {
            ServerPlayer player = handler.getPlayer();
            ServerLevel world = (ServerLevel) player.level();
            BlockPos lightPos = TorchPlacerLogic.HELD_LIGHT_POSITIONS.remove(player.getUUID());
            if (lightPos != null) {
                TorchPlacerLogic.clearLightBlock(world, lightPos);
            }
            BlockPos deferredPos = TorchPlacerLogic.DEFERRED_CLEARS.remove(player.getUUID());
            if (deferredPos != null) {
                TorchPlacerLogic.clearLightBlock(world, deferredPos);
            }
        });

        LOGGER.info("Torch Placer initialized.");
    }

    private static String getWolfName(Wolf wolf) {
        return wolf.hasCustomName() ? wolf.getCustomName().getString() : "Your wolf";
    }
}
