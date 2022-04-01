local ArchitectBuilder = luajava.bindClass("architect.ArchitectBuilder")

function createArchitect()
	return ArchitectBuilder:start()
		:addFloorPlanSubdivider()
		:addProtoRoomAssigner()
		:addRoomRectAssigner()
		
		:addWallPressureBuilder(false)
		:addWallPressureRelaxer()
		:addProtrusionRemover()
		
		:addRoomTypeAssigner()
		:addWallPressureBuilder(false)
		:addWallPressureRelaxer()
		:addProtrusionRemover()
		
		:addRoomConnector()
		:addRoomMerger()
		:addProtrusionRemover()
		
		:addDoorCreator()
		:addWindowCreator()
		:addStatisticsAnalyzer()
		
		:build()
end


		


