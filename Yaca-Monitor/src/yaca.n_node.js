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

function NNode(node) {
    this.id = node.id;
    this.name = node.name;
    this.alias = node.alias;

    this.isClusterNode = node.isClusterNode;
    this.clusterIsVisible = false;
    this.clusterId = node.clusterId;

    this.calls = node.calls;

    this.isFiltered = g_filter.test(node.name);

    this.x = node.x;
    this.y = node.y;
    this.z = node.z;

    this.force_x = 0.0;
    this.force_y = 0.0;
    this.force_z = 0.0;

    this.sphere = new THREE.Mesh();
    this.sphereCreated = false;

    this.text = new THREE.Mesh();
    this.textCreated = false;
}

NNode.prototype.update = function(node) {
    this.callFilter();
    this.calls = this.isFiltered ? node.calls : 0;
};

NNode.prototype.callFilter = function() {
    this.clusterIsVisible = false;
    this.isFiltered = g_filter.test(this.name);
};

NNode.prototype.getCalls = function() {
    return this.calls;
};

NNode.prototype.getActivity = function() {
    return this.isClusterNode ? 0 : 101 * (Math.log(this.getCalls()) / Math.log(g_simulator.maxNodeCalls));
};

NNode.prototype.isVisible = function() {
    return (this.getActivity() > g_options.RENDER_THRESHOLD) && this.isFiltered && !this.isClusterNode
	    || this.clusterIsVisible;
};

NNode.prototype.getRadius = function() {
    return this.isClusterNode ? 0.3 * g_options.SPHERE_RADIUS_MINIMUM : g_options.SPHERE_RADIUS_MINIMUM;
};