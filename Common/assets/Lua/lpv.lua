function settings(lpv) -- 'lpv' is a LPVProcessor object
	lpv:getGeometryInjector():setBlockingScale(0.001)

	lpv:getRsmInjector():setFluxScale(0.00)
	lpv:getRsmInjector():setUseCosLobe(true)

	lpv:getLightPointInjector():setFluxScale(0.001)
	lpv:getLightPointInjector():setUseCosLobe(true)

	lpv:getLightPropagator():setFluxScale(1.4)
	lpv:getLightPropagator():setSteps(32)

	lpv:setFluxScale(1.7)
end
