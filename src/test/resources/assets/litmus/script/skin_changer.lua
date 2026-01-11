-- Skin Changer Script
-- Handles the "Set Skin" button functionality

-- Helper function to find an element by ID recursively
local function findById(element, targetId)
    local elemId = element:id()
    if elemId == targetId then
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

function onDisplay(self)
    -- Update the player name display
    local root = getRoot(self)
    local mc = self:minecraft()

    local playerNameText = findById(root, "litmus:player_name_text")
    if playerNameText then
        local username = mc:username()
        playerNameText:setText("Current: " .. username)
    end
end

function onClick(self, mouseX, mouseY, button)
    local root = getRoot(self)
    local mc = self:minecraft()

    -- Find the text input
    local usernameInput = findById(root, "litmus:username_input")
    local statusText = findById(root, "litmus:status_text")

    if not usernameInput then
        mc:sendMessage("§cError: Could not find username input!", false)
        return
    end

    local username = usernameInput:getText()

    -- Validate input
    if not username or username == "" then
        if statusText then
            statusText:setText("§cEnter a username!")
        end
        mc:playSound("minecraft:block.note_block.bass", 0.5, 0.8)
        return
    end

    -- Trim whitespace (basic)
    username = username:match("^%s*(.-)%s*$")

    if username == "" then
        if statusText then
            statusText:setText("§cEnter a username!")
        end
        mc:playSound("minecraft:block.note_block.bass", 0.5, 0.8)
        return
    end

    -- Run the skin command
    local command = "/amblekit skin @p set " .. username
    mc:runCommand(command)

    -- Update status
    if statusText then
        statusText:setText("§aSkin set to: " .. username)
    end

    -- Play success sound
    mc:playSound("minecraft:ui.button.click", 1.0, 1.2)

    -- Clear the input
    usernameInput:setText("")
end

