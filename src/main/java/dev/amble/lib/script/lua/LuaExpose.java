package dev.amble.lib.script.lua;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface LuaExpose {
    String name() default ""; // optional Lua name override
}
