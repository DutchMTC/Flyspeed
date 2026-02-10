package com.miyaki;

import net.fabricmc.api.ModInitializer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Flyspeed implements ModInitializer {
	public static final String MOD_ID = "flyspeed";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		LOGGER.info("Flyspeed initialized.");
	}
}
