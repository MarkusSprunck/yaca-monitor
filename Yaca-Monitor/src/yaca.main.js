/*******************************************************************************
 * Copyright (C) 2013, Markus Sprunck
 *
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *  - Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer.
 *  - Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 *  - The name of its contributor may be used to endorse or promote products
 * derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 *
 ******************************************************************************/

/**
 * Global constants
 */
var BORDER_RIGHT = 170;
var BORDER_BOTTOM = 30;
var HALF_PI = Math.PI * 0.5;
var TEXT_LENGTH = 800;
var TEXT_HEIGHT = 20;
var RUN_WEBGL_INTERVAL = 200;

/**
 * Global variables for rendering
 */
var g_panelWidthWebGL;
var g_panelHeightWebGL;
var g_updateTimerImport;
var g_updateTimerSimulation;
var g_updateTimerWebGL;
var g_scene;
var g_cube_material_solid;
var g_cube_wireframe1;
var g_cube_wireframe2;
var g_cube_wireframe3;
var g_stats;
var g_camera;
var g_renderer;
var g_control;
var g_light;
var g_container;
var g_colorHash = new HashMap();
var g_gui;
var g_gui_pid;
var g_Message = '';
var g_imported_data = false;
var g_process_id_active = '0000';
var g_old_pids = [];

/**
 * Initializes the application
 */
function initApplication() {
	"use strict";
	YACA_NodeRegexFilter = new RegExp(YACA_SimulationOptions.RUN_IMPORT_FILTER);
	loadDefultModel();
	initWebGL();

	g_updateTimerImport = setInterval(function() {
		updateTimerImport();
	}, YACA_SimulationOptions.RUN_IMPORT_INTERVAL);

	g_updateTimerWebGL = setInterval(function() {
		YACA_NBodySimulator.applyFilter();
		runSimulation(10);
		updateWebGL(YACA_NBodySimulator.node_list_visible_last, YACA_NBodySimulator.link_list_visible_last);
		updateWebGL(YACA_NBodySimulator.node_list_visible, YACA_NBodySimulator.link_list_visible);
		renderer();
		updateStatusLine();
	}, RUN_WEBGL_INTERVAL);

}

/**
 * Initialize WebGL
 */
function initWebGL() {
	"use strict";
	// Container for WebGL rendering
	g_container = document.getElementById('graphic-container');
	g_container.style.background = "#000";

	// Size of drawing
	g_panelHeightWebGL = window.outerHeight - BORDER_BOTTOM;
	g_panelWidthWebGL = window.outerWidth - BORDER_RIGHT * 2; // 
	BORDER_RIGHT = window.innerWidth - g_panelWidthWebGL;

	// Create g_camera
	g_camera = new THREE.PerspectiveCamera(40, g_panelWidthWebGL / g_panelHeightWebGL, 1, 40000);
	resetCamera();

	// Create g_scene
	g_scene = new THREE.Scene();
	g_scene.add(g_camera);

	// Create g_renderer
	if (Detector.webgl) {
		g_renderer = new THREE.WebGLRenderer({
			antialias : true
		});
	} else {
		g_container.appendChild(Detector.getWebGLErrorMessage());
		return;
	}
	g_renderer.setSize(g_panelWidthWebGL, g_panelHeightWebGL);
	g_container.appendChild(g_renderer.domElement);
	initDatGui(g_container);

	// Create g_light front
	g_light = new THREE.SpotLight(0xffffff, 1.25);
	g_light.position.set(YACA_SimulationOptions.SPHERE_RADIUS * 2, YACA_SimulationOptions.SPHERE_RADIUS * 2, 0);
	g_light.target.position.set(0, 0, 0);
	g_light.castShadow = true;
	g_scene.add(g_light);

	// Create light near the center
	var hemiLight = new THREE.HemisphereLight(0xffffff, 0xffffff, 0.6);
	hemiLight.groundColor.setHSL(0.095, 1, 0.75);
	hemiLight.position.set(0, 200, 0);
	g_scene.add(hemiLight);
	g_scene.fog = new THREE.FogExp2(0xffffff, 0.00005);

	// Support window resize
	var resizeCallback = function() {
		g_panelWidthWebGL = window.innerWidth - BORDER_RIGHT;
		g_panelHeightWebGL = window.innerHeight - BORDER_BOTTOM;
		var devicePixelRatio = window.devicePixelRatio || 1;
		g_renderer.setSize(g_panelWidthWebGL * devicePixelRatio, g_panelHeightWebGL * devicePixelRatio);
		g_renderer.domElement.style.width = g_panelWidthWebGL + 'px';
		g_renderer.domElement.style.height = g_panelHeightWebGL + 'px';
		resetCamera();
		g_camera.updateProjectionMatrix();

		g_gui.domElement.style.position = 'absolute';
		g_gui.domElement.style.left = '' + (g_panelWidthWebGL + 20) + 'px';
		g_gui.domElement.style.top = '' + (document.getElementById('statusLine2').offsetTop + 50) + 'px';
	};
	window.addEventListener('resize', resizeCallback, false);
	resizeCallback();

	g_stats = new Stats();
	g_stats.domElement.style.position = 'absolute';
	g_stats.domElement.style.left = '11px';
	g_stats.domElement.style.top = '11px';
	g_container.appendChild(g_stats.domElement);

	g_control = new THREE.TrackballControls(g_camera, g_renderer.domElement);
	g_control.target.set(0, 0, 0);
	g_control.rotateSpeed = 1.0;
	g_control.zoomSpeed = 1.2;
	g_control.panSpeed = 0.8;
	g_control.noZoom = false;
	g_control.noPan = false;
	g_control.staticMoving = false;
	g_control.dynamicDampingFactor = 0.15;
	g_control.keys = [ 65, 83, 68 ];
	g_control.addEventListener('change', renderer);

	// Start animation
	animate();
}

