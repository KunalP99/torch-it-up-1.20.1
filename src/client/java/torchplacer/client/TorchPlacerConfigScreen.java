package torchplacer.client;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import torchplacer.PlacementMode;
import torchplacer.TorchPlacerConfig;
import torchplacer.TorchPlacerNetwork;
import torchplacer.TorchSource;

public class TorchPlacerConfigScreen extends Screen {
    private static final int PANEL_WIDTH = 220;
    private static final int WIDGET_HEIGHT = 20;

    private final Screen parent;
    private final TorchPlacerConfig config;

    // Working copies — committed only on Save
    private int lightThreshold;
    private PlacementMode placementMode;
    private TorchSource torchSource;

    public TorchPlacerConfigScreen(Screen parent) {
        super(Component.translatable("screen.torch-placer.title"));
        this.parent = parent;
        this.config = TorchPlacerClient.CONFIG;
        this.lightThreshold = config.lightThreshold;
        this.placementMode = config.placementMode;
        this.torchSource = config.torchSource;
    }

    @Override
    protected void init() {
        int cx = this.width / 2;
        int y = this.height / 2 - 55;
        int left = cx - PANEL_WIDTH / 2;

        // Light threshold slider
        addRenderableWidget(new ThresholdSlider(left, y, PANEL_WIDTH, WIDGET_HEIGHT));
        y += WIDGET_HEIGHT + 8;

        // Placement mode cycle button
        addRenderableWidget(Button.builder(
                        Component.literal("Placement: " + placementMode.getDisplayName()),
                        btn -> {
                            placementMode = placementMode.next();
                            btn.setMessage(Component.literal("Placement: " + placementMode.getDisplayName()));
                        })
                .bounds(left, y, PANEL_WIDTH, WIDGET_HEIGHT)
                .build());
        y += WIDGET_HEIGHT + 8;

        // Torch source cycle button
        addRenderableWidget(Button.builder(
                        Component.literal("Torch Source: " + torchSource.getDisplayName()),
                        btn -> {
                            torchSource = torchSource.next();
                            btn.setMessage(Component.literal("Torch Source: " + torchSource.getDisplayName()));
                        })
                .bounds(left, y, PANEL_WIDTH, WIDGET_HEIGHT)
                .build());
        y += WIDGET_HEIGHT + 16;

        // Save / Cancel buttons
        int half = PANEL_WIDTH / 2 - 4;
        addRenderableWidget(Button.builder(
                        Component.translatable("screen.torch-placer.save"), btn -> save())
                .bounds(left, y, half, WIDGET_HEIGHT)
                .build());
        addRenderableWidget(Button.builder(
                        Component.translatable("screen.torch-placer.cancel"), btn -> onClose())
                .bounds(left + half + 8, y, half, WIDGET_HEIGHT)
                .build());
    }

    private void save() {
        config.lightThreshold = lightThreshold;
        config.placementMode = placementMode;
        config.torchSource = torchSource;
        config.save();
        if (this.minecraft != null && this.minecraft.getConnection() != null) {
            ClientPlayNetworking.send(TorchPlacerNetwork.CONFIG_SYNC, TorchPlacerNetwork.buildPacket(config));
        }
        onClose();
    }

    @Override
    public void onClose() {
        if (this.minecraft != null) this.minecraft.setScreen(parent);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float delta) {
        renderBackground(guiGraphics);
        guiGraphics.drawCenteredString(font, title, this.width / 2, this.height / 2 - 80, 0xFFFFFF);
        super.render(guiGraphics, mouseX, mouseY, delta);
    }

    // ── Sliders ──────────────────────────────────────────────────────────────

    private class ThresholdSlider extends AbstractSliderButton {
        ThresholdSlider(int x, int y, int width, int height) {
            super(x, y, width, height, Component.empty(), lightThreshold / 14.0);
            updateMessage();
        }

        @Override
        protected void updateMessage() {
            setMessage(Component.literal("Light Threshold: " + lightThreshold));
            setTooltip(Tooltip.create(Component.literal(
                    levelDescription() + "\n\n" +
                    "Hostile mobs spawn at light level 0.\n" +
                    "Higher values = more torches placed."
            )));
        }

        @Override
        protected void applyValue() {
            lightThreshold = (int) Math.round(this.value * 14);
        }

        private String levelDescription() {
            if (lightThreshold == 0)  return "Level 0 — Pitch dark only, very few placements.";
            if (lightThreshold <= 3)  return "Level " + lightThreshold + " — Very dark areas only.";
            if (lightThreshold <= 6)  return "Level " + lightThreshold + " — Dim areas.";
            if (lightThreshold == 7)  return "Level 7 — Default. Comfortable safety buffer.";
            if (lightThreshold <= 10) return "Level " + lightThreshold + " — Moderately lit areas.";
            if (lightThreshold <= 13) return "Level " + lightThreshold + " — Fairly bright areas, aggressive placement.";
            return "Level 14 — Almost everywhere, maximum placement.";
        }
    }

}

