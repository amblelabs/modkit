package dev.drtheo.yaar.behavior;

import dev.drtheo.yaar.event.TEvents;
import dev.drtheo.yaar.event.TEventsRegistry;
import org.jetbrains.annotations.Contract;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.function.Supplier;

/**
 * Registry for all {@link TBehavior}s.
 * All elements must be registered <i>after</i> {@link TEventsRegistry} has been frozen.
 * @author DrTheodor (DrTheo_)
 */
public class TBehaviorRegistry {

    private static boolean frozen;
    private static final Set<TBehavior> handlers = Collections.newSetFromMap(new IdentityHashMap<>());

    /**
     * Registers the provided behavior class instance.
     *
     * @param behavior the behavior to register.
     * @throws IllegalStateException if the registry is already frozen <i>or</i> if the {@link TEventsRegistry} was not frozen yet.
     */
    @Contract(pure = true)
    public static void register(TBehavior behavior) {
        if (frozen)
            throw new IllegalStateException("Registry already frozen!");

        if (!TEventsRegistry.isFrozen())
            throw new IllegalStateException("Events registry was not frozen yet!");

        handlers.add(behavior);
        buildEvents(behavior);
    }

    /**
     * A fancy of registering behaviors without instantiating them yourself.
     *
     * @param behavior the consumer for creating the behavior, gets executed immediately.
     */
    @Contract(pure = true)
    public static void register(Supplier<TBehavior> behavior) {
        register(behavior.get());
    }

    /**
     * Freezes the registry and fixes all the {@link Resolve}-annotated fields.
     */
    @Contract(pure = true)
    public static void freeze() {
        frozen = true;

        Map<Class<?>, TBehavior> lookup = new HashMap<>();

        for (TBehavior handler : handlers) {
            lookup.put(handler.getClass(), handler);
        }

        for (TBehavior handler : handlers) {
            Class<? extends TBehavior> clazz = handler.getClass();

            for (Field field : clazz.getDeclaredFields()) {
                field.setAccessible(true);

                if (!Modifier.isFinal(field.getModifiers()))
                    continue;

                Resolve resolve = field.getAnnotation(Resolve.class);

                if (resolve == null)
                    continue;

                Class<?> target = field.getType();

                try {
                    field.set(handler, lookup.get(target));
                } catch (IllegalAccessException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    private static void buildEvents(TBehavior handler) {
        for (TEvents.BaseType<?> holder : TEventsRegistry.registered()) {
            if (!holder.isApplicable(handler))
                continue;

            holder.subscribe(handler);
        }
    }
}
