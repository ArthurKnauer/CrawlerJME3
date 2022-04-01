local FloorPlanBuilder = luajava.bindClass("architect.floorplan.FloorPlanBuilder")
local FloorPlanPolyCreator = luajava.bindClass("architect.floorplan.FloorPlanPolyCreator")
local SnapGrid = luajava.bindClass("architect.snapgrid.SnapGrid")
local RoomType = luajava.bindClass("architect.room.RoomType")
local RoomsToBuildBuilder = luajava.bindClass("architect.floorplan.RoomsToBuild$Builder")
local FloorPlanAttribsBuilder = luajava.bindClass("architect.floorplan.FloorPlanAttribs$Builder")


function createFloorPlan()
	attribs = createFloorPlanAttribs()
	snapGrid = createSnapGrid(attribs.minSnapLineDist)
	floorPlanPoly = FloorPlanPolyCreator:createPolyForApartment(attribs, snapGrid)
	roomsToBuild = createRoomsToBuild()
	
	return FloorPlanBuilder:start()
		:setFloorPlanPoly(floorPlanPoly)
		:setRoomsToBuild(roomsToBuild)
		:setFloorPlanAttribs(attribs)
		:setSnapGrid(snapGrid)
		:build()
end

function createRoomsToBuild()
	return	RoomsToBuildBuilder:start()
		:addRoomType(RoomType.LivingRoom)
		:addRoomType(RoomType.Kitchen)
		:addRoomType(RoomType.Bedroom)
		:addRoomType(RoomType.Bedroom)
		:addRoomType(RoomType.Bathroom)
		:build()
end

function createFloorPlanAttribs()
	return	FloorPlanAttribsBuilder:start()
		:setWallThickness(0.2) 
		:setWallHeight(3)
		:setWindowWidth(1.5) 
		:setWindowTop(2.8)
		:setWindowBottom(1.1)
		:setDoorWidth(1.2)
		:setDoorHeight(2.2)
		:setMinRoomSideSize(3)
		:setMinSubrectSplitLength(2)
		:setMinSubrectCut(0.1)
		:setMinWallFixLength(2.0)
		:setMinWallFixDepth(0.3) 
		:setMaxWallOptimalPosFuseDist(0.5)
		:setMinSnapLineDist(0.2)
		:setHallwayWidth(1.5)
		:setMinHallwayArea(5)
		:build()
end

function createSnapGrid(minSnapLineDist)
	return luajava.newInstance("architect.snapgrid.SnapGrid", minSnapLineDist)
end




		


