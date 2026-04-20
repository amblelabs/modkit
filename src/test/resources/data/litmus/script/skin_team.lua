-- Skin Team Script: Assign team-based skins to players
-- Enable with: /serverscript enable litmus:skin_team
-- Disable with: /serverscript disable litmus:skin_team
-- Execute with: /serverscript execute litmus:skin_team
--
-- This script assigns players to teams based on join order
-- and gives them matching team skins.

-- Team configurations
local TEAMS = {
    {
        name = "Red Team",
        color = "§c",
        skin = "Notch"  -- Team captain skin
    },
    {
        name = "Blue Team", 
        color = "§9",
        skin = "jeb_"   -- Team captain skin
    },
    {
        name = "Green Team",
        color = "§a",
        skin = "Dinnerbone"
    },
    {
        name = "Yellow Team",
        color = "§e",
        skin = "Dream"
    }
}

-- Track team assignments
local playerTeams = {}  -- uuid -> team index
local teamCounts = {}   -- team index -> player count

function initTeamCounts()
    teamCounts = {}
    for i = 1, #TEAMS do
        teamCounts[i] = 0
    end
end

function getSmallestTeam()
    local minCount = 999999
    local minTeam = 1
    
    for i, count in ipairs(teamCounts) do
        if count < minCount then
            minCount = count
            minTeam = i
        end
    end
    
    return minTeam
end

function onExecute(mc, args)
    mc:sendMessage("§6§l━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━", false)
    mc:sendMessage("§e§l✦ Team Skin Status ✦", false)
    mc:sendMessage("§6§l━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━", false)
    
    -- Show team rosters
    for i, team in ipairs(TEAMS) do
        local count = teamCounts[i] or 0
        mc:sendMessage(team.color .. "§l" .. team.name .. " §7(" .. count .. " players)", false)
        
        -- List players on this team
        for uuid, teamIdx in pairs(playerTeams) do
            if teamIdx == i then
                -- Find player name by UUID
                for _, player in ipairs(mc:allPlayers()) do
                    if player:uuid() == uuid then
                        mc:sendMessage("  " .. team.color .. "• §f" .. player:name(), false)
                        break
                    end
                end
            end
        end
    end
    
    mc:sendMessage("§6§l━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━", false)
end

function onEnable(mc)
    initTeamCounts()
    playerTeams = {}
    
    -- Assign existing players to teams
    local players = mc:allPlayers()
    for _, player in ipairs(players) do
        assignPlayerToTeam(mc, player:name(), player:uuid())
    end
    
    mc:broadcast("§e[Teams] §aTeam mode enabled! Players assigned to teams.")
    mc:log("Team Skin script enabled with " .. #players .. " players")
end

function assignPlayerToTeam(mc, playerName, uuid)
    -- Skip if already assigned
    if playerTeams[uuid] then
        return
    end
    
    -- Assign to smallest team
    local teamIdx = getSmallestTeam()
    local team = TEAMS[teamIdx]
    
    playerTeams[uuid] = teamIdx
    teamCounts[teamIdx] = (teamCounts[teamIdx] or 0) + 1
    
    -- Apply team skin
    if mc:setSkinByUuid(uuid, team.skin) then
        mc:broadcastToPlayer(playerName, team.color .. "§lYou joined " .. team.name .. "!", false)
        mc:log("Assigned " .. playerName .. " to " .. team.name)
    else
        mc:logWarn("Failed to apply team skin for " .. playerName)
    end
end

-- Track player count to detect new joins
local lastPlayerCount = 0

function onTick(mc)
    local currentCount = mc:playerCount()
    
    -- Check for new players
    if currentCount > lastPlayerCount then
        local players = mc:allPlayers()
        for _, player in ipairs(players) do
            local uuid = player:uuid()
            if not playerTeams[uuid] then
                assignPlayerToTeam(mc, player:name(), uuid)
            end
        end
    end
    
    lastPlayerCount = currentCount
end

function onDisable(mc)
    -- Clear all team skins
    local cleared = 0
    for uuid, _ in pairs(playerTeams) do
        if mc:clearSkinByUuid(uuid) then
            cleared = cleared + 1
        end
    end
    
    mc:broadcast("§e[Teams] §cTeam mode disabled. Cleared " .. cleared .. " team skins.")
    mc:log("Team Skin script disabled")
    
    playerTeams = {}
    initTeamCounts()
end
