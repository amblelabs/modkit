# Lua Scripting System

AmbleKit includes a powerful Lua scripting engine (powered by LuaJ) that allows you to extend Minecraft functionality without writing Java code. Scripts can run on both the client and server sides.

## Table of Contents
- [Script Types & Locations](#script-types--locations)
- [Commands](#commands)
- [Lifecycle Callbacks](#lifecycle-callbacks)
- [Minecraft API Reference](#minecraft-api-reference)
- [Entity API](#entity-api)
- [ItemStack API](#itemstack-api)
- [Example Scripts](#example-scripts)
- [GUI Integration](#gui-integration)

---

## Script Types & Locations

| Type | Location | Loaded From |
|------|----------|-------------|
| **Client Scripts** | `assets/<namespace>/script/*.lua` | Resource Packs |
| **Server Scripts** | `data/<namespace>/script/*.lua` | Data Packs |

---

## Commands

### Client-side (available to all players)
```
/amblescript execute <script_id> [args...]   - Run a script's onExecute function with optional arguments
/amblescript enable <script_id>              - Enable a script (starts onTick loop)
/amblescript disable <script_id>             - Disable a running script
/amblescript toggle <script_id>              - Toggle script enabled state
/amblescript list                            - Show enabled scripts
/amblescript available                       - Show all available scripts
```

### Server-side (requires operator permissions)
```
/serverscript execute <script_id> [args...]  - Run a script's onExecute function with optional arguments
/serverscript enable <script_id>             - Enable a script (starts onTick loop)
/serverscript disable <script_id>            - Disable a running script
/serverscript toggle <script_id>             - Toggle script enabled state
/serverscript list                           - Show enabled scripts
/serverscript available                      - Show all available scripts
```

### Command Arguments

The `execute` command accepts optional space-separated arguments that are passed to the script's `onExecute` function as a Lua table:

```
/serverscript execute mymod:my_script arg1 arg2 arg3
```

In the script, access arguments via the second parameter:

```lua
function onExecute(mc, args)
    if args[1] then
        mc:sendMessage("First argument: " .. args[1], false)
    end
end
```

---

## Lifecycle Callbacks

Scripts can define the following callback functions. Each receives a `mc` (MinecraftData) parameter:

| Callback | When Called | Use Case |
|----------|-------------|----------|
| `onExecute(mc, args)` | Via `/amblescript execute` or `/serverscript execute` | One-time actions, parameterized commands |
| `onEnable(mc)` | When script is enabled | Initialize state, play sounds |
| `onTick(mc)` | Every game tick while enabled | Continuous monitoring, automation |
| `onDisable(mc)` | When script is disabled | Cleanup, final messages |

The `args` parameter in `onExecute` is a Lua table containing space-separated arguments from the command (1-indexed, may be empty).

---

## Minecraft API Reference

The `mc` parameter provides access to Minecraft data. Methods vary by side:

### Shared Methods (Client & Server)
| Method | Description |
|--------|-------------|
| `mc:isClientSide()` | Returns true if running on client |
| `mc:dimension()` | Current dimension ID (e.g., "minecraft:overworld") |
| `mc:worldTime()` | Current world time in ticks |
| `mc:dayCount()` | Number of days elapsed |
| `mc:isRaining()` / `mc:isThundering()` | Weather state |
| `mc:biomeAt(x, y, z)` | Biome ID at position |
| `mc:blockAt(x, y, z)` | Block ID at position |
| `mc:lightLevelAt(x, y, z)` | Light level at position |
| `mc:player()` | The executing player entity |
| `mc:entities()` | All entities in the world |
| `mc:nearestEntity(distance)` | Closest entity within range |
| `mc:entitiesInRadius(radius)` | All entities within radius |
| `mc:runCommand(command)` | Execute a command |
| `mc:sendMessage(text, overlay)` | Send message to player (overlay = action bar) |
| `mc:log(message)` | Log to console |

### Client-Only Methods
| Method | Description |
|--------|-------------|
| `mc:username()` | Local player's username |
| `mc:selectedSlot()` | Currently selected hotbar slot (1-9) |
| `mc:selectSlot(slot)` | Set hotbar selection |
| `mc:dropStack(slot, entireStack)` | Drop item from inventory |
| `mc:isKeyPressed(key)` | Check if key is pressed ("forward", "jump", "w", etc.) |
| `mc:isMouseButtonPressed(button)` | Check mouse button ("left", "right", "middle") |
| `mc:gameMode()` | Current game mode |
| `mc:playSound(id, volume, pitch)` | Play a sound |
| `mc:lookingAtEntity()` | Entity in crosshairs (or nil) |
| `mc:lookingAtBlock()` | Block position in crosshairs (or nil) |
| `mc:clipboard()` / `mc:setClipboard(text)` | System clipboard access |
| `mc:displayScreen(screenId)` | Open a registered AmbleKit screen |
| `mc:closeScreen()` | Close current screen |

### Server-Only Methods
| Method | Description |
|--------|-------------|
| `mc:allPlayers()` | List of all online player entities |
| `mc:allPlayerNames()` | List of all online player names |
| `mc:playerCount()` / `mc:maxPlayers()` | Player counts |
| `mc:getPlayerByName(name)` | Get player entity by name |
| `mc:broadcast(message)` | Send message to all players |
| `mc:broadcastToPlayer(name, msg, overlay)` | Send message to specific player |
| `mc:serverName()` | Server name |
| `mc:serverTps()` | Current server TPS |
| `mc:tickCount()` | Total server ticks |
| `mc:isDedicatedServer()` | True if dedicated server |
| `mc:runCommandAs(playerName, command)` | Run command as specific player |

### Skin Management (Server-Only)

All skin methods return `true` on success, `false` on failure.

| Method | Description |
|--------|-------------|
| `mc:setSkin(playerName, skinUsername)` | Set player's skin to another player's skin |
| `mc:setSkinUrl(playerName, url, slim)` | Set player's skin from URL (slim: true/false) |
| `mc:setSkinSlim(playerName, slim)` | Change arm model without changing texture |
| `mc:clearSkin(playerName)` | Remove custom skin, restore original |
| `mc:hasSkin(playerName)` | Check if player has a custom skin |
| `mc:setSkinByUuid(uuid, skinUsername)` | Set skin by UUID string |
| `mc:setSkinUrlByUuid(uuid, url, slim)` | Set skin from URL by UUID string |
| `mc:clearSkinByUuid(uuid)` | Clear skin by UUID string |
| `mc:hasSkinByUuid(uuid)` | Check if entity has custom skin by UUID |

---

## Entity API

When you get an entity via `mc:player()`, `mc:entities()`, etc., you can call:

| Method | Description |
|--------|-------------|
| `entity:name()` | Display name |
| `entity:type()` | Entity type ID (e.g., "minecraft:player") |
| `entity:uuid()` | Entity UUID |
| `entity:isPlayer()` | True if player |
| `entity:position()` | Vec3d with x, y, z fields |
| `entity:blockPosition()` | BlockPos with x, y, z fields |
| `entity:health()` / `entity:maxHealth()` | Health values |
| `entity:velocity()` | Current velocity vector |
| `entity:yaw()` / `entity:pitch()` | Rotation |
| `entity:isAlive()` / `entity:isSneaking()` / `entity:isSprinting()` | State checks |
| `entity:isOnFire()` / `entity:isInvisible()` / `entity:isTouchingWater()` | Condition checks |
| `entity:inventory()` | List of ItemStacks |
| `entity:foodLevel()` / `entity:saturation()` | Hunger (players only) |
| `entity:experienceLevel()` / `entity:totalExperience()` | XP (players only) |
| `entity:effects()` | List of active effect IDs |
| `entity:hasEffect(effectId)` | Check for specific effect |
| `entity:armorValue()` | Total armor points |

---

## ItemStack API

ItemStacks from inventories provide:

| Method | Description |
|--------|-------------|
| `item:id()` | Item ID (e.g., "minecraft:diamond_sword") |
| `item:name()` | Display name |
| `item:count()` / `item:maxCount()` | Stack counts |
| `item:damage()` / `item:maxDamage()` | Durability |
| `item:durabilityPercent()` | Remaining durability (0.0 - 1.0) |
| `item:isEmpty()` / `item:isStackable()` | Stack properties |
| `item:hasEnchantments()` | Has enchantments |
| `item:enchantments()` | List of "enchant_id:level" strings |
| `item:rarity()` | Item rarity |
| `item:isFood()` | Is food item |
| `item:hasNbt()` / `item:nbt()` | NBT data access |

---

## Example Scripts

### Script with Arguments
Set a player's skin with command arguments:

```lua
-- data/mymod/script/skin_set.lua
-- Usage:
--   /serverscript execute mymod:skin_set Notch true
--   /serverscript execute mymod:skin_set Notch false duzo

function onExecute(mc, args)
    -- Validate arguments
    if args == nil or #args < 2 then
        mc:sendMessage("§cUsage: /serverscript execute mymod:skin_set <skin_username> <slim> [target_player]", false)
        return
    end

    local skinUsername = args[1]
    local slim = args[2] == "true"
    local targetPlayer = args[3]

    -- If no target player specified, use the executing player
    if targetPlayer == nil then
        local player = mc:player()
        if player == nil then
            mc:sendMessage("§cNo player context!", false)
            return
        end
        targetPlayer = player:name()
    end

    -- Apply the skin
    if mc:setSkin(targetPlayer, skinUsername) then
        mc:sendMessage("§aSkin applied to " .. targetPlayer .. "!", false)
        mc:setSkinSlim(targetPlayer, slim)
    else
        mc:sendMessage("§cFailed to apply skin!", false)
    end
end
```

### Simple Execute Script
Display world info on command:

```lua
-- assets/mymod/script/world_info.lua
-- Run with: /amblescript execute mymod:world_info

function onExecute(mc, args)
    local player = mc:player()
    local pos = player:blockPosition()
    
    mc:sendMessage("§6=== World Info ===", false)
    mc:sendMessage("§7Dimension: §a" .. mc:dimension(), false)
    mc:sendMessage("§7Day: §e" .. mc:dayCount(), false)
    mc:sendMessage("§7Biome: §b" .. mc:biomeAt(pos.x, pos.y, pos.z), false)
    mc:sendMessage("§7Light: §f" .. mc:lightLevelAt(pos.x, pos.y, pos.z), false)
end
```

### Tick-Based Script
Continuous monitoring with enable/disable:

```lua
-- assets/mymod/script/health_monitor.lua
-- Enable with: /amblescript enable mymod:health_monitor
-- Disable with: /amblescript disable mymod:health_monitor

local lastHealth = 0

function onEnable(mc)
    lastHealth = mc:player():health()
    mc:sendMessage("§aHealth monitor enabled!", false)
end

function onTick(mc)
    local health = mc:player():health()
    if health < lastHealth then
        mc:sendMessage("§cDamage taken! Health: " .. health, true)
        if mc:isClientSide() then
            mc:playSound("minecraft:entity.player.hurt", 0.5, 1.0)
        end
    end
    lastHealth = health
end

function onDisable(mc)
    mc:sendMessage("§7Health monitor disabled.", false)
end
```

### Server Script
Admin broadcast utility:

```lua
-- data/mymod/script/server_status.lua
-- Run with: /serverscript execute mymod:server_status

function onExecute(mc, args)
    local playerCount = mc:playerCount()
    local maxPlayers = mc:maxPlayers()
    local tps = string.format("%.1f", mc:serverTps())
    
    mc:broadcast("§6[Server] §fPlayers: §e" .. playerCount .. "/" .. maxPlayers)
    mc:broadcast("§6[Server] §fTPS: §a" .. tps)
    mc:log("Server status broadcast by admin")
end
```

### Skin Management Script
Change player skins on the server:

```lua
-- data/mymod/script/disguise.lua
-- Run with: /serverscript execute mymod:disguise
-- Or with args: /serverscript execute mymod:disguise Herobrine

function onExecute(mc, args)
    local player = mc:player()
    if player == nil then
        mc:log("No player context for this script")
        return
    end
    
    local playerName = player:name()
    local skinName = args[1] or "Notch"  -- Default to Notch if no argument provided
    
    -- Set the player's skin to look like the specified user (returns true/false)
    if mc:setSkin(playerName, skinName) then
        mc:broadcastToPlayer(playerName, "§aYou are now disguised as " .. skinName .. "!", false)
    else
        mc:broadcastToPlayer(playerName, "§cFailed to apply disguise!", false)
    end
end

-- Example: Disguise all players as the same skin
function disguiseAll(mc, skinUsername)
    local success = 0
    for _, playerName in pairs(mc:allPlayerNames()) do
        if mc:setSkin(playerName, skinUsername) then
            success = success + 1
        end
    end
    mc:broadcast("§eDisguised " .. success .. " players!")
end

-- Example: Clear all disguises
function clearAllDisguises(mc)
    for _, playerName in pairs(mc:allPlayerNames()) do
        if mc:hasSkin(playerName) then
            mc:clearSkin(playerName)
        end
    end
    mc:broadcast("§7All disguises have been removed.")
end
```

### Skin from URL Example
Apply custom skins from URLs:

```lua
-- data/mymod/script/custom_skin.lua
-- Run with: /serverscript execute mymod:custom_skin <url> [slim]
-- Example: /serverscript execute mymod:custom_skin https://example.com/skin.png true

function onExecute(mc, args)
    local player = mc:player()
    if player == nil then return end
    
    local playerName = player:name()
    local skinUrl = args[1] or "https://example.com/skins/custom_skin.png"
    local slim = args[2] == "true"
    
    -- Set skin from URL with slim (Alex-style) arms
    if mc:setSkinUrl(playerName, skinUrl, slim) then
        mc:broadcastToPlayer(playerName, "§aCustom skin applied!", false)
    else
        mc:broadcastToPlayer(playerName, "§cFailed to apply skin!", false)
    end
end

-- Toggle between slim and wide arm models
function toggleSlimArms(mc)
    local player = mc:player()
    if player == nil then return end
    
    local playerName = player:name()
    
    if mc:hasSkin(playerName) then
        if mc:setSkinSlim(playerName, true) then
            mc:broadcastToPlayer(playerName, "§7Switched to slim arms.", false)
        end
    else
        mc:broadcastToPlayer(playerName, "§cYou don't have a custom skin!", false)
    end
end
```

### UUID-Based Skin Management
Use UUIDs directly for non-player entities or stored references:

```lua
-- data/mymod/script/npc_skins.lua
-- Run with: /serverscript execute mymod:npc_skins [skin_username]

function onExecute(mc, args)
    local player = mc:player()
    if player == nil then return end
    
    -- Get the player's UUID
    local uuid = player:uuid()
    local skinName = args[1] or "Herobrine"
    
    -- Set skin using UUID string
    if mc:setSkinByUuid(uuid, skinName) then
        mc:sendMessage("§cSkin applied via UUID!", false)
    end
end

-- Apply skin to a stored NPC UUID
function applyNpcSkin(mc)
    local npcUuid = "550e8400-e29b-41d4-a716-446655440000"  -- example UUID
    
    if mc:setSkinUrlByUuid(npcUuid, "https://example.com/npc_skin.png", false) then
        mc:log("NPC skin updated successfully")
    else
        mc:logWarn("Failed to update NPC skin")
    end
end
```

---

## GUI Integration

Lua scripts integrate with AmbleKit's [JSON GUI System](GUI_SYSTEM.md) in two ways:

1. **Opening screens** from scripts using `mc:displayScreen()`
2. **Handling GUI events** by attaching scripts to buttons

### Opening GUI Screens from Scripts

Use `mc:displayScreen(screenId)` to open any registered AmbleKit GUI:

```lua
-- assets/mymod/script/open_menu.lua

function onExecute(mc)
    -- Open a GUI defined in assets/mymod/gui/my_menu.json
    mc:displayScreen("mymod:my_menu")
end
```

### Attaching Scripts to GUI Elements

In your JSON GUI definition, use the `script` property to attach a Lua script to a button:

```json
{
  "layout": [80, 24],
  "background": [100, 100, 100],
  "hover_background": [150, 150, 150],
  "script": "mymod:my_button_handler"
}
```

This loads `assets/mymod/script/my_button_handler.lua`.

### GUI Callbacks

GUI scripts use different callbacks than standalone scripts. Instead of `mc`, they receive `self` (a LuaElement wrapper):

| Callback | When Called | Parameters |
|----------|-------------|------------|
| `onInit(self)` | When script is attached | `self` |
| `onClick(self, mouseX, mouseY, button)` | Mouse pressed on element | `self`, coordinates, button (0=left) |
| `onRelease(self, mouseX, mouseY, button)` | Mouse released on element | `self`, coordinates, button |
| `onHover(self, mouseX, mouseY)` | Mouse hovering over element | `self`, coordinates |

### LuaElement API

The `self` parameter provides access to the GUI element:

| Method | Description |
|--------|-------------|
| `self:id()` | Element's identifier |
| `self:x()`, `self:y()` | Current position |
| `self:width()`, `self:height()` | Current dimensions |
| `self:setPosition(x, y)` | Update position |
| `self:setDimensions(w, h)` | Update size |
| `self:setVisible(bool)` | Show/hide element |
| `self:parent()` | Parent LuaElement (or nil) |
| `self:child(index)` | Get child at index (0-based) |
| `self:childCount()` | Number of children |
| `self:getText()` | Get text content (text elements only) |
| `self:setText(text)` | Set text content (text elements only) |
| `self:closeScreen()` | Close the current screen |
| `self:minecraft()` | Get MinecraftData for full API access |

### Accessing Minecraft Data from GUI Scripts

Use `self:minecraft()` to get a MinecraftData object with full API access:

```lua
function onClick(self, mouseX, mouseY, button)
    local mc = self:minecraft()
    
    -- Access player data
    local player = mc:player()
    local health = player:health()
    
    -- Play sounds
    mc:playSound("minecraft:ui.button.click", 1.0, 1.0)
    
    -- Send messages
    mc:sendMessage("Health: " .. health, false)
    
    -- Check input
    if mc:isKeyPressed("sneak") then
        mc:sendMessage("Shift-click detected!", false)
    end
end
```

### Complete GUI Script Example

```lua
-- assets/mymod/script/inventory_button.lua
-- Attached to a button in a JSON GUI

local clickCount = 0

function onInit(self)
    -- Initialize when script is attached to the button
    print("Button initialized: " .. self:id())
end

function onClick(self, mouseX, mouseY, button)
    clickCount = clickCount + 1
    local mc = self:minecraft()
    
    -- Update a child text element
    for i = 0, self:childCount() - 1 do
        local child = self:child(i)
        if child:getText() then
            child:setText("Clicked: " .. clickCount)
            break
        end
    end
    
    -- Show player inventory info
    local player = mc:player()
    local inventory = player:inventory()
    local itemCount = 0
    
    for _, item in pairs(inventory) do
        if not item:isEmpty() then
            itemCount = itemCount + item:count()
        end
    end
    
    mc:sendMessage("You have " .. itemCount .. " items!", false)
    mc:playSound("minecraft:ui.button.click", 1.0, 1.0)
end

function onHover(self, mouseX, mouseY)
    -- Called every frame while hovering (use sparingly)
end

function onRelease(self, mouseX, mouseY, button)
    -- Called when mouse button is released over element
end
```

---

## See Also

- [JSON GUI System](GUI_SYSTEM.md) - Full GUI definition documentation
- [Dynamic Skin System](SKIN_SYSTEM.md) - Skin commands, Java API, and persistence
