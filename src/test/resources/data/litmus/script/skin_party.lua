-- Skin Party Script: Shuffle everyone's skins randomly!
-- Run with: /serverscript execute litmus:skin_party
--
-- This script swaps everyone's skins around for fun.
-- Great for events and parties!

function onExecute(mc, args)
    local players = mc:allPlayers()
    local count = #players
    
    if count < 2 then
        mc:sendMessage("§cNeed at least 2 players for a skin party!", false)
        return
    end
    
    mc:broadcast("§d§l✨ SKIN PARTY! ✨")
    mc:broadcast("§7Everyone's skins are being shuffled...")
    
    -- Collect all player names and UUIDs
    local playerData = {}
    for _, player in ipairs(players) do
        table.insert(playerData, {
            name = player:name(),
            uuid = player:uuid()
        })
    end
    
    -- Create a shuffled copy of names for skin sources
    local skinSources = {}
    for _, data in ipairs(playerData) do
        table.insert(skinSources, data.name)
    end
    
    -- Fisher-Yates shuffle
    math.randomseed(os.time())
    for i = #skinSources, 2, -1 do
        local j = math.random(i)
        skinSources[i], skinSources[j] = skinSources[j], skinSources[i]
    end
    
    -- Apply shuffled skins
    local success = 0
    for i, data in ipairs(playerData) do
        local skinSource = skinSources[i]
        
        -- Don't give someone their own skin
        if skinSource == data.name then
            -- Swap with next person
            local nextIdx = (i % #skinSources) + 1
            skinSources[i], skinSources[nextIdx] = skinSources[nextIdx], skinSources[i]
            skinSource = skinSources[i]
        end
        
        if mc:setSkin(data.name, skinSource) then
            mc:broadcastToPlayer(data.name, "§dYou now look like §e" .. skinSource .. "§d!", false)
            success = success + 1
        end
    end
    
    mc:broadcast("§d§l✨ " .. success .. " skins shuffled! ✨")
    mc:broadcast("§7Run the command again to reshuffle!")
    mc:log("Skin party: shuffled " .. success .. " skins")
end
