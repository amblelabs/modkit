package dev.amble.lib.script.lua;

import dev.amble.lib.client.gui.AmbleElement;
import dev.amble.lib.client.gui.lua.LuaElement;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import org.joml.Vector3f;
import org.luaj.vm2.*;
import org.luaj.vm2.lib.*;
import org.luaj.vm2.lib.jse.CoerceJavaToLua;
import org.luaj.vm2.lib.jse.CoerceLuaToJava;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Binds Java objects to Lua, exposing methods annotated with @LuaExpose.
 * Uses MethodHandles for improved performance over reflection.
 */
public final class LuaBinder {

    private static final MethodHandles.Lookup LOOKUP = MethodHandles.lookup();
    private static final Map<Class<?>, LuaTable> CACHE = new HashMap<>();

    public static LuaValue bind(Object target) {
        LuaTable table = CACHE.computeIfAbsent(
                target.getClass(),
                LuaBinder::buildMetatable
        );

        LuaUserdata userdata = new LuaUserdata(target);
        userdata.setmetatable(table);
        return userdata;
    }

    // ===== Separate coerceResult methods for each type =====

    /**
     * Coerces a null value to Lua NIL.
     */
    public static LuaValue coerceNull() {
        return LuaValue.NIL;
    }

    /**
     * Coerces a String to a Lua string.
     */
    public static LuaValue coerceString(String value) {
        return LuaString.valueOf(value);
    }

    /**
     * Coerces an integer to a Lua number.
     */
    public static LuaValue coerceInt(int value) {
        return LuaInteger.valueOf(value);
    }

    /**
     * Coerces a long to a Lua number.
     */
    public static LuaValue coerceLong(long value) {
        return LuaInteger.valueOf(value);
    }

    /**
     * Coerces a float to a Lua number.
     */
    public static LuaValue coerceFloat(float value) {
        return LuaDouble.valueOf(value);
    }

    /**
     * Coerces a double to a Lua number.
     */
    public static LuaValue coerceDouble(double value) {
        return LuaDouble.valueOf(value);
    }

    /**
     * Coerces a boolean to a Lua boolean.
     */
    public static LuaValue coerceBoolean(boolean value) {
        return LuaBoolean.valueOf(value);
    }

    /**
     * Coerces a List to a Lua table (1-indexed array).
     */
    public static LuaValue coerceList(List<?> list) {
        LuaTable table = new LuaTable();
        for (int i = 0; i < list.size(); i++) {
            table.set(i + 1, coerceResult(list.get(i)));
        }
        return table;
    }

    /**
     * Coerces a Vector3f to a Lua table with x, y, z fields.
     */
    public static LuaValue coerceVector3f(Vector3f vec3) {
        LuaTable table = new LuaTable();
        table.set("x", vec3.x());
        table.set("y", vec3.y());
        table.set("z", vec3.z());
        table.set("toString", new ZeroArgFunction() {
            @Override
            public LuaValue call() {
                return LuaString.valueOf("(" + vec3.x() + ", " + vec3.y() + ", " + vec3.z() + ")");
            }
        });
        return table;
    }

    /**
     * Coerces a Vec3d to a Lua table with x, y, z fields.
     */
    public static LuaValue coerceVec3d(Vec3d vec3) {
        LuaTable table = new LuaTable();
        table.set("x", vec3.x);
        table.set("y", vec3.y);
        table.set("z", vec3.z);
        table.set("toString", new ZeroArgFunction() {
            @Override
            public LuaValue call() {
                return LuaString.valueOf("(" + vec3.x + ", " + vec3.y + ", " + vec3.z + ")");
            }
        });
        return table;
    }

    /**
     * Coerces a BlockPos to a Lua table with x, y, z fields.
     */
    public static LuaValue coerceBlockPos(BlockPos pos) {
        LuaTable table = new LuaTable();
        table.set("x", pos.getX());
        table.set("y", pos.getY());
        table.set("z", pos.getZ());
        table.set("toString", new ZeroArgFunction() {
            @Override
            public LuaValue call() {
                return LuaString.valueOf("(" + pos.getX() + ", " + pos.getY() + ", " + pos.getZ() + ")");
            }
        });
        return table;
    }

