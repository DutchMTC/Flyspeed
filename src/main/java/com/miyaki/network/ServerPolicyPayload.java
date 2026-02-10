package com.miyaki.network;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record ServerPolicyPayload(boolean allowMovementBoost, float maxMultiplier) implements CustomPacketPayload {
	public static final Type<ServerPolicyPayload> TYPE = new Type<>(Identifier.parse("flyspeed:server_policy"));
	public static final StreamCodec<RegistryFriendlyByteBuf, ServerPolicyPayload> STREAM_CODEC = StreamCodec.composite(
		ByteBufCodecs.BOOL,
		ServerPolicyPayload::allowMovementBoost,
		ByteBufCodecs.FLOAT,
		ServerPolicyPayload::maxMultiplier,
		ServerPolicyPayload::new
	);

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}