/**
 * Render WebGL with about 60 fames per second if possible
 */
function animate() {
	"use strict";
	requestAnimationFrame(animate);
	g_control.update();
	updateLightPosition();
	g_stats.update();
}

function renderer() {
	"use strict";
	g_renderer.render(g_scene, g_camera);
}

/**
 * Creates and/or updates THRRE.js elements of the webGL graphic. Because the
 * creation of THREE.js elements is relative slow, so most of the elements are
 * moved and rotated as needed. Invisible elements will just be hidden and not
 * deleted.
 */
function updateWebGL(nodes, links) {
	"use strict";

	for (var i = 0; i < nodes.length; i++) {
		renderNodeSphere(nodes[i]);
		renderNodeLabel(nodes[i]);
	}
	for (i = 0; i < links.length; i++) {
		renderArrowElementForLink(links[i]);
	}
}

/**
 * Renders sphere for the node
 */
function renderNodeSphere(node) {
	"use strict";
	if (node.sphereCreated) {

		// Update position
		node.sphere.position.x = node.x;
		node.sphere.position.z = node.z;
		node.sphere.position.y = node.y;
		node.sphere.material.color.setHex('0x' + getColorHex(node.clusterId));
		node.sphere.visible = node.isVisible();
	} else {

		// Create sphere
		var material = new THREE.MeshLambertMaterial({
			reflectivity : 0.9,
			ambient : 0x3A3A3A,
			depthTest : true,
			transparent : false,
			opacity : node.isClusterNode ? 0.2 : 1.0
		});
		node.sphere = new THREE.Mesh(new THREE.SphereGeometry(node.getRadius()), material);
		node.sphere.position.x = node.x;
		node.sphere.position.z = node.z;
		node.sphere.position.y = node.y;
		node.sphere.visible = false;
		g_scene.add(node.sphere);
		node.sphereCreated = true;
	}
}

/**
 * Renders the alias of the node
 */
