package dev.amble.lib.client.model;

import net.minecraft.client.render.entity.model.SinglePartEntityModel;
import net.minecraft.entity.Entity;

@SuppressWarnings("rawtypes")
public abstract class BlockEntityModel extends SinglePartEntityModel {

    @Override
    public void setAngles(Entity entity, float limbAngle, float limbDistance, float animationProgress, float headYaw, float headPitch) { }
}
