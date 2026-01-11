# JSON GUI System

AmbleKit provides a declarative JSON-based GUI system that lets you create Minecraft screens without writing Java code. Define layouts, colors, textures, and interactive buttons entirely in JSON files loaded from resource packs.

## Table of Contents
- [Getting Started](#getting-started)
- [File Location](#file-location)
- [JSON Structure](#json-structure)
- [Properties Reference](#properties-reference)
- [Background Types](#background-types)
- [Text Elements](#text-elements)
- [Text Input Elements](#text-input-elements)
- [Slider Elements](#slider-elements)
- [Color Picker Elements](#color-picker-elements)
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

## Text Input Elements

Create interactive text input fields using the `text_input` property. Text inputs support full keyboard navigation, text selection, copy/paste, and horizontal scrolling for long text.

### Basic Text Input

```json
{
  "id": "mymod:username_field",
  "text_input": true,
  "placeholder": "Enter username...",
  "layout": [150, 20],
  "background": [30, 30, 40]
}
```

### Text Input Properties

| Property | Type | Default | Description |
|----------|------|---------|-------------|
| `text_input` | boolean | required | Must be `true` to create a text input |
| `placeholder` | string | `""` | Placeholder text shown when empty |
| `max_length` | integer | unlimited | Maximum number of characters allowed |
| `editable` | boolean | `true` | Whether the user can edit the text |
| `text` | string | `""` | Initial text content |
| `text_alignment` | [h, v] | `["start", "centre"]` | Text alignment within the field |

### Color Customization

Text inputs support extensive color customization:

| Property | Type | Default | Description |
|----------|------|---------|-------------|
| `text_color` | [r,g,b] or [r,g,b,a] | white | Color of the input text |
| `placeholder_color` | [r,g,b] or [r,g,b,a] | gray | Color of placeholder text |
| `selection_color` | [r,g,b,a] | blue | Highlight color for selected text |
| `border_color` | [r,g,b] or [r,g,b,a] | gray | Border color when unfocused |
| `focused_border_color` | [r,g,b] or [r,g,b,a] | light blue | Border color when focused |
| `cursor_color` | [r,g,b] or [r,g,b,a] | white | Color of the text cursor |

### Styled Text Input Example

```json
{
  "id": "mymod:styled_input",
  "text_input": true,
  "placeholder": "Search...",
  "max_length": 50,
  "layout": [200, 24],
  "background": [20, 20, 30],
  "border_color": [80, 80, 100],
  "focused_border_color": [100, 140, 220],
  "selection_color": [80, 120, 200, 128],
  "placeholder_color": [100, 100, 120],
  "text_color": [255, 255, 255]
}
```

### Keyboard Shortcuts

Text inputs support standard keyboard shortcuts:

| Shortcut | Action |
|----------|--------|
| `←` / `→` | Move cursor left/right |
| `Ctrl+←` / `Ctrl+→` | Move cursor by word |
| `Shift+←` / `Shift+→` | Select characters |
| `Ctrl+Shift+←` / `Ctrl+Shift+→` | Select words |
| `Home` / `End` | Move to start/end of text |
| `Shift+Home` / `Shift+End` | Select to start/end |
| `Ctrl+A` | Select all text |
| `Ctrl+C` | Copy selected text |
| `Ctrl+X` | Cut selected text |
| `Ctrl+V` | Paste from clipboard |
| `Backspace` | Delete character before cursor |
| `Delete` | Delete character after cursor |
| `Ctrl+Backspace` | Delete word before cursor |
| `Ctrl+Delete` | Delete word after cursor |
| `Tab` | Move focus to next input |
| `Shift+Tab` | Move focus to previous input |

### Mouse Interactions

| Action | Result |
|--------|--------|
| Click | Position cursor at click location |
| Click + Drag | Select text range |
| Double-click | Select entire word |
| Shift + Click | Extend selection to click position |

### Reading Text Input Values in Lua

```lua
function onClick(self, mouseX, mouseY, button)
    local root = getRoot(self)
    local usernameInput = findById(root, "mymod:username_field")
    
    if usernameInput then
        local text = usernameInput:getText()
        if text and text ~= "" then
            -- Use the input value
            self:minecraft():sendMessage("You entered: " .. text, false)
        end
    end
end
```

### LuaElement Text Input API

| Method | Description |
|--------|-------------|
| `self:getText()` | Get the current text content |
| `self:setText(text)` | Set the text content |
| `self:getPlaceholder()` | Get the placeholder text |
| `self:setPlaceholder(text)` | Set the placeholder text |
| `self:getMaxLength()` | Get the maximum length |
| `self:setMaxLength(int)` | Set the maximum length |
| `self:isEditable()` | Check if input is editable |
| `self:setEditable(bool)` | Enable/disable editing |
| `self:isInputFocused()` | Check if input has focus |
| `self:setInputFocused(bool)` | Set focus state |
| `self:getSelectionStart()` | Get selection start index |
| `self:getSelectionEnd()` | Get selection end index |
| `self:setSelection(start, end)` | Set selection range |
| `self:selectAll()` | Select all text |
| `self:setSelectionColor(r,g,b,a)` | Set selection highlight color |
| `self:setBorderColor(r,g,b,a)` | Set border color |
| `self:setFocusedBorderColor(r,g,b,a)` | Set focused border color |
| `self:setTextColor(r,g,b,a)` | Set text color |
| `self:setPlaceholderColor(r,g,b,a)` | Set placeholder color |

---

## Slider Elements

Create interactive slider controls using the `slider` property. Sliders allow users to select a value within a configurable range by dragging a thumb along a track.

### Basic Slider

```json
{
  "id": "mymod:volume_slider",
  "slider": true,
  "min": 0,
  "max": 100,
  "value": 50,
  "layout": [150, 20],
  "background": [30, 30, 40]
}
```

### Slider Properties

| Property | Type | Default | Description |
|----------|------|---------|-------------|
| `slider` | boolean | required | Must be `true` to create a slider |
| `min` | float | `0` | Minimum value |
| `max` | float | `1` | Maximum value |
| `value` | float | `0` | Initial value |
| `step` | float | `0` | Step increment for snapping (0 = continuous) |

### Visual Customization

| Property | Type | Default | Description |
|----------|------|---------|-------------|
| `track_color` | [r,g,b] or [r,g,b,a] | dark gray | Color of the unfilled track |
| `track_filled_color` | [r,g,b] or [r,g,b,a] | blue | Color of the filled track portion |
| `thumb_color` | [r,g,b] or [r,g,b,a] | light gray | Color of the thumb |
| `thumb_hover_color` | [r,g,b] or [r,g,b,a] | white | Color of thumb when hovered |
| `border_color` | [r,g,b] or [r,g,b,a] | gray | Border color when unfocused |
| `focused_border_color` | [r,g,b] or [r,g,b,a] | light blue | Border color when focused |
| `track_height` | integer | `4` | Height of the track in pixels |
| `thumb_width` | integer | `8` | Width of the thumb in pixels |
| `thumb_height` | integer | `16` | Height of the thumb in pixels |

### Styled Slider Example

```json
{
  "id": "mymod:brightness_slider",
  "slider": true,
  "min": 0,
  "max": 100,
  "value": 75,
  "step": 5,
  "layout": [200, 24],
  "background": [20, 20, 30],
  "track_color": [40, 40, 50],
  "track_filled_color": [80, 140, 200],
  "thumb_color": [180, 180, 200],
  "thumb_hover_color": [220, 220, 255],
  "border_color": [60, 60, 80],
  "focused_border_color": [100, 140, 220],
  "script": "mymod:brightness_handler"
}
```

### Keyboard Controls

| Key | Action |
|-----|--------|
| `←` / `↓` | Decrease value by step (or 1%) |
| `→` / `↑` | Increase value by step (or 1%) |
| `Ctrl+←` / `Ctrl+↓` | Decrease by 10× step |
| `Ctrl+→` / `Ctrl+↑` | Increase by 10× step |
| `Home` | Set to minimum value |
| `End` | Set to maximum value |
| `Tab` | Move focus to next element |

### Reading Slider Values in Lua

```lua
function onValueChanged(self, value)
    -- Called whenever the slider value changes
    local mc = self:minecraft()
    mc:sendMessage("Value changed to: " .. value, false)
end

function onClick(self, mouseX, mouseY, button)
    local root = getRoot(self)
    local slider = findById(root, "mymod:volume_slider")
    
    if slider then
        local value = slider:getValue()
        local min = slider:getMin()
        local max = slider:getMax()
    end
end
```

### LuaElement Slider API

| Method | Description |
|--------|-------------|
| `self:getValue()` | Get the current value |
| `self:setValue(value)` | Set the value (clamped to min/max) |
| `self:getMin()` | Get the minimum value |
| `self:setMin(min)` | Set the minimum value |
| `self:getMax()` | Get the maximum value |
| `self:setMax(max)` | Set the maximum value |
| `self:getStep()` | Get the step increment |
| `self:setStep(step)` | Set the step increment |

---

## Color Picker Elements

Create interactive color pickers using the `color_picker` property. Color pickers display a small color swatch that expands into a full color selection popup with a hue bar, saturation/value square, and input fields.

### Basic Color Picker

```json
{
  "id": "mymod:text_color",
  "color_picker": true,
  "initial_color": [255, 128, 0],
  "layout": [24, 24],
  "background": [30, 30, 40]
}
```

### Color Picker Properties

| Property | Type | Default | Description |
|----------|------|---------|-------------|
| `color_picker` | boolean | required | Must be `true` to create a color picker |
| `initial_color` | [r,g,b] or [r,g,b,a] | white | Initial color value |
| `include_alpha` | boolean | `false` | Whether to show an alpha slider |

### Visual Customization

| Property | Type | Default | Description |
|----------|------|---------|-------------|
| `border_collapsed` | [r,g,b] or [r,g,b,a] | gray | Border color when collapsed |
| `border_collapsed_hover` | [r,g,b] or [r,g,b,a] | light gray | Border color when hovered |
| `border_expanded` | [r,g,b] or [r,g,b,a] | dark gray | Border color of expanded popup |
| `background_expanded` | [r,g,b,a] | dark semi-transparent | Background of expanded popup |
| `popup_width` | integer | `180` | Width of expanded popup in pixels |
| `popup_height` | integer | `150` | Height of expanded popup in pixels |

### Color Picker with Alpha

```json
{
  "id": "mymod:highlight_color",
  "color_picker": true,
  "initial_color": [255, 255, 0, 128],
  "include_alpha": true,
  "layout": [30, 30],
  "background": [25, 25, 35],
  "border_collapsed": [70, 70, 90],
  "border_collapsed_hover": [100, 100, 140],
  "background_expanded": [35, 35, 50, 245],
  "popup_width": 200,
  "popup_height": 160
}
```

### Expanded Popup Features

When expanded, the color picker displays:

1. **Saturation/Value Square** - Click and drag to select saturation (horizontal) and brightness (vertical)
2. **Hue Bar** - Vertical rainbow bar for selecting the base hue
3. **Alpha Bar** (optional) - Horizontal bar for selecting transparency
4. **Hex Input** - Text field accepting `RRGGBB` or `RRGGBBAA` format
5. **RGB/A Inputs** - Individual numeric fields for each color component (auto-clamps 0-255)

### Keyboard Controls

| Key | Action |
|-----|--------|
| `Escape` | Close the expanded popup |
| `Enter` | Apply changes and close popup |
| `Tab` | Cycle through input fields |
| `Shift+Tab` | Cycle backwards through input fields |

### Reading Color Values in Lua

```lua
function onColorChanged(self, r, g, b, a)
    -- Called whenever the color changes
    local hex = self:getColorHex()
    local mc = self:minecraft()
    mc:sendMessage("Color changed to #" .. hex, false)
end

function onClick(self, mouseX, mouseY, button)
    local root = getRoot(self)
    local picker = findById(root, "mymod:text_color")
    
    if picker then
        -- Get color as hex string
        local hex = picker:getColorHex()
        
        -- Get color as RGBA array
        local rgba = picker:getColorRGBA()
        local r, g, b, a = rgba[1], rgba[2], rgba[3], rgba[4]
        
        -- Set color programmatically
        picker:setColorRGBA(255, 0, 128, 255)
        -- Or by hex
        picker:setColorHex("FF0080")
    end
end
```

### LuaElement Color Picker API

| Method | Description |
|--------|-------------|
| `self:getColorRGBA()` | Get color as `{r, g, b, a}` array (0-255) |
| `self:setColorRGBA(r,g,b,a)` | Set color from RGBA values |
| `self:getColorHex()` | Get color as hex string (`RRGGBB` or `RRGGBBAA`) |
| `self:setColorHex(hex)` | Set color from hex string |
| `self:isPickerExpanded()` | Check if popup is open |
| `self:setPickerExpanded(bool)` | Open or close the popup |
| `self:isIncludeAlpha()` | Check if alpha slider is shown |
| `self:setIncludeAlpha(bool)` | Show/hide alpha slider |

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
function onDisplay(self)
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
| `onAttached(self)` | When script is attached during JSON parsing (GUI tree not yet built) | `self` |
| `onDisplay(self)` | On first render when GUI tree is fully built | `self` |
| `onClick(self, mouseX, mouseY, button)` | Mouse button pressed | `self`, coordinates, button (0=left, 1=right) |
| `onRelease(self, mouseX, mouseY, button)` | Mouse button released | `self`, coordinates, button |
| `onHover(self, mouseX, mouseY)` | Mouse hovering over element | `self`, coordinates |

**Note:** Use `onDisplay` for operations that require traversing the GUI tree (finding elements by ID, accessing parent/children). Use `onAttached` only for early setup that doesn't depend on other elements.

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
| `self:getText()` | Get text content (text/text input elements) |
| `self:setText(text)` | Set text content (text/text input elements) |
| `self:closeScreen()` | Close the current screen |
| `self:minecraft()` | Get MinecraftData for world/player access |

#### Text Input Methods

These methods only work on `AmbleTextInput` elements:

| Method | Description |
|--------|-------------|
| `self:getPlaceholder()` | Get placeholder text |
| `self:setPlaceholder(text)` | Set placeholder text |
| `self:getMaxLength()` | Get maximum character limit |
| `self:setMaxLength(int)` | Set maximum character limit |
| `self:isEditable()` | Check if input is editable |
| `self:setEditable(bool)` | Enable/disable editing |
| `self:isInputFocused()` | Check if input has focus |
| `self:setInputFocused(bool)` | Set focus state |
| `self:getSelectionStart()` | Get selection start index |
| `self:getSelectionEnd()` | Get selection end index |
| `self:setSelection(start, end)` | Set selection range |
| `self:selectAll()` | Select all text |
| `self:setSelectionColor(r,g,b,a)` | Set selection highlight color |
| `self:setBorderColor(r,g,b,a)` | Set unfocused border color |
| `self:setFocusedBorderColor(r,g,b,a)` | Set focused border color |
| `self:setTextColor(r,g,b,a)` | Set text color |
| `self:setPlaceholderColor(r,g,b,a)` | Set placeholder text color |

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

function onDisplay(self)
    -- Called when the GUI is first displayed (tree is built)
    print("Button displayed: " .. self:id())
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