function renderNodeLabel(node) {
	"use strict";
	if (node.textCreated) {

		// Update orientation
		node.text.visible = YACA_SimulationOptions.DISPLAY_NAMES && node.isVisible();
		if (node.text.visible) {
			node.text.position.x = node.x + YACA_SimulationOptions.SPHERE_RADIUS_MINIMUM * 1.5;
			node.text.position.y = node.y + YACA_SimulationOptions.SPHERE_RADIUS_MINIMUM * 1.5;
			node.text.position.z = node.z + YACA_SimulationOptions.SPHERE_RADIUS_MINIMUM * 1.5;
			node.text.rotation.x = g_camera.rotation._x;
			node.text.rotation.y = g_camera.rotation._y;
			node.text.rotation.z = g_camera.rotation._z;

		}
	} else {

		// create canvas for text rendering
		var canvas = document.createElement('canvas');
		canvas.width = TEXT_HEIGHT * node.alias.length;
		canvas.height = TEXT_HEIGHT;

		// get text width
		var context = canvas.getContext('2d');
		context.font = '22px "Lucida Grande", sans-serif';
		context.textAlign = 'left';
		var metrics = context.measureText(node.alias);
		canvas.width = metrics.width;

		// render text
		context = canvas.getContext('2d');
		context.fillStyle = ('#' + getColorHex(node.clusterId));
		context.font = '22px "Lucida Grande", sans-serif';
		context.textAlign = 'left';
		context.fillText(node.alias, 0, TEXT_HEIGHT * 0.8);

		// create 3d element
		var tex = new THREE.Texture(canvas);
		var mat = new THREE.MeshBasicMaterial({
			map : tex,
			depthTest : true,
			transparent : true,
			overdraw : true
		});
		mat.map.needsUpdate = true;
		node.text = new THREE.Mesh(new THREE.PlaneGeometry(canvas.width, canvas.height), mat);
		node.text.dynamic = true;
		node.text.doubleSided = true;
		node.text.visible = false;
		node.textCreated = true;
		g_scene.add(node.text);
	}
}

/**
 * Renders a link - optional with arrow head
 */
function renderArrowElementForLink(link) {
	"use strict";

	// Center position of the nodes
	var source_position = new THREE.Vector3(link.source.x, link.source.y, link.source.z);
	var target_position = new THREE.Vector3(link.target.x, link.target.y, link.target.z);

	// Recursive link
	if (link.source.id == link.target.id) {
		source_position.y += link.source.getRadius() * 8;
	}

	// Truncate the line
	var isArrowVisible = link.isVisible() && YACA_SimulationOptions.DISPLAY_DIRECTIONS && !link.isClusterLink;
	var delta_target = (isArrowVisible) ? link.target.getRadius() * 2 : link.target.getRadius();
	moveEndPoints(source_position, link.source.getRadius(), target_position, delta_target);
	if (link.linkWebGLCreated) {

		// Move existing line
		link.threeElement.geometry.vertices[0] = source_position;
		link.threeElement.geometry.vertices[1] = target_position;
		link.threeElement.geometry.verticesNeedUpdate = true;
		link.threeElement.visible = link.isVisible();

		// Render arrow head
		if (link.arrowCreated) {

			// Move arrow head
			link.arrow.visible = isArrowVisible;
			var direction = new THREE.Vector3().subVectors(source_position, target_position);
			var arrow = new THREE.ArrowHelper(direction.normalize(), target_position);
			link.arrow.rotation.x = arrow.rotation.x;
			link.arrow.rotation.y = arrow.rotation.y;
			link.arrow.rotation.z = arrow.rotation.z;
			link.arrow.position = new THREE.Vector3().addVectors(target_position, direction.multiplyScalar(0.5));
		} else if (isArrowVisible) {

			// Create arrow head
			var material = new THREE.MeshLambertMaterial({
				reflectivity : 0.9,
				ambient : 0x3A3A3A,
				depthTest : true
			});
			var arrowHeadLength = link.target.getRadius() * 2;
			var cylinder = new THREE.CylinderGeometry(arrowHeadLength / 6, 0, arrowHeadLength, 10, 4, false);
			material.color.setHex('0x' + getColorHex(link.source.clusterId));
			link.arrow = new THREE.Mesh(cylinder, material);
			link.arrowCreated = true;
			g_scene.add(link.arrow);
		}
	} else {

		// Create line
		var line_geometry = new THREE.Geometry();
		line_geometry.vertices.push(source_position);
		line_geometry.vertices.push(target_position);
		var line_material = new THREE.LineBasicMaterial({
			depthTest : true
		});
		line_material.color.setHex('0x' + getColorHex(link.source.clusterId));
		line_material.transparent = true;
		var line = new THREE.Line(line_geometry, line_material);
		line.visible = false;
		link.threeElement = line;
		link.linkWebGLCreated = true;
		g_scene.add(line);
	}
}

