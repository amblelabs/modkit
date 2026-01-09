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
/amblescript execute <script_id>   - Run a script's onExecute function
/amblescript enable <script_id>    - Enable a script (starts onTick loop)
/amblescript disable <script_id>   - Disable a running script
/amblescript toggle <script_id>    - Toggle script enabled state
/amblescript list                  - Show enabled scripts
/amblescript available             - Show all available scripts
```

### Server-side (requires operator permissions)
```
/serverscript execute <script_id>  - Run a script's onExecute function
/serverscript enable <script_id>   - Enable a script (starts onTick loop)
/serverscript disable <script_id>  - Disable a running script
/serverscript toggle <script_id>   - Toggle script enabled state
/serverscript list                 - Show enabled scripts
/serverscript available            - Show all available scripts
```

---

## Lifecycle Callbacks

Scripts can define the following callback functions. Each receives a `mc` (MinecraftData) parameter:

| Callback | When Called | Use Case |
|----------|-------------|----------|
| `onExecute(mc)` | Via `/amblescript execute` or `/serverscript execute` | One-time actions, info displays |
| `onEnable(mc)` | When script is enabled | Initialize state, play sounds |
| `onTick(mc)` | Every game tick while enabled | Continuous monitoring, automation |
| `onDisable(mc)` | When script is disabled | Cleanup, final messages |

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

### Simple Execute Script
Display world info on command:

```lua
-- assets/mymod/script/world_info.lua
-- Run with: /amblescript execute mymod:world_info

function onExecute(mc)
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

function onExecute(mc)
    local playerCount = mc:playerCount()
    local maxPlayers = mc:maxPlayers()
    local tps = string.format("%.1f", mc:serverTps())
    
    mc:broadcast("§6[Server] §fPlayers: §e" .. playerCount .. "/" .. maxPlayers)
    mc:broadcast("§6[Server] §fTPS: §a" .. tps)
    mc:log("Server status broadcast by admin")
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
