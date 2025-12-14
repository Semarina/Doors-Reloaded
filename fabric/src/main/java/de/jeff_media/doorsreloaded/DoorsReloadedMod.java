package de.jeff_media.doorsreloaded;

import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DoorsReloadedMod implements ModInitializer {
	public static final String MOD_ID = "doorsreloaded";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		LOGGER.info("DoorsReloaded is initializing...");
		de.jeff_media.doorsreloaded.config.ModConfig.load();
		de.jeff_media.doorsreloaded.listeners.InteractionListener.register();
		de.jeff_media.doorsreloaded.scheduler.DoorScheduler.register();

		// Check for updates on Modrinth
		de.jeff_media.doorsreloaded.utils.UpdateChecker.checkForUpdates();
	}
}
