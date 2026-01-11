# JSON GUI System

AmbleKit provides a declarative JSON-based GUI system that lets you create Minecraft screens without writing Java code. Define layouts, colors, textures, and interactive buttons entirely in JSON files loaded from resource packs.

## Table of Contents
- [Getting Started](#getting-started)
- [File Location](#file-location)
- [JSON Structure](#json-structure)
- [Properties Reference](#properties-reference)
- [Background Types](#background-types)
- [Text Elements](#text-elements)
- [Entity Display Elements](#entity-display-elements)
- [Buttons & Interactivity](#buttons--interactivity)
- [Lua Script Integration](#lua-script-integration)
- [Displaying Screens](#displaying-screens)
- [Complete Example](#complete-example)

---

## Getting Started

The AmbleKit GUI system allows you to:
- Define screen layouts in JSON files
- Use solid colors or textures as backgrounds
- Create nested container hierarchies
- Add text with automatic word wrapping
- Create interactive buttons with hover/press states
- Attach Lua scripts for dynamic behavior

---

## File Location

GUI definitions are loaded from resource packs:

```
assets/<namespace>/gui/<name>.json
```

For example, `assets/mymod/gui/main_menu.json` creates a screen with ID `mymod:main_menu`.

---

## JSON Structure

Every GUI element shares these core properties:

```json
{
  "layout": [width, height],
  "background": <color or texture>,
  "padding": 0,
  "spacing": 0,
  "alignment": ["centre", "centre"],
  "children": []
}
```

---

## Properties Reference

### Layout

Defines the element's dimensions as `[width, height]`:

```json
"layout": [200, 150]
```

### Padding

Internal spacing between the element's edge and its children:

```json
"padding": 10
```

### Spacing

Gap between child elements:

```json
"spacing": 5
```

### Alignment

Controls how children are positioned within the container. Format: `[horizontal, vertical]`

| Value | Description |
|-------|-------------|
| `"start"` | Align to left/top |
| `"centre"` / `"center"` | Center alignment |
| `"end"` | Align to right/bottom |

```json
"alignment": ["centre", "centre"]
```

### Requires New Row

Forces this element to start on a new row (for flow layout):

```json
"requires_new_row": true
```

### Should Pause

Whether the screen pauses the game (singleplayer):

```json
"should_pause": true
```

### ID

Optional explicit identifier for the element:

```json
"id": "mymod:my_button"
```

---

## Background Types

### Solid Color

RGB array (0-255), with optional alpha:

```json
"background": [255, 128, 0]
```

With transparency:

```json
"background": [0, 0, 0, 128]
```

### Texture

Reference a texture file with UV coordinates:

```json
"background": {
  "texture": "mymod:textures/gui/panel.png",
  "u": 0,
  "v": 0,
  "regionWidth": 200,
  "regionHeight": 150,
  "textureWidth": 256,
  "textureHeight": 256
}
```

| Property | Description |
|----------|-------------|
| `texture` | Resource location of the texture |
| `u`, `v` | Top-left corner of the region in the texture |
| `regionWidth`, `regionHeight` | Size of the region to sample |
| `textureWidth`, `textureHeight` | Full dimensions of the texture file |

---

## Text Elements

Add the `text` property to display text. The text will automatically wrap to fit the container width:

```json
{
  "layout": [100, 30],
  "background": [0, 0, 0, 0],
  "text": "gui.mymod.welcome_message"
}
```

The `text` value is passed through `Text.translatable()`, so you can use translation keys from your lang files.

### Text Alignment

Control text positioning within the element:

```json
{
  "layout": [100, 30],
  "background": [50, 50, 50],
  "text": "Hello World",
  "text_alignment": ["centre", "centre"]
}
```

---

## Entity Display Elements

Display living entities within a GUI element using the `entity_uuid` property. This renders the entity in an inventory-screen style, perfect for character viewers, mob displays, or player previews.

### Basic Entity Display

```json
{
  "layout": [60, 80],
  "background": [40, 40, 60, 200],
  "entity_uuid": "player"
}
```

The special value `"player"` automatically uses the local player's UUID.

### Entity Display Properties

| Property | Type | Default | Description |
|----------|------|---------|-------------|
| `entity_uuid` | string | required | Entity UUID or `"player"` for local player |
| `follow_cursor` | boolean | `false` | Entity rotates to follow the mouse cursor |
| `look_at` | [x, y] | center | Fixed position the entity looks at (relative to element) |
| `entity_scale` | float | `1.0` | Scale multiplier for entity rendering |

### Follow Cursor Mode

Make the entity rotate to track the mouse cursor:

```json
{
  "layout": [60, 80],
  "background": [40, 40, 60],
  "entity_uuid": "player",
  "follow_cursor": true,
  "entity_scale": 0.9
}
```

### Fixed Look-At Position

Set a specific point the entity should look at:

```json
{
  "layout": [60, 80],
  "background": [60, 40, 40],
  "entity_uuid": "",
  "follow_cursor": false,
  "look_at": [30, 20]
}
```

Coordinates are relative to the element's top-left corner.

### Dynamic Entity Display

Set the entity UUID dynamically via Lua scripts:

```json
{
  "id": "mymod:mob_display",
  "layout": [60, 80],
  "background": [50, 50, 50],
  "entity_uuid": ""
}
```

```lua
function onInit(self)
    local mc = self:minecraft()
    local nearest = mc:nearestEntity(20)
    
    -- Find the entity display by ID
    local display = findChildById(root, "mymod:mob_display")
    if display and nearest then
        display:setEntityUuid(nearest:uuid())
    end
end
```

### Notes

- Only `LivingEntity` types (players, mobs, animals) can be rendered
- Non-living entities or invalid UUIDs display "N/A"
- The entity is looked up each render frame using a cached approach for efficiency

---

## Buttons & Interactivity

Adding any of these properties converts an element into a button:
- `script` - Attach a Lua script
- `on_click` - Run a command on click
- `hover_background` - Background when hovered
- `press_background` - Background when pressed

When a button has a `text` property, a child text element is automatically created with a transparent background, so you can define text directly on buttons without manually creating children.

### Basic Button

```json
{
  "layout": [80, 24],
  "background": [100, 100, 100],
  "hover_background": [150, 150, 150],
  "press_background": [50, 50, 50],
  "text": "Click Me",
  "on_click": "/say Button clicked!"
}
```

### Button with Text Alignment

```json
{
  "layout": [120, 30],
  "background": [80, 80, 80],
  "hover_background": [100, 100, 100],
  "text": "gui.mymod.button_label",
  "text_alignment": ["centre", "centre"],
  "script": "mymod:my_handler"
}
```

### Command Execution

The `on_click` property runs a command when clicked:

```json
"on_click": "/gamemode creative"
```

Commands must start with `/` and are executed as the local player.

---

## Lua Script Integration

For dynamic behavior, attach Lua scripts to buttons. This is where the GUI system integrates with AmbleKit's [Lua Scripting System](LUA_SCRIPTING.md).

### Attaching a Script

Reference a script by its ID (without the `script/` prefix or `.lua` suffix):

```json
{
  "layout": [80, 24],
  "background": [0, 200, 0],
  "hover_background": [0, 255, 0],
  "press_background": [0, 150, 0],
  "script": "mymod:button_handler"
}
```

This loads `assets/mymod/script/button_handler.lua`.

### GUI Script Callbacks

GUI scripts use different callbacks than standalone scripts. They receive a `self` (LuaElement) parameter:

| Callback | When Called | Parameters |
|----------|-------------|------------|
| `onInit(self)` | When script is attached to element | `self` |
| `onClick(self, mouseX, mouseY, button)` | Mouse button pressed | `self`, coordinates, button (0=left, 1=right) |
| `onRelease(self, mouseX, mouseY, button)` | Mouse button released | `self`, coordinates, button |
| `onHover(self, mouseX, mouseY)` | Mouse hovering over element | `self`, coordinates |

### LuaElement API

The `self` parameter provides access to the GUI element:

| Method | Description |
|--------|-------------|
| `self:id()` | Element's identifier (as string) |
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
| `self:minecraft()` | Get MinecraftData for world/player access |

#### Entity Display Methods

These methods only work on `AmbleEntityDisplay` elements:

| Method | Description |
|--------|-------------|
| `self:getEntityUuid()` | Get entity UUID as string (or nil) |
| `self:setEntityUuid(uuid)` | Set entity UUID (string or "player") |
| `self:isFollowCursor()` | Check if entity follows cursor |
| `self:setFollowCursor(bool)` | Enable/disable cursor following |
| `self:setLookAt(x, y)` | Set fixed look-at position |
| `self:setEntityScale(scale)` | Set entity scale multiplier |

### Example GUI Script

```lua
-- assets/mymod/script/button_handler.lua

local clickCount = 0

function onInit(self)
    -- Called when the script is attached to the button
    print("Button initialized: " .. self:id())
end

function onClick(self, mouseX, mouseY, button)
    clickCount = clickCount + 1
    
    -- Update button text
    for i = 0, self:childCount() - 1 do
        local child = self:child(i)
        if child:getText() then
            child:setText("Clicks: " .. clickCount)
            break
        end
    end
    
    -- Access Minecraft data
    local mc = self:minecraft()
    mc:sendMessage("Button clicked " .. clickCount .. " times!", false)
    
    -- Play a sound
    mc:playSound("minecraft:ui.button.click", 1.0, 1.0)
end

function onHover(self, mouseX, mouseY)
    -- Called every frame while hovering
end

function onRelease(self, mouseX, mouseY, button)
    -- Called when mouse button is released
end
```

### Accessing World Data from GUI

Use `self:minecraft()` to access the full [Minecraft API](LUA_SCRIPTING.md#minecraft-api-reference):

```lua
function onClick(self, mouseX, mouseY, button)
    local mc = self:minecraft()
    
    -- Get player info
    local player = mc:player()
    local health = player:health()
    
    -- Check input
    if mc:isKeyPressed("sneak") then
        mc:sendMessage("Shift-clicked!", false)
    end
    
    -- Run commands
    mc:runCommand("/time set day")
    
    -- Close the screen
    self:closeScreen()
end
```

---

## Displaying Screens

### From Lua Scripts

Use `mc:displayScreen(screenId)` to open a registered GUI:

```lua
function onExecute(mc)
    mc:displayScreen("mymod:main_menu")
end
```

### From Java

```java
AmbleContainer screen = AmbleGuiRegistry.getInstance().get(new Identifier("mymod", "main_menu"));
if (screen != null) {
    screen.display();
}
```

---

## Complete Example

### GUI Definition

`assets/mymod/gui/example_menu.json`:

```json
{
  "layout": [200, 150],
  "background": {
    "texture": "mymod:textures/gui/menu_bg.png",
    "u": 0, "v": 0,
    "regionWidth": 200, "regionHeight": 150,
    "textureWidth": 256, "textureHeight": 256
  },
  "padding": 15,
  "spacing": 8,
  "alignment": ["centre", "start"],
  "should_pause": true,
  "children": [
    {
      "layout": [170, 20],
      "background": [0, 0, 0, 0],
      "text": "gui.mymod.title",
      "text_alignment": ["centre", "centre"]
    },
    {
      "layout": [120, 24],
      "background": [80, 80, 80],
      "hover_background": [100, 100, 100],
      "press_background": [60, 60, 60],
      "text": "gui.mymod.play",
      "script": "mymod:play_button",
      "requires_new_row": true
    },
    {
      "layout": [120, 24],
      "background": [80, 80, 80],
      "hover_background": [100, 100, 100],
      "press_background": [60, 60, 60],
      "text": "gui.mymod.quit",
      "on_click": "/quit",
      "requires_new_row": true
    }
  ]
}
```

### Attached Lua Script

`assets/mymod/script/play_button.lua`:

```lua
function onClick(self, mouseX, mouseY, button)
    local mc = self:minecraft()
    mc:sendMessage("Starting game...", false)
    mc:playSound("minecraft:ui.button.click", 1.0, 1.0)
    self:closeScreen()
end
```

### Opening the Screen

`assets/mymod/script/open_menu.lua`:

```lua
-- Run with: /amblescript execute mymod:open_menu

function onExecute(mc)
    mc:displayScreen("mymod:example_menu")
end
```

---

## See Also

- [Lua Scripting System](LUA_SCRIPTING.md) - Full Lua API documentation
- [AmbleGuiRegistry](src/main/java/dev/amble/lib/client/gui/registry/AmbleGuiRegistry.java) - Java registry source
