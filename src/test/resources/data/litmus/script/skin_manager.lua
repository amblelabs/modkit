-- Skin Manager Script: Comprehensive skin management with tick-based monitoring
-- Enable with: /serverscript enable litmus:skin_manager
-- Disable with: /serverscript disable litmus:skin_manager
-- Execute with: /serverscript execute litmus:skin_manager
--
-- This script demonstrates:
-- - Setting skins by username and URL
-- - Checking skin status
-- - UUID-based skin operations
-- - Tick-based skin monitoring

-- Track players we've given skins to
local skinnedPlayers = {}
local tickCounter = 0

function onExecute(mc, args)
    mc:sendMessage("§6§l━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━", false)
    mc:sendMessage("§e§l✦ Skin Manager Status ✦", false)
    mc:sendMessage("§6§l━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━", false)
    
    local players = mc:allPlayerNames()
    local withSkin = 0
    local withoutSkin = 0
    
    for _, name in ipairs(players) do
        if mc:hasSkin(name) then
            withSkin = withSkin + 1
            mc:sendMessage("  §a✓ §f" .. name .. " §7(custom skin)", false)
        else
            withoutSkin = withoutSkin + 1
            mc:sendMessage("  §7○ §f" .. name .. " §8(default skin)", false)
        end
    end
    
    mc:sendMessage("§6§l━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━", false)
    mc:sendMessage("§7Custom skins: §a" .. withSkin .. "§7 | Default: §8" .. withoutSkin, false)
    mc:sendMessage("§6§l━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━", false)
end

function onEnable(mc)
    skinnedPlayers = {}
    tickCounter = 0
    mc:broadcast("§e[Skin Manager] §aEnabled! New players will receive skins.")
    mc:log("Skin Manager enabled")
end

function onTick(mc)
    tickCounter = tickCounter + 1
    
    -- Only check every 100 ticks (5 seconds)
    if tickCounter % 100 ~= 0 then
        return
    end
    
    -- Check for new players without skins
    local players = mc:allPlayers()
    
    for _, player in ipairs(players) do
        local name = player:name()
        local uuid = player:uuid()
        
        -- Skip if we've already processed this player
        if skinnedPlayers[uuid] then
            goto continue
        end
        
        -- Check if player already has a custom skin
        if not mc:hasSkinByUuid(uuid) then
            -- Give them a default "welcome" skin
            if mc:setSkinByUuid(uuid, "Steve") then
                mc:broadcastToPlayer(name, "§e[Skin Manager] §7Welcome! Default skin applied.", false)
                mc:log("Applied default skin to new player: " .. name)
            end
        end
        
        -- Mark as processed
        skinnedPlayers[uuid] = true
        
        ::continue::
    end
end

function onDisable(mc)
    -- Clear all skins when disabled
    local cleared = 0
    for _, name in ipairs(mc:allPlayerNames()) do
        if mc:hasSkin(name) then
            if mc:clearSkin(name) then
                cleared = cleared + 1
            end
        end
    end
    
    mc:broadcast("§e[Skin Manager] §cDisabled. Cleared " .. cleared .. " custom skins.")
    mc:log("Skin Manager disabled, cleared " .. cleared .. " skins")
    skinnedPlayers = {}
end
