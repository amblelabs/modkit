package dev.amble.plushies.client;

import dev.amble.lib.animation.client.BedrockBlockEntityRenderer;
import dev.amble.lib.client.bedrock.BedrockEntityModel;
import dev.amble.plushies.MarketablePlushieBlock;
import dev.amble.plushies.MarketablePlushieBlockEntity;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.world.LightType;

public class MarketablePlushieRenderer<T extends MarketablePlushieBlockEntity> extends BedrockBlockEntityRenderer<T> {

    private static final float MAX_SCALE = 3.0f;
    private static final float NORMAL_SCALE = 1.5f;

    public MarketablePlushieRenderer(BlockEntityRendererFactory.Context context) {
        super(context);
    }

    @Override
    public void render(T entity, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay) {
        BlockState state = entity.getCachedState();
        Block block = state.getBlock();

        if (!(block instanceof MarketablePlushieBlock plushieBlock)) return;

        BedrockEntityModel<?> model = plushieBlock.model == null ? plushieBlock.model = refreshModel(entity) : plushieBlock.model;

        BlockState downState = entity.getWorld().getBlockState(entity.getPos().down());
        Block downBlock = downState.getBlock();
        if (downBlock instanceof MarketablePlushieBlock && downState.get(MarketablePlushieBlock.STACKED))
            return;

        matrices.push();
        matrices.translate(0.5D, 0.0D, 0.5D);
        matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(180F));
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(entity.getRenderYaw()));

        boolean stacked = state.get(MarketablePlushieBlock.STACKED);
        float scale = stacked ? MAX_SCALE : NORMAL_SCALE;
        matrices.scale(scale, scale, scale);

        if (entity.getWorld() != null) {
            int sky = entity.getWorld().getLightLevel(LightType.SKY, entity.getPos().up().up());
            light = LightmapTextureManager.pack(0, sky);
        }

        model.setAngles(entity, entity.getAge() + tickDelta);

        model.render(
                matrices,
                vertexConsumers.getBuffer(RenderLayer.getEntityCutoutNoCull(getTexture(entity))),
                light,
                overlay,
                1.0f, 1.0f, 1.0f, 1.0f
        );

        Identifier emission = entity.getEmissionTexture();
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
