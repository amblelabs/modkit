package dev.amble.lib.client.gui.lua;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface LuaExpose {
    String name() default ""; // optional Lua name override
}
