/** Copyright (C) 2013, Markus Sprunck
 *
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or
 * without modification, are permitted provided that the following
 * conditions are met:
 *
 * - Redistributions of source code must retain the above copyright
 *   notice, this list of conditions and the following disclaimer.
 *
 * - Redistributions in binary form must reproduce the above
 *   copyright notice, this list of conditions and the following
 *   disclaimer in the documentation and/or other materials provided
 *   with the distribution.
 *
 * - The name of its contributor may be used to endorse or promote
 *   products derived from this software without specific prior
 *   written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND
 * CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES,
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
 * ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 *******************************************************************/

/**
 * User interface to change parameters
 */
function initDatGui(container) {
    var gui = new dat.GUI({
	autoPlace : false
    });

    f1 = gui.addFolder('Agent Connection');
    f1.add(g_options, 'RUN_IMPORT').name('Run');
    f1.add(g_options, 'RUN_IMPORT_INTERVAL', 500, 10000).step(500).name('Interval [ms]').onChange(function(value) {
	clearInterval(g_updateTimerImport);
	g_updateTimerImport = setInterval(function() {
	    updateTimerImport();
	}, g_options.RUN_IMPORT_INTERVAL);
    });
    ;
    f1.open();

    f2 = gui.addFolder('Filter');
    f2.add(g_options, 'FILTER_CLUSTER').name('Cluster');
    f2.add(g_options, 'RENDER_THRESHOLD', 0.0, 100.0).step(1.0).name('Activity Index');
    f2.add(g_options, 'RUN_IMPORT_FILTER').name('Names').listen().onChange(function(value) {
	g_filter = new RegExp(g_options.RUN_IMPORT_FILTER);
    });
    f2.open();

    f3 = gui.addFolder('Simulation');
    f3.add(g_options, 'RUN_SIMULATION').name('Run');
    f3.add(g_options, 'SPHERE_RADIUS', 400, 2000).step(100.0).name('Sphere Radius').onChange(function(value) {
	g_simulator.node_list.forEach(function(node) {
	    g_simulator.scaleToBeInSphere(node);
	});
    });
    f3.add(g_options, 'CHARGE', 0, 1000).step(10.0).name('Charge');
    f3.add(g_options, 'GRAVITY', 0, 2000).step(100.0).name('Gravity');
    f3.add(g_options, 'DISTANCE', g_options.SPHERE_RADIUS_MINIMUM * 2, g_options.SPHERE_RADIUS_MINIMUM * 20).step(10.0)
	    .name('Link Distance');
    f3.add(g_options, 'SPRING', 0.0, 20).step(1.0).name('Spring Link');
    f3.open();

    f2 = gui.addFolder('Display');
    f2.add(g_options, 'DISPLAY_CUBE').name('Show Borders');
    f2.add(g_options, 'DISPLAY_NAMES').name('Show Names');
    f2.add(g_options, 'DISPLAY_DIRECTIONS').name('Show Directions');
    f2.open();

    gui.domElement.style.position = 'absolute';
    gui.domElement.style.right = '' + BORDER_RIGHT + 'px';
    gui.domElement.style.top = '36px';
    container.appendChild(gui.domElement);

    return gui.domElement.offsetLeft;
}