/**
 * Truncate the end points of a line
 */
function moveEndPoints(source_position, source_delta, target_position, target_delta) {
	"use strict";
	var deltaX = (source_position.x - target_position.x);
	var deltaY = (source_position.y - target_position.y);
	var deltaZ = (source_position.z - target_position.z);
	var radius = Math.sqrt(deltaX * deltaX + deltaY * deltaY + deltaZ * deltaZ);
	source_position.x -= (deltaX) / radius * (source_delta);
	source_position.y -= (deltaY) / radius * (source_delta);
	source_position.z -= (deltaZ) / radius * (source_delta);
	target_position.x += (deltaX) / radius * (target_delta);
	target_position.y += (deltaY) / radius * (target_delta);
	target_position.z += (deltaZ) / radius * (target_delta);
}

/**
 * Move light dependent on the position of the camera. The rotation of the
 * graphic is done by movement of the camera and not the rotation of the scene
 */
function updateLightPosition() {
	"use strict";
	g_light.position.x = g_control.object.position.x - 500;
	g_light.position.y = g_control.object.position.y - 500;
	g_light.position.z = g_control.object.position.z;
	g_light.target.position.set(0, 0, 0);
}

function resetCamera() {
	"use strict";
	g_camera.position.x = 0.0;
	g_camera.position.y = 0.0;
	g_camera.position.z = YACA_SimulationOptions.SPHERE_RADIUS * 2.5;
	g_camera.lookAt(new THREE.Vector3(0, 0, 0));
}

/**
 * Converts a integer to a two char hex code without 0x
 */
function integerToHex(n) {
	"use strict";
	n = Math.max(0, Math.min(parseInt(n, 10), 255));
	var charFirst = "0123456789ABCDEF".charAt((n - n % 16) / 16);
	var charSecond = "0123456789ABCDEF".charAt(n % 16);
	return charFirst + charSecond;
}

/**
 * Helper for coloring the nodes and links
 */
function getColorHex(value) {
	"use strict";
	var key = 'c' + value;
	if (null == g_colorHash.get(key)) {
		var frequency = value * 0.5;
		var red = Math.sin(frequency) * 127 + 127;
		var green = Math.sin(2 + frequency) * 127 + 127;
		var blue = Math.sin(4 + frequency) * 127 + 127;
		g_colorHash.put(key, '' + integerToHex(red) + integerToHex(green) + integerToHex(blue));
	}
	return g_colorHash.get(key);
}

/**
 * Connection with local Agent via JSONP. "It provides a method to request data
 * from a server in a different domain, something prohibited by typical web
 * browsers because of the same origin policy."
 * 
 * [see http://en.wikipedia.org/wiki/JSONP]
 */
function importAgentData(url) {
	"use strict";
	var script = document.createElement("script");
	script.setAttribute("type", "text/javascript");
	script.id = 'JSONP';
	url += (url.indexOf("?") == -1 ? "?" : "&time=") + (new Date().getTime());
	script.setAttribute("src", url);
	document.body.appendChild(script);
	setTimeout(function() {
		var script;
		while (script = document.getElementById('JSONP')) {
			script.parentNode.removeChild(script);
			for ( var prop in script) {
				delete script[prop];
			}
		}
	}, 10000);
}

function arraysIdentical(a, b) {
	var i = a.length;
	if (i != b.length)
		return false;
	while (i--) {
		if (a[i] !== b[i])
			return false;
	}
	return true;
};

/**
 * Callback function for connection with agent. It takes the new model and
 * updates the model.
 */
function yaca_agent_callback(input_model) {
	"use strict";
	YACA_NBodySimulator.updateModel(input_model);
	if (typeof (input_model.process_id_active) !== "undefined") {
		// 
		if (!arraysIdentical(g_old_pids, input_model.process_id_available)) {
			g_gui_folder1.remove(g_gui_pid);
			g_gui_pid = g_gui_folder1.add(YACA_SimulationOptions, 'ACTIVE_PID', input_model.process_id_available).listen().name(
					'Active PID').onChange(function(value) {
			
				yaca_agent_callback({
					"nodes" : [],
					"links" : []
				});
				YACA_NBodySimulator.node_list = [];
				YACA_NBodySimulator.link_list = [];
				
			});

			g_old_pids = input_model.process_id_available;
		}
	}
}

