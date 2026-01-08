package dev.amble.lib.script;

import org.luaj.vm2.LuaValue;

public record AmbleScript(
        LuaValue onInit,
        LuaValue onClick,
        LuaValue onRelease,
        LuaValue onHover,
        LuaValue onExecute,
        LuaValue onEnable,
        LuaValue onTick,
        LuaValue onDisable
) {}
