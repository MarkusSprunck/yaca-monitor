package com.sw_engineering_candies.yaca;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class RequestData {
	
	private static final Log	LOGGER		= LogFactory.getLog(RequestData.class);
	
	private String				body		= "";
	
	private String				firstLine	= "";
	
	public RequestData(InputStream in) {
		
		try {
			StringBuffer buffer = new StringBuffer(1000);
			final DataInputStream remoteInput = new DataInputStream(in);
			final byte[] data = new byte[Short.MAX_VALUE];
			int length = remoteInput.read(data, 0, Short.MAX_VALUE);
			for (byte b : data) {
				buffer.append((char) b);
			}
			
			String requestString = buffer.toString();
			if (!requestString.isEmpty()) {
				firstLine = requestString.substring(0, requestString.indexOf("\r\n"));
				body = requestString.substring(requestString.lastIndexOf("\r\n") + 2, length);
			}
		} catch (IOException ex) {
			LOGGER.error(ex);
		}
	}
	
	public String getBody() {
		return body;
	}
	
	public String getFirstLine() {
		return firstLine;
	}
	
	public boolean startsWith(String string) {
		return getFirstLine().startsWith(string);
	}
	
}
