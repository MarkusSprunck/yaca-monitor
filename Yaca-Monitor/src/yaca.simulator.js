/**
 * Copyright (C) 2013, Markus Sprunck
 *
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met: -
 * Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer. - Redistributions in binary
 * form must reproduce the above copyright notice, this list of conditions and
 * the following disclaimer in the documentation and/or other materials provided
 * with the distribution. - The name of its contributor may be used to endorse
 * or promote products derived from this software without specific prior written
 * permission.
 *
 * this SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF this SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */

/* jshint undef: true, unused: true, strict: true */

/**
 * Options for n-body simulation and rendering
 */
function SimulationOptions() {
    "use strict";
    return {

	SPHERE_RADIUS : 900,
	SPHERE_RADIUS_MINIMUM : 20,
	CHARGE : 300,
	GRAVITY : 1000,
	THETA : 0.8,
	DISTANCE : 160,
	SPRING : 20,
	RUN_IMPORT_FILTER : "^((?!(Thread.run)).)*$",

	// Parameters for rendering
	RUN_IMPORT : true,
	RUN_IMPORT_INTERVAL : 2000,
	FILTER_CLUSTER : true,
	RUN_SIMULATION : true,
	DISPLAY_DIRECTIONS : true,
	DISPLAY_CUBE : true,
	DISPLAY_NAMES : true,
	RENDER_THRESHOLD : 15.0
    };
}

/**
 * Node element with information for simulation and rendering
 */
function NNode(node) {
    "use strict";
    this.id = node.id;
    this.name = node.name;
    this.alias = node.alias;
    this.isClusterNode = node.isClusterNode;
    this.calls = node.calls;
    this.clusterId = node.clusterId;

    this.clusterIsVisible = false;
    this.isFiltered = YACA_NodeRegexFilter.test(node.name);

    this.x = node.x;
    this.y = node.y;
    this.z = node.z;

    this.force_x = 0.0;
    this.force_y = 0.0;
    this.force_z = 0.0;

    // Rendering elements
    this.sphere = {};
    this.sphereCreated = false;
    this.text = {};
    this.textCreated = false;
}

NNode.prototype.update = function(node) {
    "use strict";
    this.callFilter();
    this.calls = this.isFiltered ? node.calls : 0;
};

NNode.prototype.callFilter = function() {
    "use strict";
    this.clusterIsVisible = false;
    this.isFiltered = YACA_NodeRegexFilter.test(this.name);
};

NNode.prototype.getCalls = function() {
    "use strict";
    return this.calls;
};

NNode.prototype.isVisible = function() {
    "use strict";
    return (this.getActivity() > YACA_SimulationOptions.RENDER_THRESHOLD) && this.isFiltered && !this.isClusterNode
	    || this.clusterIsVisible;
};

NNode.prototype.getRadius = function() {
    "use strict";
    return this.isClusterNode ? 0.3 * YACA_SimulationOptions.SPHERE_RADIUS_MINIMUM
	    : YACA_SimulationOptions.SPHERE_RADIUS_MINIMUM;
};

NNode.prototype.getActivity = function() {
    "use strict";
    return this.isClusterNode ? 0 : 101 * (Math.log(this.getCalls()) / Math.log(YACA_NBodySimulator.maxNodeCalls));
};

/**
 * Link element with information for simulation and rendering
 */
function NLink(link, me) {
    "use strict";
    // Simulation of spring forces between nodes
    this.id = link.id;
    this.isClusterLink = link.isClusterLink;
    this.source = me.node_list[link.sourceId];
    this.target = me.node_list[link.targetId];

    // Rendering elements
    this.threeElement = {};
    this.linkWebGLCreated = false;
    this.arrow = {};
    this.arrowCreated = false;
}

NLink.prototype.isVisible = function() {
    "use strict";
    return this.source.isVisible() && this.target.isVisible() || this.target.clusterIsVisible
	    && this.source.isVisible();
};

/**
 * Implementation of Barnes-Hut algorithm for a three-dimensional simulation of
 * charge and gravity
 */
