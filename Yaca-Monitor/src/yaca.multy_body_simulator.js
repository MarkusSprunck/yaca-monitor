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

NBodySimulator = function() {

    // Used to find default position of a cluster node
    var nodesHash = new HashMap();

    // all existing nodes and links
    this.node_list = [];
    this.link_list = [];

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
		this.initPositionNode(newNode);
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
	me = this;
	for ( var i = 0; i < links.length; i++) {
	    var newLink = links[i];
	    var link;
	    if (this.link_list.length <= newLink.id) {
		link = new NLink(newLink);
		this.link_list.push(link);
	    } else {
		link = this.link_list[newLink.id];
	    }
	    if (link.isClusterLink) {
		link.target.clusterIsVisible = link.target.clusterIsVisible || link.source.isVisible()
			&& !g_options.FILTER_CLUSTER;
	    }
	}
    };

    NBodySimulator.prototype.applyFilter = function() {
	
	this.node_list_visible_last = this.node_list_visible;
	this.link_list_visible_last = this.link_list_visible;

	this.node_list_visible = [];
	var nodes = this.node_list;
	for ( var i = 0; i < nodes.length; i++) {
	    var node = nodes[i];
	    node.callFilter();
	    if (node.isVisible()) {
		me.node_list_visible.push(node);
	    }
	}

	this.link_list_visible = [];
	var links = this.link_list;
	for ( var i = 0; i < links.length; i++) {
	    var link = links[i];
	    if (link.isClusterLink) {
		link.target.clusterIsVisible = link.target.clusterIsVisible
			|| (link.source.isVisible() && !g_options.FILTER_CLUSTER);
	    }
	}

	for ( var i = 0; i < links.length; i++) {
	    var link = links[i];
	    if (link.isVisible()) {
		me.link_list_visible.push(link);
	    }
	}

	for ( var i = 0; i < nodes.length; i++) {
	    var node = nodes[i];
	    if (node.clusterIsVisible) {
		me.node_list_visible.push(node);
	    }
	}
    };

    NBodySimulator.prototype.initPositionNode = function(node) {
	key = 'c' + node.clusterId;
	if (null == nodesHash.get(key)) {
	    BarnesHutAlgorithm.initRandomPosition(node);
	    nodesHash.put(key, node);
	}
	this.initRandomPositionCluster(node, nodesHash.get(key));
    };

    NBodySimulator.prototype.initRandomPositionCluster = function(node, prototype) {
	gamma = 2 * Math.PI * Math.random();
	delta = Math.PI * Math.random();
	radius = g_options.DISTANCE * (1 + Math.random())*0.5;
	node.x = prototype.x + radius * Math.sin(delta) * Math.cos(gamma);
	node.y = prototype.y + radius * Math.sin(delta) * Math.sin(gamma);
	node.z = prototype.z + radius * Math.cos(delta);
    };

    NBodySimulator.prototype.simulateAllForces = function() {

	// Execute Barnes-Hut simulation
	octTree = new BarnesHutAlgorithm.OctTree();
	octTree.run(this.node_list_visible);

	// Calculate link forces
	me = this;
	this.link_list_visible.forEach(function(link) {
	    if (link.source.id != link.target.id) {
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
	deltaX = (link.source.x - link.target.x);
	deltaY = (link.source.y - link.target.y);
	deltaZ = (link.source.z - link.target.z);
	radius = Math.sqrt(deltaX * deltaX + deltaY * deltaY + deltaZ * deltaZ);
	if (radius > 1e-6) {
	    factor = (radius - g_options.DISTANCE) / radius / radius * g_options.SPRING;
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
	node.x = Math.min(Math.max(1 - g_options.SPHERE_RADIUS, node.x), g_options.SPHERE_RADIUS - 1);
	node.y = Math.min(Math.max(1 - g_options.SPHERE_RADIUS, node.y), g_options.SPHERE_RADIUS - 1);
	node.z = Math.min(Math.max(1 - g_options.SPHERE_RADIUS, node.z), g_options.SPHERE_RADIUS - 1);
    };

    /**
     * Ensure that the new position is in the sphere
     */
    NBodySimulator.prototype.scaleForceToBeSmall = function(node) {
	radius = Math.sqrt(node.force_x * node.force_x + node.force_y * node.force_y + node.force_z * node.force_z);
	if (radius > g_options.SPHERE_RADIUS_MINIMUM) {
	    node.force_x *= g_options.SPHERE_RADIUS_MINIMUM / radius;
	    node.force_y *= g_options.SPHERE_RADIUS_MINIMUM / radius;
	    node.force_z *= g_options.SPHERE_RADIUS_MINIMUM / radius;
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