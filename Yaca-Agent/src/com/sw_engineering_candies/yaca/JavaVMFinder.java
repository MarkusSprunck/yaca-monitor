package com.sw_engineering_candies.yaca;

import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.sun.tools.attach.AttachNotSupportedException;
import com.sun.tools.attach.VirtualMachine;
import com.sun.tools.attach.VirtualMachineDescriptor;

public class JavaVMFinder {

    /**
     * Constants
     */
    private static final Log LOGGER = LogFactory.getLog(JavaVMFinder.class);
    private static final String NL = System.getProperty("line.separator");

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

    public static void main(String[] args) throws Exception {
	List<Integer> processIDs = findOtherAttachableJavaVMs();
	System.out.println(processIDs);
    }

}