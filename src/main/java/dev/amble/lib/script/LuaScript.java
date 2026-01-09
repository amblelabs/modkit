package dev.amble.lib.script;

import org.luaj.vm2.Globals;
import org.luaj.vm2.LuaValue;

/**
 * Represents a loaded Lua script with its lifecycle callback functions.
 * <p>
 * Core lifecycle callbacks (onInit, onExecute, onEnable, onTick, onDisable) are stored directly.
 * GUI-specific callbacks (onClick, onRelease, onHover) are looked up from globals on demand
 * to keep this record focused on script lifecycle rather than GUI concerns.
 */
public record LuaScript(
        Globals globals,
        LuaValue onInit,
        LuaValue onExecute,
        LuaValue onEnable,
        LuaValue onTick,
        LuaValue onDisable
) {
    /**
     * Gets a GUI callback by name (onClick, onRelease, onHover).
     * Returns NIL if the callback is not defined.
     */
    public LuaValue getGuiCallback(String name) {
        return globals.get(name);
    }

    public LuaValue onClick() {
        return getGuiCallback("onClick");
    }

    public LuaValue onRelease() {
        return getGuiCallback("onRelease");
    }

    public LuaValue onHover() {
        return getGuiCallback("onHover");
    }
}
