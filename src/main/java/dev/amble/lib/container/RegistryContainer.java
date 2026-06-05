package dev.amble.lib.container;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Locale;

import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

import dev.amble.lib.AmbleKit;

public interface RegistryContainer<T> {

    Class<T> getTargetClass();

    /**
     * @return The registry the fields of this class should be registered into
     */
    Registry<T> getRegistry();

    /**
     * Called after the given field has been registered
     *
     * @param namespace  The namespace that is being used to register this class' fields
     * @param value      The value that was registered
     * @param identifier The identifier the field was assigned, possibly overridden by an {@link AssignedName}
     *                   annotation and always fully lowercase
     */
    default void postProcessField(Identifier identifier, T value, Field field) {}

    static <T> void register(Class<? extends RegistryContainer<T>> clazz, String namespace) {
        try {
            RegistryContainer<T> container = clazz.getDeclaredConstructor().newInstance();
            Field[] fields = clazz.getDeclaredFields();

            container.start(fields.length);

            for (Field field : fields) {
                if (!Modifier.isStatic(field.getModifiers()))
                    continue;

                if (!container.getTargetClass().isAssignableFrom(field.getType()))
                    continue;

                // trust me bro
                T v = (T) field.get(null);

                if (v == null)
                    continue;

                String name = field.getName().toLowerCase(Locale.ROOT);

                if (field.isAnnotationPresent(AssignedName.class))
                    name = field.getAnnotation(AssignedName.class).value();

                Identifier id = new Identifier(namespace, name);

                // Skip values that are already present in the target registry.
                // Some external APIs (e.g. TerraformersMC's boat API) register the
                // value themselves, so re-registering the same instance here would
                // throw "Attempted to register object ... twice". See issue #56.
                if (container.getRegistry().getKey(v).isPresent())
                    continue;

                Registry.register(container.getRegistry(), id, v);
                container.postProcessField(id, v, field);
            }

            container.finish();
        } catch (ReflectiveOperationException e) {
            AmbleKit.LOGGER.error("Failed to process a registry container", e);
        }
    }

    @SuppressWarnings({"unchecked"})
    static <T> Class<T> conform(Class<?> input) {
        return (Class<T>) input;
    }

    default void start(int fields) { }

    default void finish() { }
}