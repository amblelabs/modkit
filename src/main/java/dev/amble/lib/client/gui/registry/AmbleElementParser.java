package dev.amble.lib.client.gui.registry;

import com.google.gson.JsonObject;
import dev.amble.lib.client.gui.AmbleContainer;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

/**
 * Interface for custom element parsers that can be registered with {@link AmbleGuiRegistry}.
 * <p>
 * Mods can implement this interface to add support for custom GUI element types.
 * When a JSON object is being parsed, all registered parsers are checked in order
 * until one returns a non-null result.
 * <p>
 * Example usage:
 * <pre>{@code
 * // Register a custom parser during mod initialization
 * AmbleGuiRegistry.getInstance().registerParser(new AmbleElementParser() {
 *     @Override
 *     public @Nullable AmbleContainer parse(JsonObject json, @Nullable Identifier resourceId, AmbleContainer base) {
 *         if (json.has("my_custom_type") && json.get("my_custom_type").getAsBoolean()) {
 *             // Create your custom element and copy state from the base container
 *             MyCustomContainer custom = new MyCustomContainer();
 *             custom.copyFrom(base);
 *             // Apply custom properties...
 *             return custom;
 *         }
 *         // Return null to let other parsers handle it
 *         return null;
 *     }
 *
 *     @Override
 *     public int priority() {
 *         return 100; // Higher priority runs first
 *     }
 * });
 * }</pre>
 */
@FunctionalInterface
public interface AmbleElementParser {

    /**
     * Attempts to parse the given JSON object into an AmbleContainer.
     * <p>
     * Implementations should check if the JSON contains properties specific to their
     * custom element type. If it does, parse and return the element. If not, return null
     * to allow other parsers (or the default parser) to handle it.
     * <p>
     * The base container has already been parsed with all standard properties (layout,
     * background, padding, spacing, alignment, children, etc). Custom parsers should
     * use {@link AmbleContainer#copyFrom(AmbleContainer)} to copy this state to their
     * custom element type.
     *
     * @param json       the JSON object to parse
     * @param resourceId the identifier of the resource being parsed (for error context), may be null
     * @param base       the base AmbleContainer already parsed with standard properties
     * @return the parsed AmbleContainer, or null if this parser cannot handle the given JSON
     */
    @Nullable
    AmbleContainer parse(JsonObject json, @Nullable Identifier resourceId, AmbleContainer base);

    /**
     * Returns the priority of this parser. Higher values are checked first.
     * <p>
     * The default parser has a priority of 0. Custom parsers that want to override
     * default behavior should return a positive value.
     *
     * @return the priority of this parser
     */
    default int priority() {
        return 0;
    }
}

