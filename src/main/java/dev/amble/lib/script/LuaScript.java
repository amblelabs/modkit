package dev.amble.lib.script;

import org.luaj.vm2.LuaValue;

/**
 * Represents a loaded Lua script with its lifecycle callback functions.
 * Scripts can define any of these callback functions to handle various events.
 */
public record LuaScript(
        LuaValue onInit,
        LuaValue onClick,
        LuaValue onRelease,
        LuaValue onHover,
        LuaValue onExecute,
        LuaValue onEnable,
        LuaValue onTick,
        LuaValue onDisable
) {}