BarnesHutAlgorithmOctTree = function(options) {
    "use strict";
    // Parameter needed for the simulation
    if (typeof (options) !== "undefined") {
	if (typeof (options.SPHERE_RADIUS) !== "undefined") {
	    YACA_SimulationOptions.SPHERE_RADIUS = options.SPHERE_RADIUS;
	}
	if (typeof (options.SPHERE_RADIUS_MINIMUM) !== "undefined") {
	    YACA_SimulationOptions.SPHERE_RADIUS_MINIMUM = options.SPHERE_RADIUS_MINIMUM;
	}
	if (typeof (options.CHARGE) !== "undefined") {
	    YACA_SimulationOptions.CHARGE = options.CHARGE;
	}
	if (typeof (options.THETA) !== "undefined") {
	    YACA_SimulationOptions.THETA = options.THETA;
	}
	if (typeof (options.GRAVITY) !== "undefined") {
	    YACA_SimulationOptions.GRAVITY = options.GRAVITY;
	}
    }

    BarnesHutAlgorithmOctTree.prototype.initRandomPosition = function(node) {
	var gamma = 2 * Math.PI * Math.random();
	var delta = Math.PI * Math.random();
	var radius = YACA_SimulationOptions.SPHERE_RADIUS * 0.95;
	node.x = radius * Math.sin(delta) * Math.cos(gamma);
	node.y = radius * Math.sin(delta) * Math.sin(gamma);
	node.z = radius * Math.cos(delta);
    };

    BarnesHutAlgorithmOctTree.prototype.calcGravityForce = function(node) {
	var deltaX = node.x;
	var deltaY = node.y;
	var deltaZ = node.z;
	var radius = Math.sqrt(deltaX * deltaX + deltaY * deltaY + deltaZ * deltaZ);
	if (radius > YACA_SimulationOptions.SPHERE_RADIUS_MINIMUM * 10) {
	    node.force_x -= (deltaX) / radius / radius * YACA_SimulationOptions.GRAVITY;
	    node.force_y -= (deltaY) / radius / radius * YACA_SimulationOptions.GRAVITY;
	    node.force_z -= (deltaZ) / radius / radius * YACA_SimulationOptions.GRAVITY;
	}
    };

    BarnesHutAlgorithmOctTree.prototype.run = function(nodes) {
	var size = YACA_SimulationOptions.SPHERE_RADIUS;
	YACA_OctTreeRoot = new BarnesHutAlgorithmOctNode(-size, size, -size, size, -size, size);
	var node;
	if (nodes.length > 1) {
	    for ( var i = nodes.length - 1; i !== 0; i--) {
		node = nodes[i];
		YACA_OctTreeRoot.addNode(node);
	    }
	    YACA_OctTreeRoot.calculateAveragesAndSumOfMass();
	    for (i = nodes.length - 1; i !== 0; i--) {
		node = nodes[i];
		YACA_OctTreeRoot.calculateForces(node);
		this.calcGravityForce(node);
	    }
	}
    };
};

BarnesHutAlgorithmOctNode = function(xMin, xMax, yMin, yMax, zMin, zMax) {
    "use strict";
    this.xMin = xMin;
    this.xMax = xMax;
    this.yMin = yMin;
    this.yMax = yMax;
    this.zMin = zMin;
    this.zMax = zMax;
    this.sum_mass = 0;
    this.sum_x = 0;
    this.sum_y = 0;
    this.sum_z = 0;
    this.node = null;
    this.children = null;
    this.diameter = (((xMax - xMin) + (yMax - yMin) + (zMax - zMin)) / 3);
};

BarnesHutAlgorithmOctNode.prototype.isFilled = function() {
    "use strict";
    return (this.node !== null);
};

BarnesHutAlgorithmOctNode.prototype.isParent = function() {
    "use strict";
    return (this.children !== null);
};

BarnesHutAlgorithmOctNode.prototype.isFitting = function(node) {
    "use strict";
    return ((node.x >= this.xMin) && (node.x <= this.xMax) && (node.y >= this.yMin) && (node.y <= this.yMax)
	    && (node.z >= this.zMin) && (node.z <= this.zMax));
};

