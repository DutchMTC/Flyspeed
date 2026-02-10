package com.miyaki.client;

import com.miyaki.network.ServerPolicyPayload;
import net.minecraft.client.Minecraft;

public final class ServerPolicyState {
	public enum Status {
		UNKNOWN,
		ALLOWED,
		DISABLED
	}

	private static Status status = Status.UNKNOWN;
	private static boolean multiplayerSession = false;
	private static float maxMultiplier = FlyspeedConfig.MAX_MULTIPLIER;

	private ServerPolicyState() {
	}

	public static void onJoin(Minecraft client) {
		multiplayerSession = client.getSingleplayerServer() == null;
		status = Status.ALLOWED;
		maxMultiplier = FlyspeedConfig.MAX_MULTIPLIER;
	}

	public static void onDisconnect() {
		multiplayerSession = false;
		status = Status.UNKNOWN;
		maxMultiplier = FlyspeedConfig.MAX_MULTIPLIER;
	}

	public static void applyPolicy(ServerPolicyPayload payload) {
		maxMultiplier = FlyspeedConfig.clampMultiplier(payload.maxMultiplier());
		status = payload.allowMovementBoost() ? Status.ALLOWED : Status.DISABLED;
	}

	public static Status getStatus() {
		return status;
	}

	public static boolean allowsBoost() {
		return status != Status.DISABLED;
	}

	public static boolean isWaitingForServerPolicy() {
		return multiplayerSession && status == Status.UNKNOWN;
	}

	public static float getEffectiveMaxMultiplier() {
		if (!allowsBoost()) {
			return FlyspeedConfig.MIN_MULTIPLIER;
		}

		return maxMultiplier;
	}
}
