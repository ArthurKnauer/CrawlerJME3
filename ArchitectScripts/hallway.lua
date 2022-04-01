local ArchitectBuilder = luajava.bindClass("architect.ArchitectBuilder")

function createArchitect()
	return ArchitectBuilder:start()
		:addWindowCreator()		
		:build()
end


		


