# Dynamic Skin System

AmbleKit provides a dynamic player skin system that allows you to change player skins at runtime. This is useful for NPCs, disguises, custom player appearances, and roleplay servers.

## Table of Contents
- [Overview](#overview)
- [Commands](#commands)
- [Java API](#java-api)
- [Skin Sources](#skin-sources)
- [Persistence](#persistence)
- [Integration](#integration)

---

## Overview

The Dynamic Skin System provides:
- **Runtime Skin Changes** - Change player/entity skins without relogging
- **Multiple Sources** - Load skins by username or direct URL
- **Slim/Wide Models** - Support for both Alex (slim) and Steve (wide) arm models
- **Server Synchronization** - Skins sync automatically to all connected clients
- **Persistent Storage** - Skins persist across server restarts
- **Entity Support** - Works with any entity implementing `PlayerSkinTexturable`

---

## Commands

All skin commands require operator permissions (level 2).

### Set Skin by Username

Copy another player's skin:

```
/amblekit skin <target> set <username>
```

**Example:**
```
/amblekit skin @p set Notch
```

### Set Skin by URL

Load a skin from a direct image URL:

```
/amblekit skin <target> slim <true|false> <url>
```

| Parameter | Description |
|-----------|-------------|
| `target` | Entity to modify |
| `slim` | `true` for slim arms (Alex), `false` for wide arms (Steve) |
| `url` | Direct URL to a skin image (PNG) |

**Example:**
```
/amblekit skin @p slim false https://example.com/skins/custom_skin.png
```

### Toggle Slim Arms

Change the arm model without changing the skin:

```
/amblekit skin <target> slim <true|false>
```

**Example:**
```
/amblekit skin @p slim true
```

### Clear/Reset Skin

Remove custom skin and restore the original:

```
/amblekit skin <target> clear
```

**Example:**
```
/amblekit skin @p clear
```

---

## Java API

### Setting Skins Programmatically

```java
import dev.amble.lib.skin.SkinData;
import dev.amble.lib.skin.SkinTracker;

// Get the target entity's UUID
UUID targetUuid = player.getUuid();

// Set skin by username (async lookup)
SkinData.username("Notch", skinData -> {
    skinData.upload(targetUuid);
});

// Set skin by username with specific arm model
SkinData data = SkinData.username("Notch", true); // slim = true
SkinTracker.getInstance().putSynced(targetUuid, data);

// Set skin by URL
SkinData urlSkin = SkinData.url("https://example.com/skin.png", false); // slim = false
SkinTracker.getInstance().putSynced(targetUuid, urlSkin);
```

### Clearing Skins

```java
// Remove custom skin
SkinTracker.getInstance().removeSynced(player.getUuid());
```

### Querying Skins

```java
import dev.amble.lib.skin.SkinTracker;
import dev.amble.lib.skin.SkinData;

// Check if entity has custom skin
Optional<SkinData> skin = SkinTracker.getInstance().getOptional(player.getUuid());

if (skin.isPresent()) {
    SkinData data = skin.get();
    // Entity has a custom skin
}
```

### Implementing PlayerSkinTexturable

To make your own entities support dynamic skins, implement `PlayerSkinTexturable`:

```java
public class MyNpcEntity extends LivingEntity implements PlayerSkinTexturable {
    
    @Override
    public UUID getUuid() {
        return super.getUuid();
    }
    
    // The skin system will automatically apply skins to entities
    // implementing this interface
}
```

---

## Skin Sources

### Username-Based Skins

When you set a skin by username, AmbleKit:
1. Looks up the player's UUID via Mojang API
2. Retrieves their skin data
3. Caches the result for performance

```java
// Async version (recommended for usernames)
SkinData.username("PlayerName", result -> {
    result.upload(targetUuid);
});

// Sync version (use with caution - may block)
SkinData data = SkinData.username("PlayerName", false);
```

### URL-Based Skins

Direct URL skins load from any accessible image URL:

```java
SkinData data = SkinData.url("https://example.com/skin.png", false);
SkinTracker.getInstance().putSynced(targetUuid, data);
```

**Requirements:**
- URL must be publicly accessible
- Image should be a valid Minecraft skin (64x64 or 64x32 PNG)
- HTTPS is recommended

### Arm Model Types

| Model | Description | Common Use |
|-------|-------------|------------|
| **Wide** (`slim = false`) | Classic Steve-style arms (4px wide) | Default male characters |
| **Slim** (`slim = true`) | Alex-style arms (3px wide) | Default female characters |

```java
// Change arm model of existing skin
SkinData existingSkin = SkinTracker.getInstance().get(uuid);
if (existingSkin != null) {
    SkinData newData = existingSkin.withSlim(true);
    SkinTracker.getInstance().putSynced(uuid, newData);
}
```

---

## Persistence

### Automatic Saving

Skins are automatically saved when the server stops:
- Storage location: `<world>/amblekit/skins.json`
- Format: JSON map of UUID to skin data

### Automatic Loading

On server start, skins are loaded and synced to all connecting players.

### Manual Sync

Force sync all skins to players:

```java
// Sync to all players
SkinTracker.getInstance().sync();

// Sync to specific player
SkinTracker.getInstance().sync(serverPlayerEntity);
```

---

## Integration

### With Lua Scripts (Server-Side)

Server-side Lua scripts have direct access to the skin management API:

```lua
-- data/mymod/script/disguise_manager.lua
-- Run with: /serverscript execute mymod:disguise_manager

function onExecute(mc)
    local player = mc:player()
    if player == nil then return end
    
    local playerName = player:name()
    
    -- Set player's skin to another player's skin
    mc:setSkin(playerName, "Notch")
    mc:broadcastToPlayer(playerName, "§aDisguised as Notch!", false)
end
```

#### Server-Side Skin API Methods

All skin methods return `true` on success, `false` on failure (player not found, invalid UUID, etc.).

**By Player Name:**
| Method | Description |
|--------|-------------|
| `mc:setSkin(playerName, skinUsername)` | Set player's skin to another player's skin |
| `mc:setSkinUrl(playerName, url, slim)` | Set skin from URL (slim = true for Alex arms) |
| `mc:setSkinSlim(playerName, slim)` | Change arm model (true = slim/Alex, false = wide/Steve) |
| `mc:clearSkin(playerName)` | Remove custom skin, restore original |
| `mc:hasSkin(playerName)` | Check if player has a custom skin |

**By UUID String:**
| Method | Description |
|--------|-------------|
| `mc:setSkinByUuid(uuid, skinUsername)` | Set skin by UUID string |
| `mc:setSkinUrlByUuid(uuid, url, slim)` | Set skin from URL by UUID string |
| `mc:clearSkinByUuid(uuid)` | Clear skin by UUID string |
| `mc:hasSkinByUuid(uuid)` | Check if entity has custom skin by UUID |

#### Complete Example: Disguise System

```lua
-- data/mymod/script/disguise_system.lua

-- Disguise player as another username
function onExecute(mc)
    local player = mc:player()
    if player == nil then
        mc:log("Script requires player context")
        return
    end
    
    local playerName = player:name()
    if mc:setSkin(playerName, "Herobrine") then
        mc:broadcastToPlayer(playerName, "§cYou are now disguised as Herobrine!", false)
        mc:log("Player " .. playerName .. " disguised as Herobrine")
    else
        mc:logWarn("Failed to disguise player " .. playerName)
    end
end

-- Tick function to auto-disguise players on join (when enabled)
local lastPlayerCount = 0
function onTick(mc)
    local currentCount = mc:playerCount()
    if currentCount > lastPlayerCount then
        -- New player joined, could auto-apply skins here
        mc:log("Player count changed: " .. lastPlayerCount .. " -> " .. currentCount)
    end
    lastPlayerCount = currentCount
end

function onDisable(mc)
    -- Clear all custom skins when script is disabled
    for _, name in pairs(mc:allPlayerNames()) do
        if mc:hasSkin(name) then
            mc:clearSkin(name)
        end
    end
    mc:broadcast("§7All disguises removed.")
end
```

#### Using URL Skins

```lua
-- Apply custom skin from URL
function applyCustomSkin(mc)
    local player = mc:player()
    if player == nil then return end
    
    local playerName = player:name()
    local url = "https://example.com/skins/custom.png"
    
    -- Use slim arms (Alex model)
    if mc:setSkinUrl(playerName, url, true) then
        mc:broadcastToPlayer(playerName, "§aCustom skin applied!", false)
    else
        mc:broadcastToPlayer(playerName, "§cFailed to apply skin!", false)
    end
end
```

#### UUID-Based Skin Changes

For NPCs or entities stored by UUID, use UUID-based methods:

```lua
function onExecute(mc)
    -- Get all players and apply skins using their UUIDs
    local players = mc:allPlayers()
    
    for _, player in pairs(players) do
        local uuid = player:uuid()
        if not mc:hasSkinByUuid(uuid) then
            if mc:setSkinByUuid(uuid, "Steve") then
                mc:log("Applied default skin to " .. player:name())
            end
        end
    end
end

-- Apply skin to a stored NPC by UUID
function applyNpcSkin(mc)
    local npcUuid = "550e8400-e29b-41d4-a716-446655440000"
    
    if mc:setSkinUrlByUuid(npcUuid, "https://example.com/npc.png", false) then
        mc:log("NPC skin updated")
    else
        mc:logWarn("Failed to update NPC skin - invalid UUID?")
    end
end
```

### With Client-Side Scripts

Client-side scripts can trigger skin changes via commands:

```lua
-- assets/mymod/script/request_skin.lua
-- Run with: /amblescript execute mymod:request_skin

function onExecute(mc)
    -- Client scripts use commands (requires server permission)
    mc:runCommand("/amblekit skin @p set Notch")
    mc:sendMessage("§7Skin change requested!", false)
end
```

> **Note:** Direct skin API methods (`setSkin`, `clearSkin`, etc.) are only available in server-side scripts. Client scripts must use commands.

### With NPCs

Create NPCs with custom skins:

```java
// Create your NPC entity
MyNpcEntity npc = new MyNpcEntity(world);
npc.setPosition(x, y, z);
world.spawnEntity(npc);

// Set its skin
SkinData.username("SomePlayer", skin -> {
    SkinTracker.getInstance().putSynced(npc.getUuid(), skin);
});
```

### Events

Listen for skin changes:

```java
// The skin tracker uses Fabric networking
// Clients receive updates via the SYNC_KEY packet
ClientPlayNetworking.registerGlobalReceiver(SkinTracker.SYNC_KEY, (client, handler, buf, responseSender) -> {
    // Handle skin update
});
```

---

## Technical Details

### Network Protocol

Skins are synchronized using Fabric's networking API:
- **Packet ID:** `amblekit:skin_sync`
- **Direction:** Server → Client
- **Triggered:** On player join, skin change, or manual sync

### Client-Side Caching

The `SkinCache` handles texture management:
- Downloads and caches skin textures
- Converts PNG data to Minecraft textures
- Handles slim/wide model variations

### Thread Safety

The `SkinTracker` is thread-safe:
- Uses concurrent data structures
- Safe to call from any thread
- Networking handled on appropriate threads

---

## Troubleshooting

### Skin Not Updating

1. Ensure the target entity implements `PlayerSkinTexturable`
2. Check that the skin URL is accessible
3. Verify the player has reconnected after server-side changes

### Invalid Skin Source

- Username lookups require internet access
- URL skins must be valid PNG images
- Some CDNs may block automated downloads

### Slim Arms Not Working

Ensure you're using `withSlim()` or specifying the slim parameter:

```java
// Correct
SkinData data = SkinData.url(url, true); // slim = true

// Or modify existing
data = data.withSlim(true);
```

---

## See Also

- [Lua Scripting System](LUA_SCRIPTING.md) - Full skin management API for server scripts
- [Animation System](ANIMATION_SYSTEM.md) - Animate entities with custom skins
