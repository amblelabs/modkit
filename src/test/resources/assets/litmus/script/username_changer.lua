-- Username Changer Script
-- Handles the "Set Username" button functionality with color support

local GuiUtils = require("litmus:gui_utils")

function onClick(self, mouseX, mouseY, button)
    local mc = self:minecraft()

    local username, input, statusText = GuiUtils.validateInput(self, "litmus:username_input", "litmus:status_text")
    if not username then return end

    -- Get color from color picker
    local root = getRoot(self)
    local colorPicker = findById(root, "litmus:name_color_picker")

    local colorHex = "FFFFFF"
    if colorPicker then
        colorHex = colorPicker:getColorHex()
    end

    -- Build JSON text component with the selected color
    local jsonText = '{"text":"' .. username .. '","color":"#' .. colorHex .. '"}'

    -- Set the username using the Lua API with JSON text
    if mc:setUsernameJson(mc:username(), jsonText) then
        -- Success feedback
        GuiUtils.onSuccess(mc, input, statusText, "Username set to: §#" .. colorHex .. username)
    else
        if statusText then
            statusText:setText("§cFailed to set username!")
        end
        mc:playSound("minecraft:block.note_block.bass", 0.5, 0.8)
    end
end

