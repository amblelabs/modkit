package dev.amble.lib.datagen.model;

import java.util.*;

import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricModelProvider;

import net.minecraft.block.Block;
import net.minecraft.data.client.*;
import net.minecraft.item.Item;
import net.minecraft.util.Identifier;

import dev.amble.lib.AmbleKit;
import dev.amble.lib.container.impl.BlockContainer;
import dev.amble.lib.container.impl.ItemContainer;
import dev.amble.lib.datagen.util.AutomaticModel;
import dev.amble.lib.util.ReflectionUtil;

public class AmbleModelProvider extends FabricModelProvider {

    protected final FabricDataOutput output;

    protected List<Class<? extends BlockContainer>> blockClass = new ArrayList<>();
    protected Queue<Class<? extends ItemContainer>> itemClass = new LinkedList<>();

    public AmbleModelProvider(FabricDataOutput output) {
        super(output);

        this.output = output;
    }

    @Override
    public void generateBlockStateModels(BlockStateModelGenerator generator) {
        this.blockClass.forEach(clazz -> {
            ReflectionUtil.getAnnotatedValues(clazz, Block.class, AutomaticModel.class, false).forEach((block, annotation) -> {
                if (!annotation.orElseThrow().justItem()) {
                    generator.registerSimpleCubeAll(block);
                }
            });
        });
    }

    @Override
    public void generateItemModels(ItemModelGenerator generator) {
        this.blockClass.forEach(clazz -> {
            ReflectionUtil.getAnnotatedValues(clazz, Block.class, AutomaticModel.class, false).forEach((block, annotation) -> {
                if (annotation.orElseThrow().justItem()) {
                    registerItem(generator, block.asItem(), output.getModId());
                }
            });
        });

        this.itemClass.forEach(clazz -> {
            ReflectionUtil.getAnnotatedValues(clazz, Item.class, AutomaticModel.class, false).forEach((item, annotation) -> {
                registerItem(generator, item, output.getModId());
            });
        });
    }

    @SafeVarargs
    public final AmbleModelProvider withBlocks(Class<? extends BlockContainer>... blockClass) {
        // add all to queue
        this.blockClass.addAll(Arrays.asList(blockClass));

        return this;
    }

    @SafeVarargs
    public final AmbleModelProvider withItems(Class<? extends ItemContainer>... itemClass) {
        // add all to queue
        this.itemClass.addAll(Arrays.asList(itemClass));

        return this;
    }

    private static Model item(String modid, String parent, TextureKey... requiredTextureKeys) {
        return new Model(Optional.of(new Identifier(modid, "item/" + parent)), Optional.empty(), requiredTextureKeys);
    }

    private static Model item(TextureKey... requiredTextureKeys) {
        return item("minecraft", "generated", requiredTextureKeys);
    }

    private void registerItem(ItemModelGenerator generator, Item item, String modid) {
        Model model = item(TextureKey.LAYER0);
        model.upload(ModelIds.getItemModelId(item), createTextureMap(item, modid), generator.writer);
    }

    private TextureMap createTextureMap(Item item, String modid) {
        Identifier texture = new Identifier(modid, "item/" + getItemName(item));
        if (!(doesTextureExist(texture))) {
            texture = AmbleKit.id("item/error");
        }

        return new TextureMap().put(TextureKey.LAYER0, texture);
    }

    private static String getItemName(Item item) {
        return item.getTranslationKey().split("\\.")[2];
    }

    public boolean doesTextureExist(Identifier texture) {
        return this.output.getModContainer().findPath("assets/" + texture.getNamespace() + "/textures/" + texture.getPath() + ".png").isPresent();
    }
}
