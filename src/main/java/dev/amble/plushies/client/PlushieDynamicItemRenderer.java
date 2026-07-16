package dev.amble.plushies.client;

import dev.amble.lib.AmbleKit;
import dev.amble.lib.client.bedrock.BedrockEntityModel;
import dev.amble.lib.client.bedrock.BedrockModelReference;
import dev.amble.plushies.MarketablePlushieBlock;
import net.fabricmc.fabric.api.client.rendering.v1.BuiltinItemRendererRegistry;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.RotationAxis;

public class PlushieDynamicItemRenderer implements BuiltinItemRendererRegistry.DynamicItemRenderer {

    @Override
    public void render(ItemStack stack, ModelTransformationMode mode, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay) {
        if (stack.getItem() instanceof BlockItem blockItem && blockItem.getBlock() instanceof MarketablePlushieBlock plushieBlock) {
            BedrockEntityModel<?> model = plushieBlock.model == null ? plushieBlock.model = refreshModel(plushieBlock) : plushieBlock.model;

            matrices.push();
            matrices.translate(0.5D, 0.0D, 0.5D);
            matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(180F));

            model.render(
                    matrices,
                    vertexConsumers.getBuffer(RenderLayer.getEntityCutoutNoCull(plushieBlock.getTexture())),
                    light,
                    overlay,
                    1.0f, 1.0f, 1.0f, 1.0f
            );

            Identifier emission = plushieBlock.getEmissionTexture();
            if (emission != null) {
                model.render(
                        matrices,
                        vertexConsumers.getBuffer(RenderLayer.getEntityCutoutNoCullZOffset(emission)),
                        LightmapTextureManager.MAX_LIGHT_COORDINATE,
                        overlay,
                        1.0f, 1.0f, 1.0f, 1.0f
                );
            }

            matrices.pop();
        }
    }

    protected BedrockEntityModel<?> refreshModel(MarketablePlushieBlock block) {
        BedrockModelReference ref = block.getModel();
        if (ref == null) {
            throw new IllegalStateException("Block " + block + " does not have a BedrockModelReference");
        }
        return new BedrockEntityModel<>(ref.get().orElseThrow(() ->
                new IllegalStateException("BedrockModel " + ref.id() + " not found for block " + block)));
    }
}
