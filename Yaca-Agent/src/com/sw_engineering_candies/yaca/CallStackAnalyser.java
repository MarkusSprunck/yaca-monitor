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
 */package com.sw_engineering_candies.yaca;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.management.ManagementFactory;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import sun.tools.attach.HotSpotVirtualMachine;

import com.sun.tools.attach.AttachNotSupportedException;
import com.sun.tools.attach.VirtualMachine;
import com.sun.tools.attach.VirtualMachineDescriptor;

/**
 * The class collects call stack data from the VM
 */
public class CallStackAnalyser {

    public static final String INVALID_PROCESS_ID = "----";
    /**
     * Constants
     */
    private static final Log LOGGER = LogFactory.getLog(CallStackAnalyser.class);
    private static final String NL = System.getProperty("line.separator");
    private static String processIdOld = INVALID_PROCESS_ID;
    private static String processIdNew = INVALID_PROCESS_ID;
    private static boolean isConnected = false;

    public CallStackAnalyser() {
    }

    public synchronized void run() {

	HotSpotVirtualMachine hsVm = null;
	do {

	    try {
		if (!processIdNew.equals(processIdOld)
			&& findOtherAttachableJavaVMs().toString().contains(processIdNew)) {
		    LOGGER.info("request change from pid=" + processIdOld + " to pid=" + processIdNew);

		    hsVm = (HotSpotVirtualMachine) VirtualMachine.attach(processIdNew);
		    Agent.MODEL.setActiveProcess(processIdNew);
		    Agent.MODEL.reset();
		    isConnected = true;
		    processIdOld = processIdNew;

		} else if (isConnected) {
		    try {
			final List<Model.Item> entryList = new ArrayList<Model.Item>(10);
			final InputStream in = hsVm.remoteDataDump(new Object[0]);
			final BufferedReader br = new BufferedReader(new InputStreamReader(in));
			String line = "";
			while ((line = br.readLine()) != null) {
			    if (line.startsWith("\tat ")) {
				final String temp2 = line.substring(4, line.lastIndexOf('('));
				final String[] split = temp2.split("\\.");
				if (split.length > 2) {
				    final Model.Item entry = Agent.MODEL.new Item();

				    final int indexOfMethodName = split.length - 1;
				    entry.setMethodName(split[indexOfMethodName]);

				    final int indexOfClassName = indexOfMethodName - 1;
				    entry.setClassName(split[indexOfClassName]);

				    final StringBuffer packageName = new StringBuffer(line.length());
				    packageName.append(split[0]);
				    for (int i = 1; i < indexOfClassName; i++) {
					packageName.append('.').append(split[i]);
				    }
				    entry.setPackageName(packageName.toString());
				    entry.setNewItem(true);
				    entryList.add(entry);

				} else {
				    LOGGER.warn("can't process line '" + line + "'");
				}
			    }
			}
			Agent.MODEL.append(entryList, true, true);

			br.close();
			in.close();

		    } catch (final IOException e) {
			LOGGER.error("IOException " + e.getMessage());
			isConnected = false;
			processIdOld = INVALID_PROCESS_ID;
		    }
		} else {
		    // Nothing to do till the next request with a new process id
		    try {
			Thread.sleep(1000);
		    } catch (InterruptedException e) {
		    }
		}

	    } catch (final AttachNotSupportedException e) {
		if (isConnected) {
		    LOGGER.error("AttachNotSupportedException " + e.getMessage());
		}
		Agent.MODEL.reset();
		isConnected = false;
		processIdOld = INVALID_PROCESS_ID;
	    } catch (IOException e) {
		LOGGER.error("IOException " + e.getMessage());
		isConnected = false;
		processIdOld = INVALID_PROCESS_ID;
	    }
	} while (true);
    }

    public static List<Integer> findOtherAttachableJavaVMs() {
	List<Integer> result = new ArrayList<Integer>();

	List<VirtualMachineDescriptor> vmDesc = VirtualMachine.list();
	for (int i = 0; i < vmDesc.size(); i++) {
	    VirtualMachineDescriptor descriptor = vmDesc.get(i);

	    final String nextPID = descriptor.id();
	    final String ownPID = ManagementFactory.getRuntimeMXBean().getName().split("@")[0];
	    if (!ownPID.equals(nextPID)) {

		final StringBuilder message = new StringBuilder();
		message.append("   pid=").append(nextPID).append(NL);

		VirtualMachine vm;
		try {
		    vm = VirtualMachine.attach(descriptor);

		    Properties props = vm.getSystemProperties();
		    message.append("   java.version=").append(props.getProperty("java.version")).append(NL);
		    message.append("   java.vendor=").append(props.getProperty("java.vendor")).append(NL);
		    message.append("   java.home=").append(props.getProperty("java.home")).append(NL);
		    message.append("   sun.arch.data.model=").append(props.getProperty("sun.arch.data.model"))
			    .append(NL);

		    Properties properties = vm.getAgentProperties();
		    Enumeration<Object> keys = properties.keys();
		    while (keys.hasMoreElements()) {
			Object elementKey = keys.nextElement();
			message.append("   ").append(elementKey).append("=")
				.append(properties.getProperty(elementKey.toString())).append(NL);
		    }
		    LOGGER.debug(message);
		    vm.detach();

		    result.add(Integer.parseInt(nextPID));
		} catch (AttachNotSupportedException e) {
		    LOGGER.error(e.getMessage());
		} catch (IOException e) {
		    LOGGER.error(e.getMessage());
		}
	    }
	}
	return result;
    }

    public synchronized static void setProcessNewID(String processIdNew) {
	CallStackAnalyser.processIdNew = processIdNew;
    }
}
