package com.miyaki.client;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import com.miyaki.Flyspeed;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;

public final class FlyspeedConfig {
	public enum ActivationMode {
		HOLD,
		TOGGLE,
		ALWAYS
	}

	public static final float MIN_MULTIPLIER = 1.0f;
	public static final float MAX_MULTIPLIER = 20.0f;
	private static final float DEFAULT_MULTIPLIER = 6.0f;

	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
	private static final Path CONFIG_PATH = FabricLoader.getInstance().getConfigDir().resolve("flyspeed.json");

	public float multiplier = DEFAULT_MULTIPLIER;
	public boolean enableSurvivalFlightBoost = false;
	public boolean showHudIndicator = true;
	public boolean warningAcknowledged = false;
	public ActivationMode activationMode = ActivationMode.HOLD;

	public static FlyspeedConfig load() {
		if (!Files.exists(CONFIG_PATH)) {
			FlyspeedConfig defaults = new FlyspeedConfig();
			defaults.save();
			return defaults;
		}

		try (Reader reader = Files.newBufferedReader(CONFIG_PATH)) {
			JsonObject root = JsonParser.parseReader(reader).getAsJsonObject();
			FlyspeedConfig loaded = GSON.fromJson(root, FlyspeedConfig.class);
			if (loaded == null) {
				throw new JsonParseException("Config content was empty");
			}

			// Migrate legacy configs that stored boolean toggleActivationMode.
			if (!root.has("activationMode") && root.has("toggleActivationMode") && root.get("toggleActivationMode").getAsBoolean()) {
				loaded.activationMode = ActivationMode.TOGGLE;
			}

			loaded.normalize();
			return loaded;
		} catch (IOException | JsonParseException exception) {
			Flyspeed.LOGGER.warn("Failed to load Flyspeed config at {}. Reverting to defaults.", CONFIG_PATH, exception);
			FlyspeedConfig defaults = new FlyspeedConfig();
			defaults.save();
			return defaults;
		}
	}

	public static float clampMultiplier(float value) {
		return Math.max(MIN_MULTIPLIER, Math.min(MAX_MULTIPLIER, value));
	}

	public void resetToDefaults() {
		this.multiplier = DEFAULT_MULTIPLIER;
		this.enableSurvivalFlightBoost = false;
		this.showHudIndicator = true;
		this.warningAcknowledged = false;
		this.activationMode = ActivationMode.HOLD;
	}

	public void save() {
		try {
			normalize();
			Files.createDirectories(CONFIG_PATH.getParent());
			try (Writer writer = Files.newBufferedWriter(CONFIG_PATH)) {
				GSON.toJson(this, writer);
			}
		} catch (IOException exception) {
			Flyspeed.LOGGER.error("Failed to save Flyspeed config at {}", CONFIG_PATH, exception);
		}
	}

	public void cycleActivationMode() {
		this.activationMode = switch (this.activationMode) {
			case HOLD -> ActivationMode.TOGGLE;
			case TOGGLE -> ActivationMode.ALWAYS;
			case ALWAYS -> ActivationMode.HOLD;
		};
	}

	private void normalize() {
		this.multiplier = clampMultiplier(this.multiplier);
		if (this.activationMode == null) {
			this.activationMode = ActivationMode.HOLD;
		}
	}
}
