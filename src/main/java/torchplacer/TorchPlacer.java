package torchplacer;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerType;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
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
        LOGGER.info("Torch Placer initialized.");
    }
}
