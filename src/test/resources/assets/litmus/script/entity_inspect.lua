-- Entity Inspect Script: Shows info about the entity you're looking at
-- Run with: /amblescript execute litmus:entity_inspect
--
-- Note: Uses client-only lookingAtEntity feature

function onExecute(mc, args)
    local target = nil
    
    -- lookingAtEntity is client-only
    if mc:isClientSide() then
        target = mc:lookingAtEntity()
    end
    
    -- Header
    mc:sendMessage("§6§l━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━", false)
    mc:sendMessage("§e§l✦ Entity Inspector ✦", false)
    mc:sendMessage("§6§l━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━", false)
    
    if not target then
        mc:sendMessage("§8Look at an entity and run this script!", false)
        
        -- Show nearest entity instead
        local nearest = mc:nearestEntity(10)
        if nearest then
            mc:sendMessage("", false)
            mc:sendMessage("§7Nearest entity (within 10 blocks):", false)
            mc:sendMessage("§a→ §f" .. nearest:name() .. " §7(" .. nearest:type():gsub("minecraft:", "") .. ")", false)
            
            local player = mc:player()
            local playerPos = player:position()
            local distance = nearest:distanceTo(playerPos.x, playerPos.y, playerPos.z)
            mc:sendMessage("§7  Distance: §e" .. string.format("%.1f", distance) .. " blocks", false)
        else
            mc:sendMessage("§8No entities within 10 blocks!", false)
        end
        
        mc:sendMessage("§6§l━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━", false)
        return
    end
    
    -- Basic info
    mc:sendMessage("§7Name: §f" .. target:name(), false)
    mc:sendMessage("§7Type: §b" .. target:type():gsub("minecraft:", ""), false)
    mc:sendMessage("§7UUID: §8" .. target:uuid():sub(1, 8) .. "...", false)
    
    -- Position
    local pos = target:position()
    mc:sendMessage("§7Position: §f" .. string.format("%.1f", pos.x) .. "§7, §f" .. string.format("%.1f", pos.y) .. "§7, §f" .. string.format("%.1f", pos.z), false)
    
    -- Distance from player
    local player = mc:player()
    local playerPos = player:position()
    local distance = target:distanceTo(playerPos.x, playerPos.y, playerPos.z)
    mc:sendMessage("§7Distance: §e" .. string.format("%.1f", distance) .. " blocks", false)
    
    -- Health (if living entity)
    local health = target:health()
    local maxHealth = target:maxHealth()
    if health >= 0 then
        -- Health bar
        local barLength = 15
        local healthPercent = health / maxHealth
        local filledLength = math.floor(healthPercent * barLength)
        local healthColor = "§a"
        if healthPercent < 0.25 then
            healthColor = "§c"
        elseif healthPercent < 0.5 then
            healthColor = "§e"
        end
        
        local healthBar = healthColor
        for i = 1, barLength do
            if i <= filledLength then
                healthBar = healthBar .. "❤"
            else
                healthBar = healthBar .. "§8❤"
            end
        end
        
        mc:sendMessage("§7Health: " .. healthBar .. " §f" .. string.format("%.1f", health) .. "§7/§f" .. string.format("%.0f", maxHealth), false)
    end
    
    -- Armor
    local armor = target:armorValue()
    if armor > 0 then
        mc:sendMessage("§9Armor: §f" .. armor, false)
    end
    
    mc:sendMessage("§6§l━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━", false)
    mc:sendMessage("§e§l✦ Entity State ✦", false)
    mc:sendMessage("§6§l━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━", false)
    
    -- States
    local states = {}
    
    if target:isPlayer() then
        table.insert(states, "§a✓ Player")
    end
    
    if target:isAlive() then
        table.insert(states, "§a✓ Alive")
    else
        table.insert(states, "§c✗ Dead")
    end
    
    if target:isOnGround() then
        table.insert(states, "§7• On Ground")
    else
        table.insert(states, "§7• Airborne")
    end
    
    if target:isSprinting() then
        table.insert(states, "§e• Sprinting")
    end
    
    if target:isSneaking() then
        table.insert(states, "§7• Sneaking")
    end
    
    if target:isTouchingWater() then
        table.insert(states, "§b• In Water")
    end
    
    if target:isSwimming() then
        table.insert(states, "§b• Swimming")
    end
    
    if target:isOnFire() then
        table.insert(states, "§c🔥 On Fire!")
    end
    
    if target:isInvisible() then
        table.insert(states, "§7• Invisible")
    end
    
    if target:isGlowing() then
        table.insert(states, "§e• Glowing")
    end
    
    for _, state in ipairs(states) do
        mc:sendMessage("  " .. state, false)
    end
    
    -- Velocity
    local vel = target:velocity()
    local speed = math.sqrt(vel.x * vel.x + vel.z * vel.z)
    mc:sendMessage("§7Speed: §f" .. string.format("%.2f", speed * 20) .. " §7blocks/sec", false)
    
    -- Rotation
    mc:sendMessage("§7Looking: §fYaw " .. string.format("%.0f", target:yaw()) .. "°, Pitch " .. string.format("%.0f", target:pitch()) .. "°", false)
    
    -- Age
    mc:sendMessage("§7Age: §f" .. target:age() .. " ticks §8(" .. string.format("%.1f", target:age() / 20) .. "s)", false)
    
    -- Effects
    local effects = target:effects()
    if #effects > 0 then
        mc:sendMessage("§6§l━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━", false)
        mc:sendMessage("§d§l✦ Status Effects ✦", false)
        for _, effect in ipairs(effects) do
            local cleanEffect = effect:gsub("minecraft:", ""):gsub("_", " ")
            mc:sendMessage("  §d✧ §f" .. cleanEffect, false)
        end
    end
    
    mc:sendMessage("§6§l━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━", false)
end
