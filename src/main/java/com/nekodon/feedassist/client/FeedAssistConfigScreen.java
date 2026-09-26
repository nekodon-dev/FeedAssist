package com.nekodon.feedassist.client;

import com.nekodon.feedassist.config.ModConfig;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.network.chat.Component;

public class FeedAssistConfigScreen extends Screen {
    private final Screen parent;
    private RangeAbstractSliderButton rangeSlider;
    private boolean enabled;

    public FeedAssistConfigScreen(Screen parent) {
        super(Component.translatable("text.feed-assist.title"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        int centerX = this.width / 2;
        int sliderY = this.height / 2 - 20;
        enabled = ModConfig.isEnabled();

        rangeSlider = new RangeAbstractSliderButton(centerX - 100, sliderY, 200, 20, ModConfig.getFeedRange());
        addRenderableWidget(rangeSlider);

        addRenderableWidget(Button.builder(getEnabledText(), button -> {
                    enabled = !enabled;
                    button.setMessage(getEnabledText());
                })
                .bounds(centerX - 100, sliderY + 35, 200, 20)
                .build());


        addRenderableWidget(Button.builder(Component.translatable("gui.done"), button -> onClose())
                .bounds(centerX - 100, sliderY + 65, 200, 20)
                .build());
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor context, int mouseX, int mouseY, float delta) {
        super.extractRenderState(context, mouseX, mouseY, delta);
        context.centeredText(this.font, this.title, this.width / 2, 20, 0xFFFFFFFF);
        context.centeredText(
                this.font,
                Component.translatable("text.feed-assist.description"),
                this.width / 2,
                this.height / 2 - 50,
                0xFFA0A0A0
        );
    }

    @Override
    public void onClose() {
        if (rangeSlider != null) {
            ModConfig.setFeedRange(rangeSlider.getFeedRange());
            ModConfig.setEnabled(enabled);
            ModConfig.save();
        }

        if (minecraft != null) {
            minecraft.setScreen(parent);
        }
    }

    private Component getEnabledText() {
        return Component.translatable(enabled
                ? "text.feed-assist.option.enabled.on"
                : "text.feed-assist.option.enabled.off");
    }

    private static class RangeAbstractSliderButton extends AbstractSliderButton {
        private int feedRange;

        RangeAbstractSliderButton(int x, int y, int width, int height, int initialRange) {
            super(x, y, width, height, Component.empty(), toSliderValue(initialRange));
            setFeedRange(initialRange);
        }

        int getFeedRange() {
            return feedRange;
        }

        @Override
        protected void updateMessage() {
            setMessage(Component.translatable("text.feed-assist.option.range", feedRange));
        }

        @Override
        protected void applyValue() {
            setFeedRange(fromSliderValue(this.value));
        }

        private void setFeedRange(int range) {
            feedRange = Math.max(ModConfig.MIN_FEED_RANGE, Math.min(ModConfig.MAX_FEED_RANGE, range));
            this.value = toSliderValue(feedRange);
            updateMessage();
        }

        private static double toSliderValue(int range) {
            int clamped = Math.max(ModConfig.MIN_FEED_RANGE, Math.min(ModConfig.MAX_FEED_RANGE, range));
            return (double) (clamped - ModConfig.MIN_FEED_RANGE)
                    / (ModConfig.MAX_FEED_RANGE - ModConfig.MIN_FEED_RANGE);
        }

        private static int fromSliderValue(double value) {
            int steps = ModConfig.MAX_FEED_RANGE - ModConfig.MIN_FEED_RANGE;
            return ModConfig.MIN_FEED_RANGE + (int) Math.round(value * steps);
        }
    }
}


