-- Item Info Script: Shows detailed information about held item
-- Run with: /amblescript execute litmus:item_info
--
-- Note: Uses client-only hotbar selection features

function onExecute(mc)
    -- Check if we're on the client side
    if not mc:isClientSide() then
        mc:sendMessage("§cThis script requires client-side features!", false)
        return
    end
    
    local player = mc:player()
    local inventory = player:inventory()
    local selectedSlot = mc:selectedSlot()
    local heldItem = inventory[selectedSlot]
    
    -- Header
    mc:sendMessage("§6§l━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━", false)
    mc:sendMessage("§e§l✦ Held Item Info (Slot " .. selectedSlot .. ") ✦", false)
    mc:sendMessage("§6§l━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━", false)
    
    if heldItem:isEmpty() then
        mc:sendMessage("§8You're not holding anything!", false)
        mc:sendMessage("§6§l━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━", false)
        return
    end
    
    -- Basic info
    mc:sendMessage("§7Name: §f" .. heldItem:name(), false)
    mc:sendMessage("§7ID: §b" .. heldItem:id(), false)
    
    -- Rarity with color
    local rarity = heldItem:rarity()
    local rarityColor = "§f"
    if rarity == "uncommon" then
        rarityColor = "§e"
    elseif rarity == "rare" then
        rarityColor = "§b"
    elseif rarity == "epic" then
        rarityColor = "§d"
    end
    mc:sendMessage("§7Rarity: " .. rarityColor .. rarity:sub(1,1):upper() .. rarity:sub(2), false)
    
    -- Stack info
    local count = heldItem:count()
    local maxCount = heldItem:maxCount()
    if heldItem:isStackable() then
        mc:sendMessage("§7Stack: §f" .. count .. "§7/§f" .. maxCount, false)
    else
        mc:sendMessage("§7Stack: §8Not stackable", false)
    end
    
    -- Durability
    if heldItem:isDamageable() then
        local damage = heldItem:damage()
        local maxDamage = heldItem:maxDamage()
        local durability = maxDamage - damage
        local durabilityPercent = heldItem:durabilityPercent()
        
        -- Durability bar
        local barLength = 20
        local filledLength = math.floor(durabilityPercent * barLength)
        local durColor = "§a"
        if durabilityPercent < 0.25 then
            durColor = "§c"
        elseif durabilityPercent < 0.5 then
            durColor = "§e"
        end
        
        local durBar = durColor
        for i = 1, barLength do
            if i <= filledLength then
                durBar = durBar .. "|"
            else
                durBar = durBar .. "§8|"
            end
        end
        
        mc:sendMessage("§7Durability: " .. durBar .. " §f" .. durability .. "§7/§f" .. maxDamage, false)
    end
    
    -- Food info
    if heldItem:isFood() then
        mc:sendMessage("§6🍖 This item is edible!", false)
    end
    
    -- Custom name
    if heldItem:hasCustomName() then
        mc:sendMessage("§7Custom Named: §a✓", false)
    end
    
    -- Enchantments
    if heldItem:hasEnchantments() then
        mc:sendMessage("§6§l━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━", false)
        mc:sendMessage("§d§l✦ Enchantments ✦", false)
        mc:sendMessage("§6§l━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━", false)
        
        local enchants = heldItem:enchantments()
        for _, enchant in ipairs(enchants) do
            -- Parse enchantment:level format
            local colonPos = enchant:find(":")
            if colonPos then
                local lastColon = enchant:match(".*():")
                local enchantName = enchant:sub(1, lastColon - 1):gsub("minecraft:", ""):gsub("_", " ")
                local level = enchant:sub(lastColon + 1)
                mc:sendMessage("  §d✧ §f" .. enchantName .. " §7" .. level, false)
            else
                mc:sendMessage("  §d✧ §f" .. enchant, false)
            end
        end
    end
    
    -- NBT info
    if heldItem:hasNbt() then
        mc:sendMessage("§7Has NBT Data: §a✓", false)
    end
    
    mc:sendMessage("§6§l━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━", false)
end
