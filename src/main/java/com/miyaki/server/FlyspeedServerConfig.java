package com.miyaki.server;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import com.miyaki.Flyspeed;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;

public final class FlyspeedServerConfig {
	public static final float MIN_MULTIPLIER = 1.0f;
	public static final float MAX_MULTIPLIER = 20.0f;

	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
	private static final Path CONFIG_PATH = FabricLoader.getInstance().getConfigDir().resolve("flyspeed-server.json");

	public boolean allowMovementBoost = true;
	public float maxMultiplier = MAX_MULTIPLIER;

	public static FlyspeedServerConfig load() {
		if (!Files.exists(CONFIG_PATH)) {
			FlyspeedServerConfig defaults = new FlyspeedServerConfig();
			defaults.save();
			return defaults;
		}

		try (Reader reader = Files.newBufferedReader(CONFIG_PATH)) {
			FlyspeedServerConfig loaded = GSON.fromJson(JsonParser.parseReader(reader), FlyspeedServerConfig.class);
			if (loaded == null) {
				throw new JsonParseException("Config content was empty");
			}

			loaded.normalize();
			return loaded;
		} catch (IOException | JsonParseException exception) {
			Flyspeed.LOGGER.warn("Failed to load Flyspeed server config at {}. Reverting to defaults.", CONFIG_PATH, exception);
			FlyspeedServerConfig defaults = new FlyspeedServerConfig();
			defaults.save();
			return defaults;
		}
	}

	public static float clampMultiplier(float value) {
		return Math.max(MIN_MULTIPLIER, Math.min(MAX_MULTIPLIER, value));
	}

	public void save() {
		try {
			normalize();
			Files.createDirectories(CONFIG_PATH.getParent());
			try (Writer writer = Files.newBufferedWriter(CONFIG_PATH)) {
				GSON.toJson(this, writer);
			}
		} catch (IOException exception) {
			Flyspeed.LOGGER.error("Failed to save Flyspeed server config at {}", CONFIG_PATH, exception);
		}
	}

	private void normalize() {
		this.maxMultiplier = clampMultiplier(this.maxMultiplier);
	}
}
