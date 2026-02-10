package com.miyaki;

import com.miyaki.network.ServerPolicyPayload;
import com.miyaki.server.FlyspeedServerConfig;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Flyspeed implements ModInitializer {
	public static final String MOD_ID = "flyspeed";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
	private FlyspeedServerConfig serverConfig;

	@Override
	public void onInitialize() {
		PayloadTypeRegistry.playS2C().register(ServerPolicyPayload.TYPE, ServerPolicyPayload.STREAM_CODEC);
		this.serverConfig = FlyspeedServerConfig.load();

		ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
			if (!ServerPlayNetworking.canSend(handler, ServerPolicyPayload.TYPE)) {
				return;
			}

			sender.sendPacket(new ServerPolicyPayload(this.serverConfig.allowMovementBoost, this.serverConfig.maxMultiplier));
		});

		LOGGER.info(
			"Flyspeed initialized. Server policy: allowMovementBoost={}, maxMultiplier={}",
			this.serverConfig.allowMovementBoost,
			this.serverConfig.maxMultiplier
		);
	}
}
