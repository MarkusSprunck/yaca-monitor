
OctreeTest = TestCase("Octree Test");

OctreeTest.prototype.testSumOfAllForcesShouldBeZero = function () {
    var n = [{
        "id": 1,
        "x": 10,
        "y": 10,
        "z": 10
    }, {
        "id": 2,
        "x": -10,
        "y": -10,
        "z": -10
    }, {
        "id": 3,
        "x": -10,
        "y": -10,
        "z": -5
    }];

    barnesHut = new BarnesHutAlgorithmOctTree({ SPHERE_RADIUS: 1000 ,SPHERE_RADIUS_MINIMUM : 1.0, CHARGE: 1000 , THETA : 0.05, GRAVITY: 0});
    barnesHut.run(n);

    // sum of all forces should be zero
    threshold = 1e-14;
    assertEquals(threshold, Math.max(threshold, Math.abs(n[0].force_x + n[1].force_x + n[2].force_x)));
    assertEquals(threshold, Math.max(threshold, Math.abs(n[0].force_y + n[1].force_y + n[2].force_y)));
    assertEquals(threshold, Math.max(threshold, Math.abs(n[0].force_z + n[1].force_z + n[2].force_z)));

    // but single forces should not be zero
    assertEquals(36.17886178861788, n[0].force_y);
    assertEquals(-16.666666666666664, n[1].force_y);
    assertEquals(-19.51219512195122, n[2].force_y);
};


OctreeTest.prototype.testTooMuchLayers = function () {
    var n = [{
        "id": 2,
        "x": 100,
        "y": 100,
        "z": 100
    }, {
        "id": 1,
        "x": 100.1,
        "y": 100,
        "z": 100
    }];

    barnesHut = new BarnesHutAlgorithmOctTree({ SPHERE_RADIUS: 160 ,SPHERE_RADIUS_MINIMUM : 10});
    barnesHut.run(n);

    assertEquals(100, n[0].x);
    assertEquals(100, n[0].y);
    assertEquals(100, n[0].z);

    assertTrue(100 != n[1].x);
    assertTrue(100 != n[1].y);
    assertTrue(100 != n[1].z);
};

OctreeTest.prototype.testManyNodesBarnesHutLargeTheta = function () {
    n = [];

    SPHERE_RADIUS = 10000.0;

    id = 0;
    for (var gamma = 0.1; gamma < 1000.0; gamma += 10.0) {
        for (var delta = 0.1; delta < 10.0; delta += 1.0) {
            id++;
            radius = 10 + SPHERE_RADIUS * Math.random() * 0.9;
            object = {
                "id": id
            };
            object.x = radius * Math.sin(delta) * Math.cos(gamma);
            object.y = radius * Math.sin(delta) * Math.sin(gamma);
            object.z = radius * Math.cos(delta);
            n.push(object);
        }
    }
    barnesHut = new BarnesHutAlgorithmOctTree({ SPHERE_RADIUS: 1000 ,SPHERE_RADIUS_MINIMUM : 1.0, CHARGE: 1000 , THETA : 0.8, GRAVITY: 0});
    barnesHut.run(n);

    sum_x = 0.0;
    sum_y = 0.0;
    sum_z = 0.0;
    sum_x_mean = 0.0;
    sum_y_mean = 0.0;
    sum_z_mean = 0.0;
    for (var index = 0; index < n.length; index++) {
        sum_x += n[index].force_x;
        sum_y += n[index].force_y;
        sum_z += n[index].force_z;
        sum_x_mean += Math.abs(n[index].force_x);
        sum_y_mean += Math.abs(n[index].force_y);
        sum_z_mean += Math.abs(n[index].force_z);
    }
    // sum of all forces should be zero
    threshold = 0.15;
    assertEquals(threshold, Math.max(threshold, Math.abs(sum_x / sum_x_mean)));
    assertEquals(threshold, Math.max(threshold, Math.abs(sum_y / sum_y_mean)));
    assertEquals(threshold, Math.max(threshold, Math.abs(sum_z / sum_z_mean)));
};

