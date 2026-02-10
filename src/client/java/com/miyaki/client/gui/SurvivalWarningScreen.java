package com.miyaki.client.gui;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.function.Consumer;

public final class SurvivalWarningScreen extends Screen {
	private final Screen parent;
	private final Consumer<Boolean> onResolve;
	private boolean resolved;

	public SurvivalWarningScreen(Screen parent, Consumer<Boolean> onResolve) {
		super(Component.translatable("screen.flyspeed.warning.title"));
		this.parent = parent;
		this.onResolve = onResolve;
	}

	@Override
	protected void init() {
		int panelWidth = 360;
		int panelHeight = 140;
		int left = (this.width - panelWidth) / 2;
		int top = (this.height - panelHeight) / 2;

		this.addRenderableWidget(Button.builder(
			Component.translatable("screen.flyspeed.warning.enable"),
			button -> finish(true)
		).bounds(left + 24, top + 92, 150, 20).build());

		this.addRenderableWidget(Button.builder(
			Component.translatable("screen.flyspeed.warning.cancel"),
			button -> finish(false)
		).bounds(left + panelWidth - 174, top + 92, 150, 20).build());
	}

	@Override
	public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float delta) {
		int panelWidth = 360;
		int panelHeight = 140;
		int left = (this.width - panelWidth) / 2;
		int top = (this.height - panelHeight) / 2;

		guiGraphics.fillGradient(left, top, left + panelWidth, top + panelHeight, 0xDD2B0E12, 0xDD1A0A10);
		guiGraphics.renderOutline(left, top, panelWidth, panelHeight, 0xFFFF6E6E);

		guiGraphics.drawCenteredString(this.font, this.title, this.width / 2, top + 12, 0xFFFFFFFF);
		guiGraphics.drawCenteredString(this.font, Component.translatable("screen.flyspeed.warning.line1"), this.width / 2, top + 40, 0xFFFFD7A0);
		guiGraphics.drawCenteredString(this.font, Component.translatable("screen.flyspeed.warning.line2"), this.width / 2, top + 56, 0xFFFFD7A0);
		guiGraphics.drawCenteredString(this.font, Component.translatable("screen.flyspeed.warning.line3"), this.width / 2, top + 72, 0xFFFFA8A8);

		super.render(guiGraphics, mouseX, mouseY, delta);
	}

	@Override
	public void onClose() {
		finish(false);
	}

	private void finish(boolean confirmed) {
		if (this.resolved) {
			return;
		}

		this.resolved = true;
		this.onResolve.accept(confirmed);
		this.minecraft.setScreen(this.parent);
	}
}
