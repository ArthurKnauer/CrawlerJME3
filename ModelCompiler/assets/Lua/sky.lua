function setupCalendar(calendar)	-- expects Calendar object
	year = 2016
	month = 6
	day = 12
	hour = 17
	minute = 0
	second = 0
	
	newTime = luajava.newInstance("java.util.GregorianCalendar", year, month, day, hour, minute, second)
	calendar:setTime(newTime:getTime())
end

function setupAtmosphereShader(shader)	-- expects AtmosphereShader object
	shader:setFluxScale(2)
	
	shader:setRadiusEarth(6360e3f)
	shader:setRadiusAtmosphereSquared(425104e8)
	shader:setRayleighScaleHeight(5994)
	shader:setMieScaleHeight(2000)
	shader:setSunIntensity(15)
	shader:setMieGFactor(0.76f)
	shader:setRayleighWavelengths(vector(5.5e-6f, 13.0e-6f, 24.4e-6f))
	shader:setMieWavelengths(vector(21e-6f, 21e-6f, 21e-6f))
	shader:setSunDirection(vector(0, 1, 0))
	shader:setSamples(8)
	shader:setLightSamples(8)
end

function vector(x, y, z)
	return luajava.newInstance("com.jme3.math.Vector3f", x, y, z)
end