BarnesHutAlgorithmOctNode.prototype.addNode = function(new_node) {
    "use strict";
    if (this.isFilled() || this.isParent()) {
	var relocated_node;
	if (YACA_SimulationOptions.SPHERE_RADIUS_MINIMUM > this.diameter) {
	    var radius = Math.sqrt(new_node.x * new_node.x + new_node.y * new_node.y + new_node.z * new_node.z);
	    var factor = (radius - YACA_SimulationOptions.SPHERE_RADIUS_MINIMUM) / radius;
	    new_node.x *= factor;
	    new_node.y *= factor;
	    new_node.z *= factor;
	    relocated_node = this.node;
	    this.node = null;
	    this.sum_mass = 0;
	    this.sum_x = 0;
	    this.sum_y = 0;
	    this.sum_z = 0;
	    YACA_OctTreeRoot.addNode(relocated_node);
	    return;
	}

	if (!this.isParent()) {
	    var xMiddle = (this.xMin + this.xMax) / 2;
	    var yMiddle = (this.yMin + this.yMax) / 2;
	    var zMiddle = (this.zMin + this.zMax) / 2;

	    // create children
	    this.children = [];
	    this.children
		    .push(new BarnesHutAlgorithmOctNode(xMiddle, this.xMax, yMiddle, this.yMax, zMiddle, this.zMax));
	    this.children
		    .push(new BarnesHutAlgorithmOctNode(this.xMin, xMiddle, yMiddle, this.yMax, zMiddle, this.zMax));
	    this.children
		    .push(new BarnesHutAlgorithmOctNode(this.xMin, xMiddle, this.yMin, yMiddle, zMiddle, this.zMax));
	    this.children
		    .push(new BarnesHutAlgorithmOctNode(xMiddle, this.xMax, this.yMin, yMiddle, zMiddle, this.zMax));
	    this.children
		    .push(new BarnesHutAlgorithmOctNode(xMiddle, this.xMax, yMiddle, this.yMax, this.zMin, zMiddle));
	    this.children
		    .push(new BarnesHutAlgorithmOctNode(this.xMin, xMiddle, yMiddle, this.yMax, this.zMin, zMiddle));
	    this.children
		    .push(new BarnesHutAlgorithmOctNode(this.xMin, xMiddle, this.yMin, yMiddle, this.zMin, zMiddle));
	    this.children
		    .push(new BarnesHutAlgorithmOctNode(xMiddle, this.xMax, this.yMin, yMiddle, this.zMin, zMiddle));

	    // re-locate old node (add into children)
	    relocated_node = this.node;
	    this.node = null;
	    this.sum_mass = 0;
	    this.sum_x = 0;
	    this.sum_y = 0;
	    this.sum_z = 0;

	    this.addChildNode(relocated_node);
	}

	// now add new node into children
	if (this.isParent()) {
	    this.addChildNode(new_node);
	}

    } else {
	this.node = new_node;
	this.sum_mass = 1;
	this.sum_x = this.node.x;
	this.sum_y = this.node.y;
	this.sum_z = this.node.z;
	this.node.force_x = 0.0;
	this.node.force_y = 0.0;
	this.node.force_z = 0.0;
    }
};

BarnesHutAlgorithmOctNode.prototype.addChildNode = function(node) {
    "use strict";
    if (this.isParent()) {
	for ( var index = 0; index < 8; index++) {
	    var child = this.children[index];
	    if (child.isFitting(node)) {
		child.addNode(node);
		return;
	    }
	}
    }
    // Unable to add node -> has to be relocated
    BarnesHutAlgorithm.initRandomPosition(node);
    YACA_OctTreeRoot.addNode(node);
};

