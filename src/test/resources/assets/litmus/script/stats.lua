-- Stats Script: Shows player info and nearby entities
-- Run with: /amblekit execute litmus:stats

function onExecute()
	-- Get player info
	local player = minecraft:player()
	local username = minecraft:username()
	local pos = player:position()
	local health = player:health()
	local food = player:foodLevel()
	local slot = minecraft:selectedSlot()
	
	-- Send a stylish header
	minecraft:sendMessage("§6§l━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━", false)
	minecraft:sendMessage("§e§l✦ Player Stats for §f" .. username .. " §e§l✦", false)
	minecraft:sendMessage("§6§l━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━", false)
	
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
	minecraft:sendMessage("§7Health: " .. heartBar .. " §f(" .. string.format("%.1f", health) .. ")", false)
	
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
	minecraft:sendMessage("§7Hunger: " .. foodBar .. " §f(" .. food .. ")", false)
	
	-- Position
	minecraft:sendMessage("§7Position: §b" .. string.format("%.1f", pos.x) .. "§7, §a" .. string.format("%.1f", pos.y) .. "§7, §d" .. string.format("%.1f", pos.z), false)
	minecraft:sendMessage("§7Selected Slot: §e" .. slot, false)
	
	-- Count nearby entities
	minecraft:sendMessage("§6§l━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━", false)
	minecraft:sendMessage("§e§l✦ Nearby Entities ✦", false)
	minecraft:sendMessage("§6§l━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━", false)
	
	local entities = minecraft:entities()
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
			minecraft:sendMessage("§7• §f" .. cleanName .. "§7: §a" .. count, false)
			displayed = displayed + 1
		end
	end
	
	if displayed == 0 then
		minecraft:sendMessage("§7No entities nearby!", false)
	elseif totalCount > displayed then
		minecraft:sendMessage("§8...and " .. (totalCount - displayed) .. " more types", false)
	end
	
	minecraft:sendMessage("§6§l━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━", false)
	minecraft:sendMessage("§7Total entities: §e" .. totalCount, false)
	minecraft:sendMessage("§6§l━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━", false)
end
