package com.nekodon.feedassist.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class ModConfig {
    public static final int MIN_FEED_RANGE = 2;
    public static final int MAX_FEED_RANGE = 10;
    public static final int DEFAULT_FEED_RANGE = 10;
    public static final boolean DEFAULT_ENABLED = true;

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path CONFIG_PATH = FabricLoader.getInstance().getConfigDir().resolve("feed-assist.json");

    private static int feedRange = DEFAULT_FEED_RANGE;
    private static boolean enabled = DEFAULT_ENABLED;

    public static int getFeedRange() {
        return feedRange;
    }

    public static void setFeedRange(int value) {
        feedRange = clampFeedRange(value);
    }

    public static boolean isEnabled() {
        return enabled;
    }

    public static void setEnabled(boolean value) {
        enabled = value;
    }

    public static void load() {
        if (!Files.exists(CONFIG_PATH)) {
            save();
            return;
        }

        try {
            ConfigData data = GSON.fromJson(Files.readString(CONFIG_PATH), ConfigData.class);
            if (data == null) {
                save();
                return;
            }

            setFeedRange(data.feedRange);
            setEnabled(data.enabled);
            save();
        } catch (IOException | JsonParseException e) {
            e.printStackTrace();
            save();
        }
    }

    public static void save() {
        try {
            Files.createDirectories(CONFIG_PATH.getParent());

            ConfigData data = new ConfigData();
            data.feedRange = feedRange;
            data.enabled = enabled;

            Files.writeString(CONFIG_PATH, GSON.toJson(data));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static int clampFeedRange(int value) {
        return Math.max(MIN_FEED_RANGE, Math.min(MAX_FEED_RANGE, value));
    }

    public static class ConfigData {
        public int feedRange = DEFAULT_FEED_RANGE;
        public boolean enabled = DEFAULT_ENABLED;
    }
}