    /**
     * Coerces an ItemStack to a bound LuaItemStack.
     */
    public static LuaValue coerceItemStack(ItemStack stack) {
        return bind(new LuaItemStack(stack));
    }

    /**
     * Coerces an Entity to a bound MinecraftEntity.
     */
    public static LuaValue coerceEntity(Entity entity) {
        return bind(new MinecraftEntity(entity));
    }

    /**
     * Coerces an Identifier to a Lua string.
     */
    public static LuaValue coerceIdentifier(Identifier identifier) {
        return LuaString.valueOf(identifier.toString());
    }

    /**
     * Coerces an AmbleElement to a bound LuaElement.
     */
    public static LuaValue coerceAmbleElement(AmbleElement element) {
        if (element instanceof LuaElement luaElement) {
            return bind(luaElement);
        }
        return bind(new LuaElement(element));
    }

    /**
     * Coerces an NbtCompound to a Lua table.
     */
    public static LuaValue coerceNbtCompound(NbtCompound nbt) {
        LuaTable table = new LuaTable();
        for (String key : nbt.getKeys()) {
            NbtElement element = nbt.get(key);
            table.set(key, coerceNbtElement(element));
        }
        return table;
    }

    /**
     * Coerces any NbtElement to the appropriate Lua type.
     */
    public static LuaValue coerceNbtElement(NbtElement element) {
        if (element == null) {
            return LuaValue.NIL;
        }

        return switch (element.getType()) {
            case NbtElement.BYTE_TYPE -> LuaInteger.valueOf(((net.minecraft.nbt.NbtByte) element).byteValue());
            case NbtElement.SHORT_TYPE -> LuaInteger.valueOf(((net.minecraft.nbt.NbtShort) element).shortValue());
            case NbtElement.INT_TYPE -> LuaInteger.valueOf(((net.minecraft.nbt.NbtInt) element).intValue());
            case NbtElement.LONG_TYPE -> LuaInteger.valueOf(((net.minecraft.nbt.NbtLong) element).longValue());
            case NbtElement.FLOAT_TYPE -> LuaDouble.valueOf(((net.minecraft.nbt.NbtFloat) element).floatValue());
            case NbtElement.DOUBLE_TYPE -> LuaDouble.valueOf(((net.minecraft.nbt.NbtDouble) element).doubleValue());
            case NbtElement.STRING_TYPE -> LuaString.valueOf(element.asString());
            case NbtElement.BYTE_ARRAY_TYPE -> {
                byte[] bytes = ((net.minecraft.nbt.NbtByteArray) element).getByteArray();
                LuaTable table = new LuaTable();
                for (int i = 0; i < bytes.length; i++) {
                    table.set(i + 1, LuaInteger.valueOf(bytes[i]));
                }
                yield table;
            }
            case NbtElement.INT_ARRAY_TYPE -> {
                int[] ints = ((net.minecraft.nbt.NbtIntArray) element).getIntArray();
                LuaTable table = new LuaTable();
                for (int i = 0; i < ints.length; i++) {
                    table.set(i + 1, LuaInteger.valueOf(ints[i]));
                }
                yield table;
            }
            case NbtElement.LONG_ARRAY_TYPE -> {
                long[] longs = ((net.minecraft.nbt.NbtLongArray) element).getLongArray();
                LuaTable table = new LuaTable();
                for (int i = 0; i < longs.length; i++) {
                    table.set(i + 1, LuaInteger.valueOf(longs[i]));
                }
                yield table;
            }
            case NbtElement.LIST_TYPE -> {
                NbtList list = (NbtList) element;
                LuaTable table = new LuaTable();
                for (int i = 0; i < list.size(); i++) {
                    table.set(i + 1, coerceNbtElement(list.get(i)));
                }
                yield table;
            }
            case NbtElement.COMPOUND_TYPE -> coerceNbtCompound((NbtCompound) element);
            default -> LuaString.valueOf(element.asString());
        };
    }

