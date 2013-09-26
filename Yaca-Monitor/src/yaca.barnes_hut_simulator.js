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
 */
BarnesHutAlgorithm = {};

BarnesHutAlgorithm.initRandomPosition = function(node) {
    var gamma = 2 * Math.PI * Math.random();
    var delta = Math.PI * Math.random();
    var radius = g_options.SPHERE_RADIUS * 0.95;
    node.x = radius * Math.sin(delta) * Math.cos(gamma);
    node.y = radius * Math.sin(delta) * Math.sin(gamma);
    node.z = radius * Math.cos(delta);
};

BarnesHutAlgorithm.OctTree = function(options) {
    /**
     * Barnes-Hut algorithm for a three-dimensional simulation of charge and
     * gravity
     */

    // Parameter needed for the simulation
    if (typeof (options) != "undefined") {
	if (typeof (options.SPHERE_RADIUS) != "undefined") {
	    g_options.SPHERE_RADIUS = options.SPHERE_RADIUS;
	}
	if (typeof (options.SPHERE_RADIUS_MINIMUM) != "undefined") {
	    g_options.SPHERE_RADIUS_MINIMUM = options.SPHERE_RADIUS_MINIMUM;
	}
	if (typeof (options.CHARGE) != "undefined") {
	    g_options.CHARGE = options.CHARGE;
	}
	if (typeof (options.THETA) != "undefined") {
	    g_options.THETA = options.THETA;
	}
	if (typeof (options.GRAVITY) != "undefined") {
	    g_options.GRAVITY = options.GRAVITY;
	}
    }

    BarnesHutAlgorithm.OctTree.prototype.calcGravityForce = function(node) {
	var deltaX = node.x;
	var deltaY = node.y;
	var deltaZ = node.z;
	var radius = Math.sqrt(deltaX * deltaX + deltaY * deltaY + deltaZ * deltaZ);
	if (radius > g_options.SPHERE_RADIUS_MINIMUM*10) {
	    node.force_x -= (deltaX) / radius / radius * g_options.GRAVITY;
	    node.force_y -= (deltaY) / radius / radius * g_options.GRAVITY;
	    node.force_z -= (deltaZ) / radius / radius * g_options.GRAVITY;
	}
    };

    BarnesHutAlgorithm.OctTree.prototype.run = function(nodes) {
	var size = g_options.SPHERE_RADIUS;
	BarnesHutAlgorithm.root = new BarnesHutAlgorithm.OctNode(-size, size, -size, size, -size, size);

	if (nodes.length > 1) {
	    for ( var i = nodes.length - 1; i != 0; i--) {
		var node = nodes[i];
		BarnesHutAlgorithm.root.addNode(node);
	    }
	    BarnesHutAlgorithm.root.calculateAveragesAndSumOfMass();
	    for ( var i = nodes.length - 1; i != 0; i--) {
		var node = nodes[i];
		BarnesHutAlgorithm.root.calculateForces(node);
		this.calcGravityForce(node);
	    }
	}
    };
};

/**
 * Every node has the same mass
 */
