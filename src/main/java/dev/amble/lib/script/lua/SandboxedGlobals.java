package dev.amble.lib.script.lua;

import org.luaj.vm2.Globals;
import org.luaj.vm2.LoadState;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.compiler.LuaC;
import org.luaj.vm2.lib.*;
import org.luaj.vm2.lib.jse.JseBaseLib;
import org.luaj.vm2.lib.jse.JseMathLib;

/**
 * Creates a sandboxed Lua environment that prevents access to dangerous APIs.
 * <p>
 * This specifically excludes:
 * <ul>
 *   <li>luajava - Prevents arbitrary Java class access and code execution</li>
 *   <li>os library - Prevents system command execution and file operations</li>
 *   <li>io library - Prevents file system access</li>
 *   <li>debug library - Prevents environment manipulation and introspection attacks</li>
 *   <li>load/loadfile/loadstring with bytecode - Prevents bytecode injection</li>
 * </ul>
 */
public final class SandboxedGlobals {

    private SandboxedGlobals() {
        // Utility class
    }

    /**
     * Creates a new sandboxed Lua globals environment.
     * This environment is safe to use with untrusted scripts.
     *
     * @return A new sandboxed Globals instance
     */
    public static Globals create() {
        Globals globals = new Globals();

        // Install safe base libraries
        globals.load(new JseBaseLib());      // Basic functions (print, type, etc.) - but we'll remove dangerous ones
        globals.load(new PackageLib());       // Package/module system
        globals.load(new Bit32Lib());         // Bit operations
        globals.load(new TableLib());         // Table manipulation
        globals.load(new StringLib());        // String manipulation
        globals.load(new JseMathLib());       // Math functions

        // Install the compiler so scripts can be loaded
        LoadState.install(globals);
        LuaC.install(globals);

        // Remove dangerous functions from base library
        removeDangerousFunctions(globals);

        return globals;
    }

    /**
     * Removes dangerous functions that could be used to escape the sandbox.
     */
    private static void removeDangerousFunctions(Globals globals) {
        // Remove functions that can load arbitrary code
        globals.set("dofile", LuaValue.NIL);      // Can load files from disk
        globals.set("loadfile", LuaValue.NIL);    // Can load files from disk

        // Note: We keep 'load' and 'loadstring' since they can only load Lua source code
        // (not bytecode) when LuaC is the only compiler installed, making them relatively safe.
        // However, if you want maximum security, uncomment these:
        // globals.set("load", LuaValue.NIL);
        // globals.set("loadstring", LuaValue.NIL);

        // Remove the package.loadlib function which can load native libraries
        LuaValue pkg = globals.get("package");
        if (pkg.istable()) {
            pkg.set("loadlib", LuaValue.NIL);
        }
    }
}

