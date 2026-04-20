package dev.amble.litmus.datagen;

import dev.amble.lib.datagen.lang.AmbleLanguageProvider;
import dev.amble.lib.datagen.lang.LanguageType;
import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;

public class LitmusDatagen implements DataGeneratorEntrypoint {
	@Override
	public void onInitializeDataGenerator(FabricDataGenerator generator) {
		FabricDataGenerator.Pack pack = generator.createPack();
		pack.addProvider(LitmusLanguageProvider::new);
	}

	public static class LitmusLanguageProvider extends AmbleLanguageProvider {
		public LitmusLanguageProvider(FabricDataOutput output) {
			super(output, LanguageType.EN_US);
		}

		@Override
		public void generateTranslations(TranslationBuilder builder) {
			// Test screen command translations
			addTranslation("command." + modid + ".screen.available", "Available screens:");
			addTranslation("command." + modid + ".screen.list_item", " - %s");
			addTranslation("command." + modid + ".screen.not_found", "No screen found with id: %s");

			// GUI translations
			addTranslation("gui." + modid + ".test_button", "press me");

			super.generateTranslations(builder);
		}
	}
}

