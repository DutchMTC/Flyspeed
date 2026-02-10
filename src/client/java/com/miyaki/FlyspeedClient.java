package com.miyaki;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import com.mojang.blaze3d.platform.InputConstants;
import org.lwjgl.glfw.GLFW;
import com.miyaki.client.FlightSpeedController;
import com.miyaki.client.FlyspeedConfig;
import com.miyaki.client.gui.FlyspeedSettingsScreen;
import com.miyaki.client.gui.SurvivalWarningScreen;

public class FlyspeedClient implements ClientModInitializer {
	private static final FlightSpeedController CONTROLLER = new FlightSpeedController();
	private static final FlyspeedConfig CONFIG = FlyspeedConfig.load();
	private static final KeyMapping.Category FLYSPEED_CATEGORY = KeyMapping.Category.register(Identifier.parse("flyspeed:controls"));

	private static KeyMapping openSettingsKey;
	private static KeyMapping toggleSurvivalKey;

	@Override
	public void onInitializeClient() {
		openSettingsKey = KeyBindingHelper.registerKeyBinding(new KeyMapping(
			"key.flyspeed.open_settings",
			InputConstants.Type.KEYSYM,
			GLFW.GLFW_KEY_F8,
			FLYSPEED_CATEGORY
		));

		toggleSurvivalKey = KeyBindingHelper.registerKeyBinding(new KeyMapping(
			"key.flyspeed.toggle_survival_boost",
			InputConstants.Type.KEYSYM,
			InputConstants.UNKNOWN.getValue(),
			FLYSPEED_CATEGORY
		));

		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			if (client.screen == null) {
				while (openSettingsKey.consumeClick()) {
					openSettings(client, client.screen);
				}

				while (toggleSurvivalKey.consumeClick()) {
					toggleSurvivalBoost(client, client.screen);
				}
			}

			CONTROLLER.tick(client, CONFIG);
		});

		HudElementRegistry.addLast(Identifier.parse("flyspeed:boost_indicator"), (drawContext, tickCounter) -> {
			CONTROLLER.renderHud(Minecraft.getInstance(), drawContext, CONFIG);
		});

		Flyspeed.LOGGER.info("Flyspeed client initialized.");
	}

	public static FlyspeedConfig getConfig() {
		return CONFIG;
	}

	public static void openSettings(Minecraft client, Screen parent) {
		if (client == null) {
			return;
		}

		client.setScreen(new FlyspeedSettingsScreen(parent));
	}

	public static void toggleSurvivalBoost(Minecraft client, Screen parent) {
		if (CONFIG.enableSurvivalFlightBoost) {
			CONFIG.enableSurvivalFlightBoost = false;
			saveConfig();
			sendActionBar(client, Component.translatable("message.flyspeed.survival_disabled"));
			return;
		}

		if (CONFIG.warningAcknowledged) {
			CONFIG.enableSurvivalFlightBoost = true;
			saveConfig();
			sendActionBar(client, Component.translatable("message.flyspeed.survival_enabled"));
			return;
		}

		client.setScreen(new SurvivalWarningScreen(parent, confirmed -> {
			if (!confirmed) {
				return;
			}

			CONFIG.warningAcknowledged = true;
			CONFIG.enableSurvivalFlightBoost = true;
			saveConfig();
			sendActionBar(client, Component.translatable("message.flyspeed.survival_enabled"));
		}));
	}

	public static void saveConfig() {
		CONFIG.save();
	}

	public static void sendActionBar(Minecraft client, Component message) {
		if (client == null || client.player == null) {
			return;
		}

		client.player.displayClientMessage(message, true);
	}
}
