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
 import java.util.concurrent.CopyOnWriteArrayList;
 import java.util.regex.Pattern;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 
 import sun.tools.attach.HotSpotVirtualMachine;
 
 import com.sun.tools.attach.AttachNotSupportedException;
 import com.sun.tools.attach.VirtualMachine;
 import com.sun.tools.attach.VirtualMachineDescriptor;
 
 /**
  * The class collects call stack data from the VM
  */
 public class Analyser {
	
	/**
	 * Constants
	 */
	private static final Log				LOGGER				= LogFactory.getLog(Analyser.class);
	private static final String				NL					= System.getProperty("line.separator");
	public static final String				INVALID_PROCESS_ID	= "----";
	private static String					currentProcessID	= INVALID_PROCESS_ID;
	private static String					newProcessID		= "";
	private static boolean					isConnected			= false;
	static CopyOnWriteArrayList<Integer>	allVirtualMachines	= new CopyOnWriteArrayList<Integer>();
	public Model							model				= null;
	
	public Analyser(Model model) {
		this.model = model;
	}
	
	public synchronized void start() {
		
		HotSpotVirtualMachine hsVm = null;
		do {
			
			try {
				if (allVirtualMachines.size() == 0) {
					findOtherAttachableJavaVMs();
					LOGGER.debug("VirtualMachines=" + allVirtualMachines);
					if (!allVirtualMachines.isEmpty()) {
						newProcessID = allVirtualMachines.get(0).toString();
						LOGGER.debug("Select pid=" + currentProcessID);
					}
				} else if (!currentProcessID.equals(newProcessID)) {
					LOGGER.info("request change from pid=" + currentProcessID + " to pid=" + newProcessID + " allVirtualMachines="
							+ allVirtualMachines);
					hsVm = (HotSpotVirtualMachine) VirtualMachine.attach(newProcessID);
					model.setActiveProcess(newProcessID);
					model.reset();
					currentProcessID = newProcessID;
					isConnected = true;
				} else if (isConnected) {
					
					// Update filter white list
					final String filterWhite = model.getFilterWhiteList();
					final Pattern patternWhiteList = Pattern.compile(filterWhite);
					final String filterBlack = model.getFilterBlackList();
					final Pattern patternBlackList = Pattern.compile(filterBlack);
					
					try {
						final List<Node> entryList = new ArrayList<Node>(10);
						final InputStream in = hsVm.remoteDataDump(new Object[0]);
						final BufferedReader br = new BufferedReader(new InputStreamReader(in));
						String line = "";
						while ((line = br.readLine()) != null) {
							if (line.startsWith("\tat ") && line.length() > 10) {
								LOGGER.debug(line);
								final String temp2 = line.substring(4, line.lastIndexOf('('));
								final String[] split = temp2.split("\\.");
								if (split.length > 2) {
									final int indexOfMethodName = split.length - 1;
									final int indexOfClassName = indexOfMethodName - 1;
									final StringBuffer packageName = new StringBuffer(line.length());
									packageName.append(split[0]);
									for (int i = 1; i < indexOfClassName; i++) {
										packageName.append('.').append(split[i]);
									}
									
									String packageString = packageName.toString();
									final boolean isWhiteListOk = patternWhiteList.matcher(temp2).find();
									if (isWhiteListOk) {
										final boolean isBlackListOk = filterBlack.isEmpty() || !patternBlackList.matcher(temp2).find();
										if (isBlackListOk) {
											String className = split[indexOfClassName];
											final Node entry = new Node();
											entry.setMethodName(split[indexOfMethodName]);
											entry.setClassName(className);
											entry.setPackageName(packageString);
											entry.setNewItem(true);
											entryList.add(entry);
										}
									}
									
								} else {
									LOGGER.warn("can't process line '" + line + "'");
								}
							}
						}
						model.append(entryList, true, true);
						
						br.close();
						in.close();
						
					} catch (final IOException e) {
						LOGGER.debug("IOException " + e.getMessage());
						isConnected = false;
					}
				}
				
			} catch (final AttachNotSupportedException e) {
				if (isConnected) {
					LOGGER.error("AttachNotSupportedException " + e.getMessage());
				}
				model.reset();
				// isConnected = false;
			} catch (IOException e) {
				LOGGER.error("IOException " + e.getMessage());
				model.reset();
				// isConnected = false;
			}
		} while (true);
	}
	
	public synchronized static List<Integer> findOtherAttachableJavaVMs() {
		
		allVirtualMachines.clear();
		
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
					message.append("   sun.arch.data.model=").append(props.getProperty("sun.arch.data.model")).append(NL);
					
					Properties properties = vm.getAgentProperties();
					Enumeration<Object> keys = properties.keys();
					while (keys.hasMoreElements()) {
						Object elementKey = keys.nextElement();
						message.append("   ").append(elementKey).append("=").append(properties.getProperty(elementKey.toString())).append(NL);
					}
					LOGGER.debug(message);
					vm.detach();
					
					int processId = Integer.parseInt(nextPID);
					allVirtualMachines.add(processId);
				} catch (AttachNotSupportedException e) {
					LOGGER.error(e.getMessage());
				} catch (IOException e) {
					LOGGER.error(e.getMessage());
				}
			}
		}
		
		return allVirtualMachines;
	}
	
	public synchronized static void setProcessNewID(String processIdNew) {
		String value = processIdNew.trim();
		try {
			Integer.valueOf(value);
			LOGGER.info("set new process id=" + value);
			Analyser.newProcessID = value;
		} catch (Exception ex) {
			LOGGER.error("invalid id=" + value);
		}
	}
 }
