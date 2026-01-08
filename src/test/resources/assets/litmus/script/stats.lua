-- Stats Script: Shows player info and nearby entities
-- Run with: /amblescript execute litmus:stats
--
-- Note: minecraft data is passed as first argument to callbacks

function onExecute(mc)
    -- Get player info
    local player = mc:player()
    local pos = player:position()
    local health = player:health()
    local food = player:foodLevel()
    
    -- Username is client-only, use player name on server
    local username = player:name()
    if mc:isClientSide() then
        username = mc:username()
    end
    
    -- Selected slot is client-only
    local slot = "N/A"
    if mc:isClientSide() then
        slot = tostring(mc:selectedSlot())
    end
    
    -- Send a stylish header
    mc:sendMessage("§6§l━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━", false)
    mc:sendMessage("§e§l✦ Player Stats for §f" .. username .. " §e§l✦", false)
    mc:sendMessage("§6§l━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━", false)
    
    -- Health bar visualization
    local maxHearts = 10
    local currentHearts = math.floor(health / 2)
    local heartBar = ""
    for i = 1, maxHearts do
        if i <= currentHearts then
            heartBar = heartBar .. "§c❤"
        else
            heartBar = heartBar .. "§8❤"
        end
    end
    mc:sendMessage("§7Health: " .. heartBar .. " §f(" .. string.format("%.1f", health) .. ")", false)
    
    -- Food bar visualization
    local maxFood = 10
    local currentFood = math.floor(food / 2)
    local foodBar = ""
    for i = 1, maxFood do
        if i <= currentFood then
            foodBar = foodBar .. "§6🍖"
        else
            foodBar = foodBar .. "§8🍖"
        end
    end
    mc:sendMessage("§7Hunger: " .. foodBar .. " §f(" .. food .. ")", false)
    
    -- Position
    mc:sendMessage("§7Position: §b" .. string.format("%.1f", pos.x) .. "§7, §a" .. string.format("%.1f", pos.y) .. "§7, §d" .. string.format("%.1f", pos.z), false)
    mc:sendMessage("§7Selected Slot: §e" .. slot, false)
    
    -- Count nearby entities
    mc:sendMessage("§6§l━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━", false)
    mc:sendMessage("§e§l✦ Nearby Entities ✦", false)
    mc:sendMessage("§6§l━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━", false)
    
    local entities = mc:entities()
    local entityCounts = {}
    local totalCount = 0
    
    for _, entity in pairs(entities) do
        local entityType = entity:type()
        -- Skip the player themselves
        if not (entity:isPlayer() and entity:name() == username) then
            entityCounts[entityType] = (entityCounts[entityType] or 0) + 1
            totalCount = totalCount + 1
        end
    end
    
    -- Display entity counts (limit to first 8 types)
    local displayed = 0
    for entityType, count in pairs(entityCounts) do
        if displayed < 8 then
            -- Clean up the entity type name
            local cleanName = entityType:gsub("minecraft:", ""):gsub("_", " ")
            mc:sendMessage("§7• §f" .. cleanName .. "§7: §a" .. count, false)
            displayed = displayed + 1
        end
    end
    
    if displayed == 0 then
        mc:sendMessage("§7No entities nearby!", false)
    elseif totalCount > displayed then
        mc:sendMessage("§8...and " .. (totalCount - displayed) .. " more types", false)
    end
    
    mc:sendMessage("§6§l━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━", false)
    mc:sendMessage("§7Total entities: §e" .. totalCount, false)
    mc:sendMessage("§6§l━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━", false)
end
