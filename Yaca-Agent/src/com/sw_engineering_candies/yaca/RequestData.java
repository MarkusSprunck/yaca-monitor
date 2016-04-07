package com.sw_engineering_candies.yaca;

import java.io.DataInputStream;
import java.io.InputStream;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class RequestData {
    
    /**
     * Constants
     */
    private static final Log LOGGER = LogFactory.getLog(RequestData.class);
    
    /**
     * Attributes
     */
    private String firstLine = "";
    
    private String body = "";
    
    /**
     * Constructor
     */
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
                int indexOfNewline = requestString.indexOf("\r\n");
                if (-1 != indexOfNewline) {
                    firstLine = requestString.substring(0, indexOfNewline);
                    body = requestString.substring(requestString.lastIndexOf("\r\n") + 2, length);
                }
            }
        } catch (Exception ex) {
            LOGGER.warn("Can't parse Header ", ex);
        }
    }
    
    public String getBody() {
        return body;
    }
    
    public String getFirstLine() {
        return firstLine;
    }
    
    public boolean isStartingWith(String string) {
        return getFirstLine().startsWith(string);
    }
    
}
