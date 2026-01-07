package dev.amble.lib.datagen.lang;

import java.util.HashMap;
import java.util.Set;

import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricLanguageProvider;

import net.minecraft.block.Block;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.text.TranslatableTextContent;

import dev.amble.lib.container.RegistryContainer;
import dev.amble.lib.container.impl.BlockContainer;
import dev.amble.lib.container.impl.ItemContainer;
import dev.amble.lib.datagen.util.NoEnglish;
import dev.amble.lib.util.ReflectionUtil;

public class AmbleLanguageProvider extends FabricLanguageProvider {
    private final FabricDataOutput output;
    protected final String modid;
    protected HashMap<String, String> translations = new HashMap<>();
    public LanguageType language;

    public AmbleLanguageProvider(FabricDataOutput output, LanguageType language) {
        super(output, language.name().toLowerCase());
        this.output = output;
        this.language = language;
        this.modid = output.getModId();
    }

    @Override
    public void generateTranslations(TranslationBuilder builder) {
        for (String key : translations.keySet()) {
            builder.add(key, translations.get(key));
        }

        output.getModContainer()
                .findPath("assets/" + modid + "/lang/" + language.name().toLowerCase() + ".existing.json")
                .ifPresent(existingFilePath -> {
                    try {
                        builder.add(existingFilePath);
                    } catch (Exception e) {
                        LOGGER.warn("Failed to add existing language file! ({}) | ", language.name().toLowerCase(),
                                e);
                    }
                });
    }

    /**
     * Adds a translation to the language file.
     *
     * @param item
     *            The item to add the translation for.
     * @param translation
     *            The translation.
     */
    public void addTranslation(Item item, String translation) {
        translations.put(item.getTranslationKey(), translation);
    }

    /**
     * Adds a translation to the language file.
     *
     * @param itemGroup
     *            The item group to add the translation for.
     * @param translation
     *            The translation.
     */
    public void addTranslation(ItemGroup itemGroup, String translation) {
        if (!(itemGroup.getDisplayName().getContent() instanceof TranslatableTextContent translatable))
            return;

        translations.put(translatable.getKey(), translation);
    }

    /**
     * Adds a translation to the language file.
     *
     * @param key
     *            The key to add the translation for.
     * @param translation
     *            The translation.
     */
    public void addTranslation(String key, String translation) {
        translations.put(key, translation);
    }

    /**
     * Adds a translation to the language file
     *
     * @param block
     *            The block to add the translation for
     * @param translation
     *            The translation
     */
    public void addTranslation(Block block, String translation) {
        translations.put(block.getTranslationKey(), translation);
    }

    public <T, R extends RegistryContainer<T>> void addTranslation(Class<R> containerClazz, Class<T> valueClazz, Translator<T> translator) {
        Set<T> values = ReflectionUtil.getAnnotatedValues(containerClazz, valueClazz, NoEnglish.class, true).keySet();

        for (T value : values) {
            translator.addTranslation(this, value);
        }
    }
    public void translateItems(Class<? extends ItemContainer> container) {
        addTranslation(container, Item.class, ((provider, value) -> {
            if (value instanceof BlockItem) return;

            provider.addTranslation(value, getNameFromKey(value.getTranslationKey()));
        }));
    }
    public void translateBlocks(Class<? extends BlockContainer> container) {
        addTranslation(container, Block.class, ((provider, value) -> provider.addTranslation(value, getNameFromKey(value.getTranslationKey()))));
    }

    @FunctionalInterface
    public interface Translator<T> {
        void addTranslation(AmbleLanguageProvider provider, T value);
    }

    public static String getNameFromKey(String key) {
        // seperate at last .
        int lastDot = key.lastIndexOf('.');
        if (lastDot == -1) {
            return key;
        }
        String suffix = key.substring(lastDot + 1);

        // split at _
        String[] parts = suffix.split("_");

        // capitalise beginning of each string and join with space
        StringBuilder builder = new StringBuilder();
        for (String part : parts) {
            builder.append(part.substring(0, 1).toUpperCase());
            builder.append(part.substring(1));
            builder.append(" ");
        }

        // remove last space
        builder.deleteCharAt(builder.length() - 1);

        return builder.toString();
    }
}