/**
 * Update import and/or simulation
 */
function updateTimerImport() {
	"use strict";
	if (YACA_SimulationOptions.RUN_IMPORT) {
		importAgentData(YACA_SimulationOptions.URL + "\\processes\\" + YACA_SimulationOptions.ACTIVE_PID);
	}
}

/**
 * Optimization of the node position
 */
function runSimulation(max_count) {
	"use strict";
	if (YACA_SimulationOptions.RUN_SIMULATION) {
		for (var count = 0; count < max_count; count++) {
			YACA_NBodySimulator.simulateAllForces();
		}
	}
}

/**
 * Output of status line
 */
function updateStatusLine() {
	"use strict";
	var stausLine1 = document.getElementById('statusLine1');
	stausLine1.innerHTML = ('Active Nodes ' + YACA_NBodySimulator.node_list_visible.length + ' ('
			+ YACA_NBodySimulator.node_list.length + ') and Links ' + YACA_NBodySimulator.link_list_visible.length
			+ ' (' + YACA_NBodySimulator.link_list.length + ')');

	var stausLine2 = document.getElementById('statusLine2');
	var modelType = (g_imported_data) ? 'Data imported from YacaAgent (dynamic)' : 'Default model (static)';
	stausLine2.innerHTML = (g_Message + modelType);
}

/**
 * User interface to change parameters
 */
function initDatGui(container) {
	g_gui = new dat.GUI({
		autoPlace : false
	});

	g_gui_folder1 = g_gui.addFolder('General Options');
	g_gui_folder1.add(YACA_SimulationOptions, 'RUN_IMPORT').name('Run Import').onChange(function(value) {
		if (value) {
			yaca_agent_callback({
				"nodes" : [],
				"links" : []
			});
			YACA_NBodySimulator.node_list = [];
			YACA_NBodySimulator.link_list = [];
			g_imported_data = true;
		}
	//	importAgentData(YACA_SimulationOptions.URL + "\\processes\\" + YACA_SimulationOptions.ACTIVE_PID);
	});
	g_gui_folder1.add(YACA_SimulationOptions, 'DISPLAY_NAMES').name('Show Names');
	g_gui_folder1.add(YACA_SimulationOptions, 'URL').name('Import URL');
	g_gui_pid = g_gui_folder1.add(YACA_SimulationOptions, 'ACTIVE_PID').name('Active PID');
	g_gui_folder1.open();

	f2 = g_gui.addFolder('Filter Nodes by ... ');
	f2.add(YACA_SimulationOptions, 'RENDER_THRESHOLD', 1.0, 100.0).step(1.0).name('Activity Index');
	f2.add(YACA_SimulationOptions, 'RUN_IMPORT_FILTER').name('Name').listen().onChange(function(value) {
		YACA_NodeRegexFilter = new RegExp(value);
		if (!YACA_SimulationOptions.RUN_IMPORT) {
			loadDefultModel();
		}
	});
	f2.open();

	f3 = g_gui.addFolder('N-Body Simulation');
	f3.add(YACA_SimulationOptions, 'RUN_SIMULATION').name('Run');
	f3.add(YACA_SimulationOptions, 'DISTANCE', YACA_SimulationOptions.SPHERE_RADIUS_MINIMUM * 2,
			YACA_SimulationOptions.SPHERE_RADIUS_MINIMUM * 10).step(10.0).name('Link Distance');
	f3.add(YACA_SimulationOptions, 'SPRING', 0.0, 40).step(5.0).name('Spring Link');
	f3.add(YACA_SimulationOptions, 'SPHERE_RADIUS', 400, 2000).step(100.0).name('Sphere Radius').onChange(
			function(value) {
				YACA_NBodySimulator.node_list.forEach(function(node) {
					YACA_NBodySimulator.scaleToBeInSphere(node);
				});
			});
	f3.add(YACA_SimulationOptions, 'CHARGE', 0, 3000).step(100.0).name('Charge');
	f3.open();

	container.appendChild(g_gui.domElement);
}

/**
 * Call initialization
 */
initApplication();