    // ===== Main coerceResult dispatcher =====

    /**
     * Coerces any Java object to an appropriate Lua value.
     * Dispatches to type-specific coercion methods.
     */
    public static LuaValue coerceResult(Object obj) {
        if (obj == null) return coerceNull();
        if (obj instanceof LuaValue lv) return lv;
        if (obj instanceof String s) return coerceString(s);
        if (obj instanceof Integer i) return coerceInt(i);
        if (obj instanceof Long l) return coerceLong(l);
        if (obj instanceof Float f) return coerceFloat(f);
        if (obj instanceof Double d) return coerceDouble(d);
        if (obj instanceof Boolean b) return coerceBoolean(b);
        if (obj instanceof Number n) return CoerceJavaToLua.coerce(n);
        if (obj instanceof List<?> list) return coerceList(list);
        if (obj instanceof Vector3f vec3) return coerceVector3f(vec3);
        if (obj instanceof Vec3d vec3) return coerceVec3d(vec3);
        if (obj instanceof BlockPos pos) return coerceBlockPos(pos);
        if (obj instanceof ItemStack stack) return coerceItemStack(stack);
        if (obj instanceof Entity entity) return coerceEntity(entity);
        if (obj instanceof NbtCompound nbt) return coerceNbtCompound(nbt);
        if (obj instanceof NbtElement nbt) return coerceNbtElement(nbt);
        if (obj instanceof Identifier id) return coerceIdentifier(id);
        if (obj instanceof LuaElement luaElement) return bind(luaElement);
        if (obj instanceof AmbleElement element) return bind(new LuaElement(element));

        // Fall back to binding the object
        return bind(obj);
    }

    private static LuaTable buildMetatable(Class<?> clazz) {
        LuaTable meta = new LuaTable();
        LuaTable index = new LuaTable();

        for (Method method : clazz.getMethods()) {
            LuaExpose expose = method.getAnnotation(LuaExpose.class);
            if (expose == null) continue;

            String luaName = expose.name().isEmpty()
                    ? method.getName()
                    : expose.name();

            // Convert Method to MethodHandle for better performance
            MethodHandle handle;
            try {
                handle = LOOKUP.unreflect(method);
            } catch (IllegalAccessException e) {
                throw new RuntimeException("Failed to create MethodHandle for " + method.getName(), e);
            }

            Class<?>[] paramTypes = method.getParameterTypes();
            String methodName = method.getName();

            index.set(luaName, new VarArgFunction() {
                @Override
                public Varargs invoke(Varargs args) {
                    try {
                        LuaValue selfValue = args.arg1();
                        if (!selfValue.isuserdata()) {
                            throw new LuaError("Expected userdata but got " + selfValue.typename());
                        }
                        Object javaSelf = selfValue.touserdata();
                        if (javaSelf == null || !clazz.isInstance(javaSelf)) {
                            throw new LuaError("Expected userdata of type " + clazz.getName() + " but got " + (javaSelf == null ? "null" : javaSelf.getClass().getName()));
                        }

                        // Build argument array: [self, arg1, arg2, ...]
                        Object[] invokeArgs = new Object[paramTypes.length + 1];
                        invokeArgs[0] = javaSelf;

                        for (int i = 0; i < paramTypes.length; i++) {
                            invokeArgs[i + 1] = CoerceLuaToJava.coerce(
                                    args.arg(i + 2),
                                    paramTypes[i]
                            );
                        }

                        Object result = handle.invokeWithArguments(invokeArgs);
                        return LuaBinder.coerceResult(result);
                    } catch (LuaError e) {
                        throw e;
                    } catch (Throwable e) {
                        throw new LuaError("Lua call failed: " + methodName + " " + e);
                    }
                }
            });
        }

        meta.set("__index", index);

        return meta;
    }
}
