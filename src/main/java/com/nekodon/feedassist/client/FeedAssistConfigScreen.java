package com.nekodon.feedassist.client;

import com.nekodon.feedassist.config.ModConfig;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.SliderWidget;
import net.minecraft.text.Text;

public class FeedAssistConfigScreen extends Screen {
    private final Screen parent;
    private RangeSliderWidget rangeSlider;
    private boolean enabled;

    public FeedAssistConfigScreen(Screen parent) {
        super(Text.translatable("text.feed-assist.title"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        int centerX = this.width / 2;
        int sliderY = this.height / 2 - 20;
        enabled = ModConfig.isEnabled();

        rangeSlider = new RangeSliderWidget(centerX - 100, sliderY, 200, 20, ModConfig.getFeedRange());
        addDrawableChild(rangeSlider);

        addDrawableChild(ButtonWidget.builder(getEnabledText(), button -> {
                    enabled = !enabled;
                    button.setMessage(getEnabledText());
                })
                .dimensions(centerX - 100, sliderY + 35, 200, 20)
                .build());


        addDrawableChild(ButtonWidget.builder(Text.translatable("gui.done"), button -> close())
                .dimensions(centerX - 100, sliderY + 65, 200, 20)
                .build());
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
        context.drawCenteredTextWithShadow(this.textRenderer, this.title, this.width / 2, 20, 0xFFFFFF);
        context.drawCenteredTextWithShadow(
                this.textRenderer,
                Text.translatable("text.feed-assist.description"),
                this.width / 2,
                this.height / 2 - 50,
                0xA0A0A0
        );
    }

    @Override
    public void close() {
        if (rangeSlider != null) {
            ModConfig.setFeedRange(rangeSlider.getFeedRange());
            ModConfig.setEnabled(enabled);
            ModConfig.save();
        }

        if (client != null) {
            client.setScreen(parent);
        }
    }

    private Text getEnabledText() {
        return Text.translatable(enabled
                ? "text.feed-assist.option.enabled.on"
                : "text.feed-assist.option.enabled.off");
    }

    private static class RangeSliderWidget extends SliderWidget {
        private int feedRange;

        RangeSliderWidget(int x, int y, int width, int height, int initialRange) {
            super(x, y, width, height, Text.empty(), toSliderValue(initialRange));
            setFeedRange(initialRange);
        }

        int getFeedRange() {
            return feedRange;
        }

        @Override
        protected void updateMessage() {
            setMessage(Text.translatable("text.feed-assist.option.range", feedRange));
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

