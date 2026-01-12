package dev.amble.lib.script.lua;

import org.luaj.vm2.Globals;
import org.luaj.vm2.LoadState;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.compiler.LuaC;
import org.luaj.vm2.lib.BaseLib;
import org.luaj.vm2.lib.Bit32Lib;
import org.luaj.vm2.lib.StringLib;
import org.luaj.vm2.lib.TableLib;
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
 *   <li>package library - Prevents loading modules from disk</li>
 *   <li>load/loadfile/dofile - Prevents loading code from files</li>
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

        // Install safe base libraries only
        // Using BaseLib instead of JseBaseLib to avoid any file system access
        globals.load(new BaseLib());          // Basic functions (print, type, tostring, etc.)
        globals.load(new Bit32Lib());         // Bit operations
        globals.load(new TableLib());         // Table manipulation
        globals.load(new StringLib());        // String manipulation
        globals.load(new JseMathLib());       // Math functions

        // NOTE: We intentionally do NOT load:
        // - PackageLib (can search/load files from disk)
        // - IoLib / JseIoLib (file system access)
        // - OsLib / JseOsLib (system commands, file operations)
        // - DebugLib (can manipulate environments)
        // - LuajavaLib (arbitrary Java class access)

        // Install the compiler so scripts can be loaded from strings
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
        // Remove functions that can load code from files
        globals.set("dofile", LuaValue.NIL);      // Loads and executes files from disk
        globals.set("loadfile", LuaValue.NIL);    // Loads files from disk

        // Remove load/loadstring to prevent any dynamic code execution
        // This is the safest option as it prevents all forms of dynamic code loading
        globals.set("load", LuaValue.NIL);
        globals.set("loadstring", LuaValue.NIL);
    }
}
