/*
 * Copyright (C) 2012-2014, Markus Sprunck <sprunck.markus@gmail.com>
 *
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * - Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer.
 *
 * - Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 *
 * - The name of its contributor may be used to endorse or promote products
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
 */
package com.sw_engineering_candies.yaca;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Vector;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * The class collects data from dynamic code analysis and provides the result in
 * the JSON format
 */
public class Model {

    /**
     * Constants
     */
    private static final Log LOGGER = LogFactory.getLog(Model.class);
    private static final String NL = System.getProperty("line.separator");
    private static final int EXPECTED_NUMBER_OF_LINKS = 2000;
    private static final int EXPECTED_NUMBER_OF_CLUSTERS = 500;
    private static final int EXPECTED_NUMBER_OF_NODES = 1000;

    public class Item {

	private String packageName;

	private String className;

	private String methodName;

	private boolean newItem;

	public boolean isNewItem() {
	    return newItem;
	}

	public void setNewItem(boolean newItem) {
	    this.newItem = newItem;
	}

	public String getPackageName() {
	    return packageName;
	}

	public String getClassName() {
	    return className;
	}

	public String getMethodName() {
	    return methodName;
	}

	@Override
	public String toString() {
	    return "Entry [" + packageName + ", " + className + ", " + methodName + "]";
	}

	public void setMethodName(final String methodName) {
	    // Assertions - start
	    assert methodName != null : "Null is not allowed";
	    assert !methodName.isEmpty() : "Empty is not allowed";
	    // Assertions - end
	    this.methodName = methodName;
	}

	public void setClassName(final String className) {
	    // Assertions - start
	    assert className != null : "Null is not allowed";
	    assert !className.isEmpty() : "Empty is not allowed";
	    // Assertions - end
	    this.className = className;
	}

	public void setPackageName(final String packageName) {
	    // Assertions - start
	    assert packageName != null : "Null is not allowed";
	    assert !packageName.isEmpty() : "Empty is not allowed";
	    // Assertions - end
	    this.packageName = packageName;
	}
    }

    /**
     * The map stores all created nodes
     */
    private final Map<String, String> nodes = new HashMap<String, String>(EXPECTED_NUMBER_OF_NODES);

    /**
     * The list nodeIds is used to find the correct ids for the creation of the
     * JSON file
     */
    private final List<String> nodeIds = new Vector<String>(EXPECTED_NUMBER_OF_NODES);

    /**
     * The list clusterIds is used to find the correct ids for the creation of
     * the JSON file
     */
    private final List<String> clusterIds = new Vector<String>(EXPECTED_NUMBER_OF_CLUSTERS);

    /**
     * The list clusterIds is used to find the correct ids for the creation of
     * the JSON file
     */
    private final List<String> linkIds = new Vector<String>(EXPECTED_NUMBER_OF_LINKS);

    /**
     * The map links all created links
     */
    private final Map<String, String> links = new HashMap<String, String>(EXPECTED_NUMBER_OF_LINKS);

    /**
     * The list is used to count the finding of each link
     */
    private final Map<String, Long> nodesCount = new HashMap<String, Long>(EXPECTED_NUMBER_OF_NODES);

    /**
     * Used to find the maximal count of a node, to scale all nodes to a
     * reasonable range
     */
    private long maxValueNodeCount = 1L;

    /**
     * Allocate right size for StringBuffer
     */
    private int lastLength = 1000;

    /**
     * Id of the current active process
     */
    private String activeProcess = "----";

    /**
     * Method updateLinks analyzes the call stack of all threads and collects
     * the data in the class ResultData.
     */
    public synchronized void append(final List<Item> entryList, final boolean countNodes, final boolean countLinks) {
	final int maxIndex = entryList.size() - 1;
	if (maxIndex > 0) {
	    for (int i = 0; i < maxIndex; i++) {
		add(entryList.get(i), entryList.get(i + 1), countNodes, countLinks);
	    }
	}
    }

    public synchronized void setActiveProcess(String processId) {
	this.activeProcess = processId;
    }

    public synchronized void reset() {
	resetCouters();
	nodes.clear();
	nodeIds.clear();
	clusterIds.clear();
	linkIds.clear();
	links.clear();
	nodesCount.clear();
	LOGGER.info("reset");
    }

    public synchronized StringBuffer getJSONPModel() {

	final StringBuffer fw = new StringBuffer(lastLength + 1000);

	final List<String> nodeskeys = new Vector<String>();
	nodeskeys.addAll(nodes.keySet());

	fw.append("yaca_agent_callback({" + NL);

	List<Integer> processIDs = CallStackAnalyser.findOtherAttachableJavaVMs();
	fw.append("\"process_id_available\":[");
	fw.append(NL);
	boolean isFrist = true;
	for (int index = 0; index < processIDs.size(); index++) {
	    final String pid = processIDs.get(index).toString();
	    fw.append((isFrist ? "    " : ",") + "");
	    isFrist = false;
	    fw.append(pid);
	}
	fw.append(NL + "],");
	fw.append(NL);

	fw.append("\"process_id_active\":\"" + this.activeProcess + "\"," + NL);

	fw.append("\"nodes\":[");
	fw.append(NL);
	isFrist = true;
	for (int index = 0; index < nodeIds.size(); index++) {
	    final String key = nodeIds.get(index);
	    if (nodes.containsKey(key)) {
		fw.append((isFrist ? "" : "," + NL) + "");
		isFrist = false;
		fw.append(String.format(Locale.ENGLISH, nodes.get(key), nodesCount.get(key)));
	    }
	}
	fw.append(NL + "],");
	fw.append(NL);

	fw.append("\"links\":[");
	fw.append(NL);
	isFrist = true;
	for (final String key : linkIds) {
	    if (links.containsKey(key)) {
		fw.append((isFrist ? "" : "," + NL) + "");
		isFrist = false;
		fw.append(links.get(key));
	    }
	}
	fw.append(NL + "]})");
	fw.append(NL);

	logStatistic();
	resetCouters();
	lastLength = fw.length();
	return fw;
    }