BarnesHutAlgorithm.OctNode = function(xMin, xMax, yMin, yMax, zMin, zMax) {
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

BarnesHutAlgorithm.OctNode.prototype.isFilled = function() {
    return (this.node != null);
};

BarnesHutAlgorithm.OctNode.prototype.isParent = function() {
    return (this.children != null);
};

BarnesHutAlgorithm.OctNode.prototype.isFitting = function(node) {
    return ((node.x >= this.xMin) && (node.x <= this.xMax) && (node.y >= this.yMin) && (node.y <= this.yMax)
	    && (node.z >= this.zMin) && (node.z <= this.zMax));
};

BarnesHutAlgorithm.OctNode.prototype.addNode = function(new_node) {

    if (this.isFilled() || this.isParent()) {
	if (g_options.SPHERE_RADIUS_MINIMUM > this.diameter) {
	    radius = Math.sqrt(new_node.x * new_node.x + new_node.y * new_node.y + new_node.z * new_node.z);
	    factor = (radius - g_options.SPHERE_RADIUS_MINIMUM) / radius;
	    new_node.x *= factor;
	    new_node.y *= factor;
	    new_node.z *= factor;
	    relocated_node = this.node;
	    this.node = null;
	    this.sum_mass = 0;
	    this.sum_x = 0;
	    this.sum_y = 0;
	    this.sum_z = 0;
	    BarnesHutAlgorithm.root.addNode(relocated_node);
	    return;
	}

	if (!this.isParent()) {
	    xMiddle = (this.xMin + this.xMax) / 2;
	    yMiddle = (this.yMin + this.yMax) / 2;
	    zMiddle = (this.zMin + this.zMax) / 2;

	    // create children
	    this.children = [];
	    this.children.push(new BarnesHutAlgorithm.OctNode(xMiddle, this.xMax, yMiddle, this.yMax, zMiddle,
		    this.zMax));
	    this.children.push(new BarnesHutAlgorithm.OctNode(this.xMin, xMiddle, yMiddle, this.yMax, zMiddle,
		    this.zMax));
	    this.children.push(new BarnesHutAlgorithm.OctNode(this.xMin, xMiddle, this.yMin, yMiddle, zMiddle,
		    this.zMax));
	    this.children.push(new BarnesHutAlgorithm.OctNode(xMiddle, this.xMax, this.yMin, yMiddle, zMiddle,
		    this.zMax));
	    this.children.push(new BarnesHutAlgorithm.OctNode(xMiddle, this.xMax, yMiddle, this.yMax, this.zMin,
		    zMiddle));
	    this.children.push(new BarnesHutAlgorithm.OctNode(this.xMin, xMiddle, yMiddle, this.yMax, this.zMin,
		    zMiddle));
	    this.children.push(new BarnesHutAlgorithm.OctNode(this.xMin, xMiddle, this.yMin, yMiddle, this.zMin,
		    zMiddle));
	    this.children.push(new BarnesHutAlgorithm.OctNode(xMiddle, this.xMax, this.yMin, yMiddle, this.zMin,
		    zMiddle));

	    // re-locate old node (add into children)
	    relocated_node = this.node;
	    this.node = null;
	    this.sum_mass = 0;
	    this.sum_x = 0;
	    this.sum_y = 0;
	    this.sum_z = 0;

	    cellId = this.addChildNode(relocated_node);
	}

	// now add new node into children
	if (this.isParent()) {
	    cellId = this.addChildNode(new_node);
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

BarnesHutAlgorithm.OctNode.prototype.addChildNode = function(node) {
    if (this.isParent()) {
	for ( var index = 0; index < 8; index++) {
	    child = this.children[index];
	    if (child.isFitting(node)) {
		child.addNode(node);
		return;
	    }
	}
    }
    // Unable to add node -> has to be relocated
    BarnesHutAlgorithm.initRandomPosition(node);
    BarnesHutAlgorithm.root.addNode(node);
};

BarnesHutAlgorithm.OctNode.prototype.calculateForces = function(new_node) {

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
	var treatInternalNodeAsSingleBody = this.diameter / radius < g_options.THETA;
	if (this.isFilled() || treatInternalNodeAsSingleBody) {
	    new_node.force_x -= (deltaX * g_options.CHARGE) / radius_squared;
	    new_node.force_y -= (deltaY * g_options.CHARGE) / radius_squared;
	    new_node.force_z -= (deltaZ * g_options.CHARGE) / radius_squared;
	} else if (this.isParent()) {
	    for ( var index = 0; index < 8; index++) {
		child = this.children[index];
		if (child.isFilled() || this.isParent()) {
		    child.calculateForces(new_node);
		}
	    }
	}
    }
};

BarnesHutAlgorithm.OctNode.prototype.calculateAveragesAndSumOfMass = function() {
    if (this.isParent()) {
	for ( var index = 0; index < 8; index++) {
	    child = this.children[index];
	    child.calculateAveragesAndSumOfMass();
	}

	this.sum_mass = 0;
	this.sum_x = 0;
	this.sum_y = 0;
	this.sum_z = 0;
	for ( var index = 0; index < 8; index++) {
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