-- Skin Changer Slim Toggle Script
-- Handles the "Toggle Slim" button functionality

local GuiUtils = require("litmus:gui_utils")

-- Track the current slim mode state
local slimMode = false

-- Update the button text to reflect current mode
local function updateButtonText(self)
    local textChild = self:findFirstText()
    if textChild then
        if slimMode then
            textChild:setText("§fModel: Slim")
        else
            textChild:setText("§fModel: Classic")
        end
    end
end

function onDisplay(self)
    -- Set initial button text
    updateButtonText(self)
end

function onClick(self, mouseX, mouseY, button)
    local root = GuiUtils.getRoot(self)
    local mc = self:minecraft()
    local statusText = GuiUtils.findById(root, "litmus:status_text")

    -- Toggle the slim mode
    slimMode = not slimMode

    -- Run the command
    local slimValue = slimMode and "true" or "false"
    local command = "/amblekit skin @p slim " .. slimValue
    mc:runCommand(command)

    -- Update button text
    updateButtonText(self)

    -- Update status
    if statusText then
        local modelName = slimMode and "Slim" or "Classic"
        statusText:setText("§aModel: " .. modelName)
    end

    -- Play sound
    mc:playSound("minecraft:ui.button.click", 1.0, 1.0)
end

