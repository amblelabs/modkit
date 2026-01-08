package dev.amble.lib.script.lua;

import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import org.joml.Vector3f;
import org.luaj.vm2.*;
import org.luaj.vm2.lib.*;
import org.luaj.vm2.lib.jse.CoerceJavaToLua;
import org.luaj.vm2.lib.jse.CoerceLuaToJava;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class LuaBinder {

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

	private static LuaValue coerceResult(Object obj) {
		if (obj == null) return LuaValue.NIL;
		if (obj instanceof LuaValue lv) return lv;

		// at language level 21 this would be a switch expression
		if (obj instanceof String
				|| obj instanceof Number
				|| obj instanceof Boolean) {
			return CoerceJavaToLua.coerce(obj);
		} else if (obj instanceof List<?> list) {
			LuaTable table = new LuaTable();
			for (int i = 0; i < list.size(); i++) {
				table.set(i + 1, coerceResult(list.get(i))); // recursive
			}
			return table;
		} else if (obj instanceof Vector3f vec3) {
			LuaTable table = new LuaTable();
			table.set("x", vec3.x());
			table.set("y", vec3.y());
			table.set("z", vec3.z());
			return table;
		} else if (obj instanceof Vec3d vec3) {
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
		} else if (obj instanceof BlockPos pos) {
			LuaTable table = new LuaTable();
			table.set("x", pos.getX());
			table.set("y", pos.getY());
			table.set("z", pos.getZ());
			return table;
		} else if (obj instanceof ItemStack stack) {
			return bind(new LuaItemStack(stack));
		} else if (obj instanceof Entity entity) {
			return bind(new MinecraftEntity(entity));
		}

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
                        Object[] javaArgs = new Object[method.getParameterCount()];

                        for (int i = 0; i < javaArgs.length; i++) {
                            javaArgs[i] = CoerceLuaToJava.coerce(
                                    args.arg(i + 2),
                                    method.getParameterTypes()[i]
                            );
                        }
	                    Object result = method.invoke(javaSelf, javaArgs);
	                    return LuaBinder.coerceResult(result);
                    } catch (Exception e) {
                        throw new LuaError("Lua call failed: " + method.getName() + " " + e);
                    }
                }
            });
        }

        meta.set("__index", index);
        return meta;
    }
}