    private void logStatistic() {
	final StringBuffer message = new StringBuffer(200);
	message.append("pid=").append(activeProcess);
	message.append(", clusters=").append(clusterIds.size());
	message.append(", nodes=").append(nodeIds.size());
	message.append(", links=").append(links.size());
	LOGGER.info(message);
    }

    private void add(final Item targetEntry, final Item sourceEntry, final boolean countNodes, final boolean countLinks) {

	// add target cluster
	final String targetClusterKey = getClusterKey(targetEntry);
	if (!clusterIds.contains(targetClusterKey)) {
	    clusterIds.add(targetClusterKey);
	}
	// add target cluster node
	if (!nodeIds.contains(targetClusterKey)) {
	    nodeIds.add(targetClusterKey);
	    String nodeString = "\t{\"id\":" + nodeIds.indexOf(targetClusterKey) + ", \"clusterId\":"
		    + clusterIds.indexOf(targetClusterKey) + ", \"name\":\"" + targetClusterKey + "\" , \"alias\":\""
		    + targetEntry.getPackageName() + "\", \"calls\": %d , \"isClusterNode\" : true }";
	    nodes.put(targetClusterKey, nodeString);
	    incrementNodeCount(targetClusterKey);
	}

	// add source cluster
	final String sourceClusterKey = getClusterKey(sourceEntry);
	if (!clusterIds.contains(sourceClusterKey)) {
	    clusterIds.add(sourceClusterKey);
	}
	// add source cluster node
	if (!nodeIds.contains(sourceClusterKey)) {
	    nodeIds.add(sourceClusterKey);
	    String nodeString = "\t{\"id\":" + nodeIds.indexOf(sourceClusterKey) + ", \"clusterId\":"
		    + clusterIds.indexOf(sourceClusterKey) + ", \"name\":\"" + sourceClusterKey + "\" , \"alias\":\""
		    + sourceEntry.getPackageName() + "\", \"calls\": %d , \"isClusterNode\" : true }";
	    nodes.put(sourceClusterKey, nodeString);
	    incrementNodeCount(sourceClusterKey);
	}

	// add target node
	final String targetKey = getNodeKey(targetEntry);
	if (!nodeIds.contains(targetKey)) {
	    nodeIds.add(targetKey);
	}
	String nodeString = "\t{\"id\":" + nodeIds.indexOf(targetKey) + ", \"clusterId\":"
		+ clusterIds.indexOf(targetClusterKey) + ", \"name\":\"" + targetKey + "\" , \"alias\":\""
		+ targetEntry.getClassName() + '.' + targetEntry.getMethodName()
		+ "\", \"calls\": %d , \"isClusterNode\" : false  }";
	nodes.put(targetKey, nodeString);
	incrementNodeCount(targetKey);

	// add source node
	final String sourceKey = getNodeKey(sourceEntry);
	if (!nodeIds.contains(sourceKey)) {
	    nodeIds.add(sourceKey);
	}
	nodeString = "\t{\"id\":" + nodeIds.indexOf(sourceKey) + ", \"clusterId\":"
		+ clusterIds.indexOf(sourceClusterKey) + ", \"name\":\"" + sourceKey + "\" , \"alias\":\""
		+ sourceEntry.getClassName() + '.' + sourceEntry.getMethodName()
		+ "\", \"calls\": %d , \"isClusterNode\" : false }";
	nodes.put(sourceKey, nodeString);
	incrementNodeCount(sourceKey);

	// add node link
	final String keyLink = targetKey + "<-" + sourceKey;
	if (!linkIds.contains(keyLink)) {
	    linkIds.add(keyLink);
	}
	nodeString = "\t{\"id\":" + linkIds.indexOf(keyLink) + ", \"sourceId\":" + nodeIds.indexOf(sourceKey)
		+ ", \"targetId\":" + nodeIds.indexOf(targetKey) + ", \"isClusterLink\" : false }";
	links.put(keyLink, nodeString);

	// add cluster link
	final String keyLink11 = targetClusterKey + "<-" + targetKey;
	if (!linkIds.contains(keyLink11)) {
	    linkIds.add(keyLink11);
	}
	nodeString = "\t{\"id\":" + linkIds.indexOf(keyLink11) + ", \"sourceId\":" + nodeIds.indexOf(targetKey)
		+ ", \"targetId\":" + nodeIds.indexOf(targetClusterKey) + ", \"isClusterLink\" : true }";
	links.put(keyLink11, nodeString);
    }

    private void incrementNodeCount(final String keyNode) {
	long value = 0L;
	if (nodesCount.containsKey(keyNode)) {
	    value = nodesCount.get(keyNode) + 1L;
	}
	maxValueNodeCount = Math.max(maxValueNodeCount, value);
	nodesCount.put(keyNode, value);
    }

    private void resetCouters() {
	for (final String key : nodesCount.keySet()) {
	    nodesCount.put(key, 0L);
	}
	maxValueNodeCount = 0L;
    }

    private String getNodeKey(Item item) {
	return item.getPackageName() + '.' + item.getClassName() + '.' + item.getMethodName();
    }

    private String getClusterKey(Item item) {
	return item.getPackageName() + '.' + item.getClassName();
    }

}
