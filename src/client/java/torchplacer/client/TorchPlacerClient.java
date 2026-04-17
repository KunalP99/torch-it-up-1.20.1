package torchplacer.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import torchplacer.ModBlocks;
import torchplacer.TorchBagMenu;
import torchplacer.TorchPlacerConfig;
import torchplacer.TorchPlacerNetwork;
import torchplacer.WoodTorchVariant;

public class TorchPlacerClient implements ClientModInitializer {
    public static TorchPlacerConfig CONFIG;
    private static int particleTick = 0;

    @Override
    public void onInitializeClient() {
        CONFIG = TorchPlacerConfig.load();

        // Register cutout render layer so transparent pixels don't render as black
        for (WoodTorchVariant v : WoodTorchVariant.values()) {
            BlockRenderLayerMap.INSTANCE.putBlock(ModBlocks.FLOOR.get(v), RenderType.cutout());
            BlockRenderLayerMap.INSTANCE.putBlock(ModBlocks.WALL.get(v), RenderType.cutout());
        }
        BlockRenderLayerMap.INSTANCE.putBlock(ModBlocks.UNDERWATER_FLOOR, RenderType.cutout());
        BlockRenderLayerMap.INSTANCE.putBlock(ModBlocks.UNDERWATER_WALL,  RenderType.cutout());

        MenuScreens.register(TorchBagMenu.TYPE, TorchBagScreen::new);

        KeyBindings.register();
        HudRenderCallback.EVENT.register(TorchHud::render);

        // Sync config to server when joining a world
        ClientPlayConnectionEvents.JOIN.register((handler, sender, client) ->
                ClientPlayNetworking.send(TorchPlacerNetwork.CONFIG_SYNC, TorchPlacerNetwork.buildPacket(CONFIG))
        );

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.player == null) return;

            ItemStack mainHand = client.player.getMainHandItem();
            ItemStack offHand  = client.player.getOffhandItem();
            if ((mainHand.is(Items.SOUL_TORCH) || offHand.is(Items.SOUL_TORCH)) && ++particleTick % 3 == 0) {
                var pos  = client.player.position();
                var rand = client.player.getRandom();
                for (int i = 0; i < 4; i++) {
                    double ox = (rand.nextDouble() - 0.5) * 1.2;
                    double oy =  rand.nextDouble() * 2.0;
                    double oz = (rand.nextDouble() - 0.5) * 1.2;
                    client.level.addParticle(ParticleTypes.SOUL_FIRE_FLAME,
                            pos.x + ox, pos.y + oy, pos.z + oz, 0.0, 0.04, 0.0);
                }
            }

            if (KeyBindings.KEY_TOGGLE.consumeClick()) {
                CONFIG.enabled = !CONFIG.enabled;
                CONFIG.save();
                String state = CONFIG.enabled ? "ON" : "OFF";
                client.player.displayClientMessage(Component.literal("Auto Torch: " + state), true);
                if (client.getConnection() != null) {
                    ClientPlayNetworking.send(TorchPlacerNetwork.CONFIG_SYNC, TorchPlacerNetwork.buildPacket(CONFIG));
                }
            }

            if (KeyBindings.KEY_CONFIG.consumeClick()) {
                client.setScreen(new TorchPlacerConfigScreen(client.screen));
            }
        });
    }
}
