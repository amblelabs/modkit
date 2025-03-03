package dev.amble.lib.test;

import dev.amble.lib.AmbleKit;
import net.fabricmc.api.ModInitializer;
import org.jetbrains.annotations.TestOnly;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@TestOnly
public class KitTestMod implements ModInitializer {
	public static final String MOD_ID = "amblekit-test";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		if (!(AmbleKit.isTestingEnabled())) return;

		LOGGER.info("AmbleKit Tests Enabled!");
	}
}
