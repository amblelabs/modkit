-- GUI Utilities Script
-- Shared helper functions for GUI scripts

local GuiUtils = {}

-- Helper function to find an element by ID recursively
function GuiUtils.findById(element, targetId)
    if element:id() == targetId then
        return element
    end

    local count = element:childCount()
    for i = 0, count - 1 do
        local child = element:child(i)
        if child then
            local found = GuiUtils.findById(child, targetId)
            if found then
                return found
            end
        end
    end

    return nil
end

-- Find the root element by traversing up from self
function GuiUtils.getRoot(element)
    local current = element
    while current:parent() do
        current = current:parent()
    end
    return current
end

-- Trim whitespace from a string
function GuiUtils.trim(str)
    if not str then return "" end
    return str:match("^%s*(.-)%s*$") or ""
end

-- Validate input and show error if empty
-- Returns the trimmed input if valid, nil if invalid
function GuiUtils.validateInput(self, inputId, statusId)
    local root = GuiUtils.getRoot(self)
    local mc = self:minecraft()

    local input = GuiUtils.findById(root, inputId)
    local statusText = statusId and GuiUtils.findById(root, statusId) or nil

    if not input then
        mc:sendMessage("§cError: Could not find input element!", false)
        return nil, nil, nil
    end

    local value = GuiUtils.trim(input:getText())

    if value == "" then
        if statusText then
            statusText:setText("§cEnter a value!")
        end
        mc:playSound("minecraft:block.note_block.bass", 0.5, 0.8)
        return nil, input, statusText
    end

    return value, input, statusText
end

-- Play success sound and clear input
function GuiUtils.onSuccess(mc, input, statusText, message)
    if statusText then
        statusText:setText("§a" .. message)
    end
    mc:playSound("minecraft:ui.button.click", 1.0, 1.2)
    if input then
        input:setText("")
    end
end

return GuiUtils