BarnesHutAlgorithmOctNode.prototype.calculateForces = function(new_node) {
    "use strict";
    if (this.sum_mass > 0.1 || this.isFilled()) {
	var deltaX, deltaY, deltaZ;
	if (this.isFilled()) {
	    deltaX = (this.node.x - new_node.x);
	    deltaY = (this.node.y - new_node.y);
	    deltaZ = (this.node.z - new_node.z);
	} else {
	    deltaX = (this.sum_x / this.sum_mass - new_node.x);
	    deltaY = (this.sum_y / this.sum_mass - new_node.y);
	    deltaZ = (this.sum_z / this.sum_mass - new_node.z);
	}

	var radius = Math.sqrt(deltaX * deltaX + deltaY * deltaY + deltaZ * deltaZ);
	var radius_squared = Math.pow((radius > 1e-6) ? radius : 1e-6, 2);
	var treatInternalNodeAsSingleBody = this.diameter / radius < YACA_SimulationOptions.THETA;
	if (this.isFilled() || treatInternalNodeAsSingleBody) {
	    new_node.force_x -= (deltaX * YACA_SimulationOptions.CHARGE) / radius_squared;
	    new_node.force_y -= (deltaY * YACA_SimulationOptions.CHARGE) / radius_squared;
	    new_node.force_z -= (deltaZ * YACA_SimulationOptions.CHARGE) / radius_squared;
	} else if (this.isParent()) {
	    for ( var index = 0; index < 8; index++) {
		var child = this.children[index];
		if (child.isFilled() || this.isParent()) {
		    child.calculateForces(new_node);
		}
	    }
	}
    }
};

BarnesHutAlgorithmOctNode.prototype.calculateAveragesAndSumOfMass = function() {
    "use strict";
    if (this.isParent()) {
	var child;
	for ( var index = 0; index < 8; index++) {
	    child = this.children[index];
	    child.calculateAveragesAndSumOfMass();
	}

	this.sum_mass = 0;
	this.sum_x = 0;
	this.sum_y = 0;
	this.sum_z = 0;
	for (index = 0; index < 8; index++) {
	    child = this.children[index];
	    if (child.isFilled() || this.isParent()) {
		this.sum_mass += child.sum_mass;
		this.sum_x += child.sum_x;
		this.sum_y += child.sum_y;
		this.sum_z += child.sum_z;
	    }
	}
    }
};

/**
 * n-body simulator makes the Branes-Hut simulation and adds the link forces.
 */
