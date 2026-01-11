-- Skin Changer Script
-- Handles the "Set Skin" button functionality

local GuiUtils = require("litmus:gui_utils")

function onDisplay(self)
    -- Update the player name display
    local root = GuiUtils.getRoot(self)
    local mc = self:minecraft()

    local playerNameText = GuiUtils.findById(root, "litmus:player_name_text")
    if playerNameText then
        local username = mc:username()
        playerNameText:setText("Current: " .. username)
    end
end

function onClick(self, mouseX, mouseY, button)
    local mc = self:minecraft()

    local username, input, statusText = GuiUtils.validateInput(self, "litmus:username_input", "litmus:status_text")
    if not username then return end

    -- Run the skin command
    local command = "/amblekit skin @p set " .. username
    mc:runCommand(command)

    -- Success feedback
    GuiUtils.onSuccess(mc, input, statusText, "Skin set to: " .. username)
end

