package com.miyaki.client.gui;

import com.miyaki.FlyspeedClient;
import com.miyaki.client.FlightSpeedController;
import com.miyaki.client.FlyspeedConfig;
import com.miyaki.client.ServerPolicyState;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public final class FlyspeedSettingsScreen extends Screen {
	private final Screen parent;
	private final FlyspeedConfig config;
	private boolean dirty = false;

	private Button survivalToggleButton;
	private Button hudToggleButton;
	private Button activationModeButton;
	private MultiplierSlider multiplierSlider;

	public FlyspeedSettingsScreen(Screen parent) {
		super(Component.translatable("screen.flyspeed.settings.title"));
		this.parent = parent;
		this.config = FlyspeedClient.getConfig();
	}

	@Override
	protected void init() {
		int panelWidth = panelWidth();
		int panelHeight = panelHeight();
		int left = (this.width - panelWidth) / 2;
		int top = (this.height - panelHeight) / 2;

		this.multiplierSlider = this.addRenderableWidget(new MultiplierSlider(left + 20, top + 58, panelWidth - 40, 20));

		this.survivalToggleButton = this.addRenderableWidget(Button.builder(
			Component.empty(),
			button -> toggleSurvivalMode()
		).bounds(left + 20, top + 86, panelWidth - 40, 20).build());

		this.hudToggleButton = this.addRenderableWidget(Button.builder(
			Component.empty(),
			button -> toggleHud()
		).bounds(left + 20, top + 110, panelWidth - 40, 20).build());

		this.activationModeButton = this.addRenderableWidget(Button.builder(
			Component.empty(),
			button -> toggleActivationMode()
		).bounds(left + 20, top + 136, (panelWidth - 48) / 2, 20).build());

		this.addRenderableWidget(Button.builder(
			Component.translatable("screen.flyspeed.settings.reset"),
			button -> resetToDefaults()
		).bounds(left + 28 + (panelWidth - 48) / 2, top + 136, (panelWidth - 48) / 2, 20).build());

		this.addRenderableWidget(Button.builder(
			Component.translatable("gui.done"),
			button -> onClose()
		).bounds(left + 20, top + panelHeight - 32, panelWidth - 40, 20).build());

		refreshLabels();
	}

	@Override
	public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float delta) {
		int panelWidth = panelWidth();
		int panelHeight = panelHeight();
		int left = (this.width - panelWidth) / 2;
		int top = (this.height - panelHeight) / 2;

		guiGraphics.fillGradient(left, top, left + panelWidth, top + panelHeight, 0xD0162C3A, 0xD00A131B);
		guiGraphics.renderOutline(left, top, panelWidth, panelHeight, 0xFF61D6FF);

		guiGraphics.drawCenteredString(this.font, this.title, this.width / 2, top + 10, 0xFFFFFFFF);
		guiGraphics.drawCenteredString(this.font, Component.translatable("screen.flyspeed.settings.subtitle"), this.width / 2, top + 24, 0xFFCAEFFC);
		guiGraphics.drawCenteredString(this.font, serverPolicyLine(), this.width / 2, top + 38, 0xFFA9E8FF);
		guiGraphics.drawWordWrap(
			this.font,
			Component.translatable("screen.flyspeed.settings.tip"),
			left + 20,
			top + panelHeight - 56,
			panelWidth - 40,
			0xFFFFCF8A
		);

		super.render(guiGraphics, mouseX, mouseY, delta);
	}

	@Override
	public void onClose() {
		commitIfDirty();
		this.minecraft.setScreen(this.parent);
	}

	@Override
	public void removed() {
		commitIfDirty();
	}

	private void toggleSurvivalMode() {
		if (this.config.enableSurvivalFlightBoost) {
			this.config.enableSurvivalFlightBoost = false;
			markDirty();
			FlyspeedClient.sendActionBar(this.minecraft, Component.translatable("message.flyspeed.survival_disabled"));
			refreshLabels();
			return;
		}

		if (this.config.warningAcknowledged) {
			this.config.enableSurvivalFlightBoost = true;
			markDirty();
			FlyspeedClient.sendActionBar(this.minecraft, Component.translatable("message.flyspeed.survival_enabled"));
			refreshLabels();
			return;
		}

		this.minecraft.setScreen(new SurvivalWarningScreen(this, confirmed -> {
			if (!confirmed) {
				refreshLabels();
				return;
			}

			this.config.warningAcknowledged = true;
			this.config.enableSurvivalFlightBoost = true;
			markDirty();
			FlyspeedClient.sendActionBar(this.minecraft, Component.translatable("message.flyspeed.survival_enabled"));
			refreshLabels();
		}));
	}

	private void toggleHud() {
		this.config.showHudIndicator = !this.config.showHudIndicator;
		markDirty();
		refreshLabels();
	}

	private void toggleActivationMode() {
		this.config.cycleActivationMode();
		markDirty();
		refreshLabels();
	}

	private void resetToDefaults() {
		this.config.resetToDefaults();
		this.multiplierSlider.syncFromConfig();
		markDirty();
		refreshLabels();
	}

	private void refreshLabels() {
		Component enabledState = this.config.enableSurvivalFlightBoost
			? Component.translatable("screen.flyspeed.settings.state.enabled")
			: Component.translatable("screen.flyspeed.settings.state.disabled");
		Component hudState = this.config.showHudIndicator
			? Component.translatable("screen.flyspeed.settings.state.on")
			: Component.translatable("screen.flyspeed.settings.state.off");

		this.survivalToggleButton.setMessage(Component.translatable("screen.flyspeed.settings.survival_toggle", enabledState));
		this.hudToggleButton.setMessage(Component.translatable("screen.flyspeed.settings.hud_toggle", hudState));
		this.activationModeButton.setMessage(Component.translatable(
			"screen.flyspeed.settings.activation_mode",
			switch (this.config.activationMode) {
				case HOLD -> Component.translatable("screen.flyspeed.settings.mode.hold");
				case TOGGLE -> Component.translatable("screen.flyspeed.settings.mode.toggle");
				case ALWAYS -> Component.translatable("screen.flyspeed.settings.mode.always");
			}
		));
	}

	private void markDirty() {
		this.dirty = true;
	}

	private void commitIfDirty() {
		if (!this.dirty) {
			return;
		}

		FlyspeedClient.saveConfig();
		this.dirty = false;
	}

	private Component serverPolicyLine() {
		Component state = switch (ServerPolicyState.getStatus()) {
			case UNKNOWN -> Component.translatable("screen.flyspeed.settings.server_policy.waiting");
			case ALLOWED -> Component.translatable(
				"screen.flyspeed.settings.server_policy.allowed",
				FlightSpeedController.formatMultiplier(ServerPolicyState.getEffectiveMaxMultiplier())
			);
			case DISABLED -> Component.translatable("screen.flyspeed.settings.server_policy.disabled");
		};

		return Component.translatable("screen.flyspeed.settings.server_policy", state);
	}

	private int panelWidth() {
		return Math.min(360, this.width - 24);
	}

	private int panelHeight() {
		return 220;
	}

	private final class MultiplierSlider extends AbstractSliderButton {
		private MultiplierSlider(int x, int y, int width, int height) {
			super(x, y, width, height, Component.empty(), toSliderValue(config.multiplier));
			updateMessage();
		}

		private static double toSliderValue(float multiplier) {
			return (FlyspeedConfig.clampMultiplier(multiplier) - FlyspeedConfig.MIN_MULTIPLIER)
				/ (FlyspeedConfig.MAX_MULTIPLIER - FlyspeedConfig.MIN_MULTIPLIER);
		}

		private static float fromSliderValue(double value) {
			float multiplier = FlyspeedConfig.MIN_MULTIPLIER
				+ (float) value * (FlyspeedConfig.MAX_MULTIPLIER - FlyspeedConfig.MIN_MULTIPLIER);
			return FlyspeedConfig.clampMultiplier(multiplier);
		}

		private void syncFromConfig() {
			this.value = toSliderValue(config.multiplier);
			updateMessage();
		}

		@Override
		protected void updateMessage() {
			this.setMessage(Component.translatable(
				"screen.flyspeed.settings.multiplier",
				FlightSpeedController.formatMultiplier(config.multiplier)
			));
		}

		@Override
		protected void applyValue() {
			config.multiplier = fromSliderValue(this.value);
			markDirty();
			updateMessage();
		}
	}
}
