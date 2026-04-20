package dev.amble.lib.script;

import org.luaj.vm2.Globals;
import org.luaj.vm2.LuaValue;

/**
 * Represents a loaded Lua script with its globals.
 * <p>
 * All callbacks are looked up from globals on demand.
 */
public record LuaScript(Globals globals) {

    /**
     * Gets a callback by name.
     * Returns NIL if the callback is not defined.
     */
    public LuaValue getCallback(String name) {
        return globals.get(name);
    }

    // ===== Core lifecycle callbacks =====

    /**
     * Called when the script is registered to the ScriptManager.
     */
    public LuaValue onRegister() {
        return getCallback("onRegister");
    }

    public LuaValue onExecute() {
        return getCallback("onExecute");
    }

    public LuaValue onEnable() {
        return getCallback("onEnable");
    }

    public LuaValue onTick() {
        return getCallback("onTick");
    }

    public LuaValue onDisable() {
        return getCallback("onDisable");
    }

    // ===== GUI-specific callbacks =====

    /**
     * Called when the script is attached to a GUI element (during JSON parsing).
     * The GUI tree is NOT fully built at this point.
     */
    public LuaValue onAttached() {
        return getCallback("onAttached");
    }

    /**
     * Called when the GUI is first displayed and the GUI tree is fully built.
     * Use this for operations that need to traverse the GUI tree.
     */
    public LuaValue onDisplay() {
        return getCallback("onDisplay");
    }

    public LuaValue onClick() {
        return getCallback("onClick");
    }

    public LuaValue onRelease() {
        return getCallback("onRelease");
    }

    public LuaValue onHover() {
        return getCallback("onHover");
    }
}
