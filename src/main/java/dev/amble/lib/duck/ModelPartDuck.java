package dev.amble.lib.duck;

import net.minecraft.client.model.ModelPart;

import java.util.Map;

/**
 * Duck interface for accessing ModelPart's private children map.
 * Cast ModelPart instances to this interface to access the children.
 */
public interface ModelPartDuck {
	Map<String, ModelPart> amblekit$getChildren();
}

