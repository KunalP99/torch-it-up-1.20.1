package torchplacer;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.screenhandler.v1.ScreenHandlerRegistry;
import net.minecraft.resources.ResourceLocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TorchPlacer implements ModInitializer {
    public static final String MOD_ID = "torch-placer";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        TorchBagMenu.TYPE = ScreenHandlerRegistry.registerExtended(
                new ResourceLocation(MOD_ID, "torch_bag"),
                (syncId, inv, buf) -> new TorchBagMenu(syncId, inv, buf)
        );
        ModBlocks.register();
        ModItems.register();
        TorchPlacerNetwork.registerServerReceiver();
        ServerTickEvents.END_SERVER_TICK.register(TorchPlacerLogic::tick);
        LOGGER.info("Torch Placer initialized.");
    }
}
