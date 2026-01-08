-- Hotbar Cycle Script: Cycles through hotbar slots with a fun animation
-- Run with: /amblescript execute litmus:hotbar_cycle
--
-- Note: Uses client-only hotbar selection features

local cycleIndex = 1
local cycleDirection = 1

function onExecute(mc)
    -- Check if we're on the client side
    if not mc:isClientSide() then
        mc:sendMessage("§cThis script requires client-side features!", false)
        return
    end
    
    local currentSlot = mc:selectedSlot()
    
    -- Calculate next slot (1-9)
    local nextSlot = currentSlot + cycleDirection
    
    if nextSlot > 9 then
        nextSlot = 1
        cycleIndex = cycleIndex + 1
    elseif nextSlot < 1 then
        nextSlot = 9
        cycleIndex = cycleIndex + 1
    end
    
    -- Select the next slot
    mc:selectSlot(nextSlot)
    
    -- Create a visual indicator
    local indicator = ""
    for i = 1, 9 do
        if i == nextSlot then
            indicator = indicator .. "§e[" .. i .. "]"
        else
            indicator = indicator .. "§7 " .. i .. " "
        end
    end
    
    mc:sendMessage("§6Hotbar: " .. indicator, true)
    
    -- Change direction every full cycle
    if cycleIndex > 2 then
        cycleDirection = -cycleDirection
        cycleIndex = 1
        mc:sendMessage("§d✦ Direction reversed! ✦", true)
    end
end
