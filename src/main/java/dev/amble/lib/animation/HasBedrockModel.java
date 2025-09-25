package dev.amble.lib.animation;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Marks a field as having a bedrock model, which will automatically register the renderer for it
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface HasBedrockModel {

}
