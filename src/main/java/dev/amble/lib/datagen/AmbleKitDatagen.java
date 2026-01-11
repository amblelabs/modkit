package dev.amble.lib.datagen;

import dev.amble.lib.datagen.lang.AmbleLanguageProvider;
import dev.amble.lib.datagen.lang.LanguageType;
import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;

public class AmbleKitDatagen implements DataGeneratorEntrypoint {
	@Override
	public void onInitializeDataGenerator(FabricDataGenerator generator) {
		FabricDataGenerator.Pack pack = generator.createPack();
		pack.addProvider(AmbleKitLanguageProvider::new);
	}

	public static class AmbleKitLanguageProvider extends AmbleLanguageProvider {
		public AmbleKitLanguageProvider(FabricDataOutput output) {
			super(output, LanguageType.EN_US);
		}

		@Override
		public void generateTranslations(TranslationBuilder builder) {
			// Skin command translations
			addTranslation("command." + modid + ".skin.error.not_texturable", "Target is not a PlayerSkinTexturable");
			addTranslation("command." + modid + ".skin.error.invalid_target", "Invalid Target");
			addTranslation("command." + modid + ".skin.error.not_disguised", "Target is not disguised.");
			addTranslation("command." + modid + ".skin.cleared", "Cleared skin of %s");
			addTranslation("command." + modid + ".skin.slimness_set", "Set slimness of %s to %s");
			addTranslation("command." + modid + ".skin.set", "Set skin of %s to %s");

			// Server script command translations
			addTranslation("command." + modid + ".script.error.not_found", "Server script '%s' not found");
			addTranslation("command." + modid + ".script.error.no_execute", "Server script '%s' has no onExecute function");
			addTranslation("command." + modid + ".script.error.execute_failed", "Failed to execute server script '%s': %s");
			addTranslation("command." + modid + ".script.error.already_enabled", "Server script '%s' is already enabled");
			addTranslation("command." + modid + ".script.error.enable_failed", "Failed to enable server script '%s'");
			addTranslation("command." + modid + ".script.error.not_enabled", "Server script '%s' is not enabled");
			addTranslation("command." + modid + ".script.error.disable_failed", "Failed to disable server script '%s'");
			addTranslation("command." + modid + ".script.executed", "Executed server script: %s");
			addTranslation("command." + modid + ".script.enabled", "Enabled server script: %s");
			addTranslation("command." + modid + ".script.disabled", "Disabled server script: %s");
			addTranslation("command." + modid + ".script.list.none_enabled", "No server scripts are currently enabled");
			addTranslation("command." + modid + ".script.list.enabled_header", "━━━ Enabled Server Scripts (%s) ━━━");
			addTranslation("command." + modid + ".script.list.none_available", "No server scripts available");
			addTranslation("command." + modid + ".script.list.available_header", "━━━ Available Server Scripts (%s) ━━━");

			// Client script command translations
			addTranslation("command." + modid + ".client_script.error.not_found", "Script '%s' not found");
			addTranslation("command." + modid + ".client_script.error.no_execute", "Script '%s' has no onExecute function");
			addTranslation("command." + modid + ".client_script.error.execute_failed", "Failed to execute script '%s': %s");
			addTranslation("command." + modid + ".client_script.error.already_enabled", "Script '%s' is already enabled");
			addTranslation("command." + modid + ".client_script.error.enable_failed", "Failed to enable script '%s'");
			addTranslation("command." + modid + ".client_script.error.not_enabled", "Script '%s' is not enabled");
			addTranslation("command." + modid + ".client_script.error.disable_failed", "Failed to disable script '%s'");
			addTranslation("command." + modid + ".client_script.executed", "Executed script: %s");
			addTranslation("command." + modid + ".client_script.enabled", "Enabled script: %s");
			addTranslation("command." + modid + ".client_script.disabled", "Disabled script: %s");
			addTranslation("command." + modid + ".client_script.list.none_enabled", "No client scripts are currently enabled");
			addTranslation("command." + modid + ".client_script.list.enabled_header", "━━━ Enabled Client Scripts (%s) ━━━");
			addTranslation("command." + modid + ".client_script.list.none_available", "No client scripts available");
			addTranslation("command." + modid + ".client_script.list.available_header", "━━━ Available Client Scripts (%s) ━━━");

			super.generateTranslations(builder);
		}
	}
}

