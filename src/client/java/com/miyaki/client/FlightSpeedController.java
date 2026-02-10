package com.miyaki.client;

import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Abilities;
import org.lwjgl.glfw.GLFW;

import java.util.Locale;

public final class FlightSpeedController {
	private static final float DEFAULT_FLY_SPEED = 0.05f;
	private static final float MAX_FLY_SPEED = 2.0f;

	private float normalFlySpeed = DEFAULT_FLY_SPEED;
	private float hudAlpha = 0.0f;
	private float displayedMultiplier = FlyspeedConfig.MIN_MULTIPLIER;
	private boolean boosted = false;
	private boolean sprintWasDown = false;
	private boolean toggleActive = false;
	private boolean restoreSprintState = false;

	public void tick(Minecraft client, FlyspeedConfig config) {
		boolean sprintDown = isSprintKeyPhysicallyDown(client);
		LocalPlayer player = client.player;
		if (player == null) {
			clearTransientState();
			animateHud(false, config.multiplier);
			sprintWasDown = sprintDown;
			return;
		}

		Abilities abilities = player.getAbilities();
		boolean canUseFlightBoost = abilities.flying && !player.isSpectator() && (abilities.instabuild || config.enableSurvivalFlightBoost);
		switch (config.activationMode) {
			case HOLD -> toggleActive = false;
			case TOGGLE -> {
				if (canUseFlightBoost && client.screen == null && sprintDown && !sprintWasDown) {
					toggleActive = !toggleActive;
				}
			}
		}

		if (!canUseFlightBoost) {
			toggleActive = false;
		}

		boolean activationRequested = switch (config.activationMode) {
			case HOLD -> sprintDown;
			case TOGGLE -> toggleActive;
			case ALWAYS -> true;
		};
		boolean shouldBoost = canUseFlightBoost && activationRequested && config.multiplier > FlyspeedConfig.MIN_MULTIPLIER;

		if (!shouldBoost) {
			if (boosted) {
				restoreNormalState(player, abilities);
			} else {
				normalFlySpeed = abilities.getFlyingSpeed();
			}

			animateHud(false, config.multiplier);
			sprintWasDown = sprintDown;
			return;
		}

		if (!boosted) {
			normalFlySpeed = abilities.getFlyingSpeed();
			restoreSprintState = player.isSprinting();
		}

		float boostedSpeed = Math.min(MAX_FLY_SPEED, normalFlySpeed * config.multiplier);
		if (Math.abs(abilities.getFlyingSpeed() - boostedSpeed) > 0.0001f) {
			abilities.setFlyingSpeed(boostedSpeed);
		}

		boosted = true;
		displayedMultiplier = config.multiplier;
		animateHud(config.showHudIndicator, config.multiplier);
		if (restoreSprintState && !player.isSprinting()) {
			player.setSprinting(true);
		}
		sprintWasDown = sprintDown;
	}

	public void renderHud(Minecraft client, GuiGraphics guiGraphics, FlyspeedConfig config) {
		if (!config.showHudIndicator || hudAlpha <= 0.01f || client.font == null) {
			return;
		}

		Component label = Component.translatable("hud.flyspeed.active", formatMultiplier(displayedMultiplier));
		int width = client.getWindow().getGuiScaledWidth();
		int x = (width - client.font.width(label)) / 2;
		int y = 16;
		int alpha = (int) (hudAlpha * 255.0f) & 0xFF;
		int color = (alpha << 24) | 0x8EF4FF;

		guiGraphics.drawString(client.font, label, x, y, color, true);
	}

	private void restoreNormalState(LocalPlayer player, Abilities abilities) {
		abilities.setFlyingSpeed(normalFlySpeed);
		boosted = false;
		if (restoreSprintState && !player.isSprinting()) {
			player.setSprinting(true);
		}
		restoreSprintState = false;
	}

	private void clearTransientState() {
		boosted = false;
		normalFlySpeed = DEFAULT_FLY_SPEED;
		sprintWasDown = false;
		toggleActive = false;
		restoreSprintState = false;
	}

	private void animateHud(boolean shouldShow, float multiplier) {
		float targetAlpha = shouldShow ? 1.0f : 0.0f;
		hudAlpha += (targetAlpha - hudAlpha) * 0.2f;
		displayedMultiplier = multiplier;
	}

	private boolean isSprintKeyPhysicallyDown(Minecraft client) {
		InputConstants.Key sprintKey = KeyBindingHelper.getBoundKeyOf(client.options.keySprint);
		InputConstants.Type type = sprintKey.getType();

		if (type == InputConstants.Type.KEYSYM) {
			return InputConstants.isKeyDown(client.getWindow(), sprintKey.getValue());
		}

		if (type == InputConstants.Type.MOUSE) {
			return GLFW.glfwGetMouseButton(client.getWindow().handle(), sprintKey.getValue()) == GLFW.GLFW_PRESS;
		}

		return client.options.keySprint.isDown();
	}

	public static String formatMultiplier(float multiplier) {
		return String.format(Locale.ROOT, "%.1f", multiplier);
	}
}
