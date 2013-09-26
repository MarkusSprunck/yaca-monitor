var g_options = new function Parameter() {
    return {
	// Parameter for n-body simulation
	SPHERE_RADIUS : 900,
	SPHERE_RADIUS_MINIMUM : 20,
	CHARGE : 300,
	GRAVITY : 1000,
	THETA : 0.8,
	DISTANCE : 160,
	SPRING : 20,

	// Parameters for webGL rendering
	RUN_IMPORT : true,
	RUN_IMPORT_INTERVAL : 2000,
	RUN_IMPORT_FILTER : "^((?!(Thread.run)).)*$",
	FILTER_CLUSTER : true,	
	RUN_SIMULATION : true,
	DISPLAY_DIRECTIONS : true,
	DISPLAY_CUBE : true,
	DISPLAY_NAMES : false,
	RENDER_THRESHOLD : 15.0
    };
};