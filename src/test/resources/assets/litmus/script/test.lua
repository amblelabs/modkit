-- Test Script: Entity Display Demo
-- This script demonstrates the AmbleEntityDisplay component
-- showing the player and nearest entity with live info updates
--
-- Note: GUI scripts receive 'self' (LuaElement) and use self:minecraft() for data

-- Helper to find a child element by ID
function findChildById(root, targetId)
    if root:id() == targetId then
        return root
    end
    for i = 0, root:childCount() - 1 do
        local child = root:child(i)
        if child then
            local found = findChildById(child, targetId)
            if found then
                return found
            end
        end
    end
    return nil
end

-- Helper to format position
function formatPos(pos)
    return string.format("%.0f, %.0f, %.0f", pos.x, pos.y, pos.z)
end

-- Helper to format health
function formatHealth(current, max)
    if current < 0 then
        return "--"
    end
    return string.format("%.1f/%.1f", current, max)
end

function onInit(self)
    local mc = self:minecraft()
    local player = mc:player()

    -- Get the root container (parent of parent since we're a child element)
    local root = self
    while root:parent() do
        root = root:parent()
    end

    -- Update player display - set UUID to local player
    local playerDisplay = findChildById(root, "litmus:player_display")
    if playerDisplay then
        playerDisplay:setEntityUuid("player")
    end

    -- Update player info
    local playerName = findChildById(root, "litmus:player_name")
    if playerName then
        playerName:setText("§b" .. mc:username())
    end

    local playerHealth = findChildById(root, "litmus:player_health")
    if playerHealth then
        local health = player:health()
        local maxHealth = player:maxHealth()
        playerHealth:setText("§c❤ §f" .. formatHealth(health, maxHealth))
    end

    local playerPos = findChildById(root, "litmus:player_pos")
    if playerPos then
        local pos = player:position()
        playerPos:setText("§7" .. formatPos(pos))
    end

    -- Find nearest entity (excluding player)
    local nearest = mc:nearestEntity(20)

    local nearestDisplay = findChildById(root, "litmus:nearest_display")
    local nearestName = findChildById(root, "litmus:nearest_name")
    local nearestType = findChildById(root, "litmus:nearest_type")
    local nearestHealth = findChildById(root, "litmus:nearest_health")
    local nearestDist = findChildById(root, "litmus:nearest_dist")

    if nearest then
        -- Set entity UUID for display
        if nearestDisplay then
            nearestDisplay:setEntityUuid(nearest:uuid())
        end

        -- Update info labels
        if nearestName then
            nearestName:setText("§e" .. nearest:name())
        end

        if nearestType then
            local entityType = nearest:type():gsub("minecraft:", "")
            nearestType:setText("§7Type: §f" .. entityType)
        end

        if nearestHealth then
            local health = nearest:health()
            local maxHealth = nearest:maxHealth()
            if health >= 0 then
                nearestHealth:setText("§c❤ §f" .. formatHealth(health, maxHealth))
            else
                nearestHealth:setText("§7No health")
            end
        end

        if nearestDist then
            local playerPos = player:position()
            local dist = nearest:distanceTo(playerPos.x, playerPos.y, playerPos.z)
            nearestDist:setText("§7Dist: §f" .. string.format("%.1f", dist) .. "m")
        end
    else
        -- No entity nearby
        if nearestDisplay then
            nearestDisplay:setEntityUuid("")
        end
        if nearestName then
            nearestName:setText("§8No entity nearby")
        end
        if nearestType then
            nearestType:setText("§7Type: §8--")
        end
        if nearestHealth then
            nearestHealth:setText("§7Health: §8--")
        end
        if nearestDist then
            nearestDist:setText("§7Dist: §8--")
        end
    end
end

function onClick(self, mouseX, mouseY, button)
    -- Refresh entity info when clicked
    onInit(self)

    local mc = self:minecraft()
    mc:playSound("minecraft:ui.button.click", 1.0, 1.0)
end

function onExecute(mc, args)
    mc:log("Entity display test script executed!")
    mc:sendMessage("§aEntity display demo - open the test GUI to see it!", false)
end
