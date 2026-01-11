-- Username Changer Script
-- Handles the "Set Username" button functionality

local GuiUtils = require("litmus:gui_utils")

function onClick(self, mouseX, mouseY, button)
    local mc = self:minecraft()

    local username, input, statusText = GuiUtils.validateInput(self, "litmus:username_input", "litmus:status_text")
    if not username then return end

    -- Set the username using the Lua API
    if mc:setUsername(mc:username(), username) then
        -- Success feedback
        GuiUtils.onSuccess(mc, input, statusText, "Username set to: " .. username)
    else
        if statusText then
            statusText:setText("§cFailed to set username!")
        end
        mc:playSound("minecraft:block.note_block.bass", 0.5, 0.8)
    end
end