OctreeTest.prototype.testManyNodesBarnesHutSmallTheta = function () {
    n = [];

    SPHERE_RADIUS = 10000.0;

    id = 0;
    for (var gamma = 0.1; gamma < 1000.0; gamma += 10.0) {
        for (var delta = 0.1; delta < 10.0; delta += 1.0) {
            id++;
            radius = 10 + SPHERE_RADIUS * Math.random() * 0.9;
            object = {
                "id": id
            };
            object.x = radius * Math.sin(delta) * Math.cos(gamma);
            object.y = radius * Math.sin(delta) * Math.sin(gamma);
            object.z = radius * Math.cos(delta);
            n.push(object);
        }
    }
    barnesHut = new BarnesHutAlgorithmOctTree({ SPHERE_RADIUS: 1000 ,SPHERE_RADIUS_MINIMUM : 1.0, CHARGE: 1000 , THETA : 0.25, GRAVITY: 0});
    barnesHut.run(n);

    sum_x = 0.0;
    sum_y = 0.0;
    sum_z = 0.0;
    sum_x_mean = 0.0;
    sum_y_mean = 0.0;
    sum_z_mean = 0.0;
    for (var index = 0; index < n.length; index++) {
        sum_x += n[index].force_x;
        sum_y += n[index].force_y;
        sum_z += n[index].force_z;
        sum_x_mean += Math.abs(n[index].force_x);
        sum_y_mean += Math.abs(n[index].force_y);
        sum_z_mean += Math.abs(n[index].force_z);
    }
    // sum of all forces should be zero
    threshold = 0.1;
    assertEquals(threshold, Math.max(threshold, Math.abs(sum_x / sum_x_mean)));
    assertEquals(threshold, Math.max(threshold, Math.abs(sum_y / sum_y_mean)));
    assertEquals(threshold, Math.max(threshold, Math.abs(sum_z / sum_z_mean)));

};

OctreeTest.prototype.testManyNodesBruteForce = function () {
    n = [];

    SPHERE_RADIUS = 10000.0;

    id = 0;
    for (var gamma = 0.1; gamma < 1000.0; gamma += 10.0) {
        for (var delta = 0.1; delta < 10.0; delta += 1.0) {
            id++;
            radius = 10 + SPHERE_RADIUS * Math.random() * 0.9;
            object = {
                "id": id
            };
            object.x = radius * Math.sin(delta) * Math.cos(gamma);
            object.y = radius * Math.sin(delta) * Math.sin(gamma);
            object.z = radius * Math.cos(delta);
            n.push(object);
        }
    }

    barnesHut = new BarnesHutAlgorithmOctTree({ SPHERE_RADIUS: 1000 ,SPHERE_RADIUS_MINIMUM : 1.0, CHARGE: 1000 , THETA : 0.0, GRAVITY: 0});
    barnesHut.run(n);

    sum_x = 0.0;
    sum_y = 0.0;
    sum_z = 0.0;
    sum_x_mean = 0.0;
    sum_y_mean = 0.0;
    sum_z_mean = 0.0;
    for (var index = 0; index < n.length; index++) {
        sum_x += n[index].force_x;
        sum_y += n[index].force_y;
        sum_z += n[index].force_z;
        sum_x_mean += Math.abs(n[index].force_x);
        sum_y_mean += Math.abs(n[index].force_y);
        sum_z_mean += Math.abs(n[index].force_z);
    }
    // sum of all forces should be zero
    threshold = 1e-9;
    assertEquals(threshold, Math.max(threshold, Math.abs(sum_x / sum_x_mean)));
    assertEquals(threshold, Math.max(threshold, Math.abs(sum_y / sum_y_mean)));
    assertEquals(threshold, Math.max(threshold, Math.abs(sum_z / sum_z_mean)));

};