var NBodySimulator = function() {
    "use strict";

    // all existing nodes and links
    this.node_list = [];
    this.link_list = [];

    this.octTree = new BarnesHutAlgorithmOctTree();

    // all nodes and links that are in the current filter
    this.node_list_visible = [];
    this.link_list_visible = [];

    this.node_list_visible_last = [];
    this.link_list_visible_last = [];

    this.maxNodeCalls = 0;

    NBodySimulator.prototype.updateModel = function(input_model) {

	// create or update nodes
	this.maxNodeCalls = 0;
	var nodes = input_model.nodes;
	for ( var i = 0; i < nodes.length; i++) {
	    var newNode = nodes[i];
	    var node;
	    if (this.node_list.length <= newNode.id) {
		this.octTree.initRandomPosition(newNode);
		node = new NNode(newNode);
		this.node_list.push(node);
	    } else {
		node = this.node_list[newNode.id];
		node.update(newNode);
	    }
	    this.maxNodeCalls = Math.max(this.maxNodeCalls, node.getCalls());
	}

	// create or update links and related nodes
	var links = input_model.links;
	for (i = 0; i < links.length; i++) {
	    var newLink = links[i];
	    var link;
	    if (this.link_list.length <= newLink.id) {
		link = new NLink(newLink, this);
		this.link_list.push(link);
	    } else {
		link = this.link_list[newLink.id];
	    }
	    if (link.isClusterLink) {
		link.target.clusterIsVisible = link.target.clusterIsVisible || link.source.isVisible()
			&& !YACA_SimulationOptions.FILTER_CLUSTER;
	    }
	}
    };

    NBodySimulator.prototype.applyFilter = function() {

	this.node_list_visible_last = this.node_list_visible;
	this.link_list_visible_last = this.link_list_visible;

	this.node_list_visible = [];
	var nodes = this.node_list;
	var link;
	var node;
	var i;
	var me = this;
	for (i = 0; i < nodes.length; i++) {
	    node = nodes[i];
	    node.callFilter();
	    if (node.isVisible()) {
		me.node_list_visible.push(node);
	    }
	}

	this.link_list_visible = [];
	var links = this.link_list;
	for (i = 0; i < links.length; i++) {
	    link = links[i];
	    if (link.isClusterLink) {
		link.target.clusterIsVisible = link.target.clusterIsVisible
			|| (link.source.isVisible() && !YACA_SimulationOptions.FILTER_CLUSTER);
	    }
	}

	for (i = 0; i < links.length; i++) {
	    link = links[i];
	    if (link.isVisible()) {
		me.link_list_visible.push(link);
	    }
	}

	for (i = 0; i < nodes.length; i++) {
	    node = nodes[i];
	    if (node.clusterIsVisible) {
		me.node_list_visible.push(node);
	    }
	}
    };

    NBodySimulator.prototype.simulateAllForces = function() {

	// Execute Barnes-Hut simulation
	this.octTree = new BarnesHutAlgorithmOctTree();
	this.octTree.run(this.node_list_visible);

	// Calculate link forces
	var me = this;
	this.link_list_visible.forEach(function(link) {
	    if (link.source.id !== link.target.id) {
		me.calcLinkForce(link);
	    }
	});

	// Scale and apply all forces
	this.node_list_visible.forEach(function(node) {
	    me.scaleForceToBeSmall(node);
	    me.applyForces(node);
	    me.scaleToBeInSphere(node);
	    me.resetForces(node);
	});

    };

    /**
     * Each link acts as simple spring. There are two types of nodes and links.
     */
    NBodySimulator.prototype.calcLinkForce = function(link) {
	var deltaX = (link.source.x - link.target.x);
	var deltaY = (link.source.y - link.target.y);
	var deltaZ = (link.source.z - link.target.z);
	var radius = Math.sqrt(deltaX * deltaX + deltaY * deltaY + deltaZ * deltaZ);
	if (radius > 1e-6) {
	    var factor = (radius - YACA_SimulationOptions.DISTANCE) / radius / radius * YACA_SimulationOptions.SPRING;
	    link.source.force_x -= (deltaX) * factor;
	    link.source.force_y -= (deltaY) * factor;
	    link.source.force_z -= (deltaZ) * factor;
	    link.target.force_x += (deltaX) * factor;
	    link.target.force_y += (deltaY) * factor;
	    link.target.force_z += (deltaZ) * factor;
	}
    };

    /**
     * Ensure that the new position is in the sphere. Nodes which leave the
     * sphere would be ignored by OctTree (Barnes-Hut-Algorithm).
     */
    NBodySimulator.prototype.scaleToBeInSphere = function(node) {
	node.x = Math.min(Math.max(1 - YACA_SimulationOptions.SPHERE_RADIUS, node.x),
		YACA_SimulationOptions.SPHERE_RADIUS - 1);
	node.y = Math.min(Math.max(1 - YACA_SimulationOptions.SPHERE_RADIUS, node.y),
		YACA_SimulationOptions.SPHERE_RADIUS - 1);
	node.z = Math.min(Math.max(1 - YACA_SimulationOptions.SPHERE_RADIUS, node.z),
		YACA_SimulationOptions.SPHERE_RADIUS - 1);
    };

    /**
     * Ensure that the new position is in the sphere
     */
    NBodySimulator.prototype.scaleForceToBeSmall = function(node) {
	var radius = Math.sqrt(node.force_x * node.force_x + node.force_y * node.force_y + node.force_z * node.force_z);
	if (radius > YACA_SimulationOptions.SPHERE_RADIUS_MINIMUM) {
	    node.force_x *= YACA_SimulationOptions.SPHERE_RADIUS_MINIMUM / radius;
	    node.force_y *= YACA_SimulationOptions.SPHERE_RADIUS_MINIMUM / radius;
	    node.force_z *= YACA_SimulationOptions.SPHERE_RADIUS_MINIMUM / radius;
	}
    };

    /**
     * Move the nodes depending of the forces
     */
    NBodySimulator.prototype.applyForces = function(node) {
	node.x += node.force_x;
	node.y += node.force_y;
	node.z += node.force_z;
    };

    /**
     * Reset all forces of the node to zero
     */
    NBodySimulator.prototype.resetForces = function(node) {
	node.force_x = 0;
	node.force_y = 0;
	node.force_z = 0;
    };
};

/**
 * global variables
 */
var YACA_SimulationOptions = new SimulationOptions();
var YACA_NodeRegexFilter = new RegExp(YACA_SimulationOptions.RUN_IMPORT_FILTER);
var YACA_OctTreeRoot = {};
var YACA_NBodySimulator = new NBodySimulator();
