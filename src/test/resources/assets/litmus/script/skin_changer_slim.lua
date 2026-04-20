-- Skin Changer Slim Toggle Script
-- Handles the "Toggle Slim" button functionality

-- Track the current slim mode state
local slimMode = false

-- Helper function to find an element by ID recursively
local function findById(element, targetId)
    if element:id() == targetId then
        return element
    end

    local count = element:childCount()
    for i = 0, count - 1 do
        local child = element:child(i)
        if child then
            local found = findById(child, targetId)
            if found then
                return found
            end
        end
    end

    return nil
end

-- Find the root element by traversing up from self
local function getRoot(element)
    local current = element
    while current:parent() do
        current = current:parent()
    end
    return current
end

-- Update the button text to reflect current mode
local function updateButtonText(self)
    -- Find the text child within the button
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
    local root = getRoot(self)
    local mc = self:minecraft()
    local statusText = findById(root, "litmus:status_text")

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

