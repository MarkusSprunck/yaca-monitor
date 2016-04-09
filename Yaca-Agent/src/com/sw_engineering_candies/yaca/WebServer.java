/*
 * Copyright (C) 2012-2016, Markus Sprunck <sprunck.markus@gmail.com>
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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * The class is a minimal web server to provide data to a external HTML client
 */
public class WebServer extends Thread {
    
    /**
     * Constants
     */
    private static final Log LOGGER = LogFactory.getLog(WebServer.class);
    
    private static final String NL = System.getProperty("line.separator");
    
    private final List<String> STATIC_JS_FILES = Arrays.asList("dat.gui.js", //
            "detector.js", //
            "three.js", //
            "trackballcontrols.js", //
            "projector.js", //
            "helvetiker_regular.typeface.js", //
            "stats.min.js", //
            "traceur.js", //
            "bootstrap.js", //
            "geometryutils.js", //
            "detector.js");
    
    /**
     * Attributes
     */
    private ClassLoader classLoader = null;
    
    private Model model = null;
    
    private final int port;
    
    /**
     * Constructor
     */
    public WebServer(final int port, Model model) {
        this.port = port;
        this.model = model;
        
        if (classLoader == null) {
            classLoader = Class.class.getClassLoader();
        }
    }
    
    @Override
    public void run() {
        
        // Create server socket
        ServerSocket serverSocket = null;
        try {
            serverSocket = new ServerSocket(this.port);
        } catch (IOException ex) {
            LOGGER.error(ex);
            stopServer();
        }
        
        while (serverSocket != null) {
            
            try {
                // Get connection
                final Socket socket = serverSocket.accept();
                socket.setSoTimeout(2000);
                
                // Get I/O streams
                final OutputStream out = socket.getOutputStream();
                final InputStream in = socket.getInputStream();
                
                // Handle request
                RequestData request = new RequestData(in);
                if (request.isStartingWith("GET /favicon.ico")) {
                    sendResponseForStaticFile(out, request.getFirstLine(), "favicon.ico", "image/x-icon");
                } else if (request.isStartingWith("GET /monitor/styles/main.css")) {
                    sendResponseForStaticFile(out, request.getFirstLine(), "styles/main.css", "text/css");
                } else if (request.isStartingWith("GET /monitor/external")) {
                    sendResponseForAllStaticJavaScriptFiles(out, request.getFirstLine());
                } else if (request.isStartingWith("DELETE /analyzer")) {
                    stopServer();
                } else if (request.isStartingWith("DELETE /tasks")) {
                    model.reset();
                    sendResponseForString(out, "OK");
                } else if (request.isStartingWith("DELETE /filterWhite")) {
                    model.setFilterWhiteList("");
                    model.reset();
                    sendResponseForString(out, "OK");
                } else if (request.isStartingWith("DELETE /filterBlack")) {
                    model.setFilterBlackList("");
                    model.reset();
                    sendResponseForString(out, "OK");
                } else if (request.isStartingWith("PUT /filterWhite")) {
                    model.setFilterWhiteList(request.getBody());
                    model.reset();
                    sendResponseForString(out, "OK");
                } else if (request.isStartingWith("PUT /filterBlack")) {
                    model.setFilterBlackList(request.getBody());
                    model.reset();
                    sendResponseForString(out, "OK");
                } else if (request.isStartingWith("GET /process/ids")) {
                    sendResponseForProcessIdRequest(out);
                } else if (request.isStartingWith("PUT /process/id")) {
                    CallStackAnalyzer.setProcessNewID(request.getBody());
                    sendResponseForString(out, "OK");
                } else if (request.isStartingWith("GET /process")) {
                    sendResponseForModelRequest(out);
                } else if (request.isStartingWith("GET /monitor")) {
                    sendResponseForStaticFile(out, request.getFirstLine(), "index.html", "text/html");
                } else {
                    LOGGER.warn("Not expected request=" + request.getFirstLine());
                }
                
                // Close all resources
                try {
                    in.close();
                    out.close();
                    socket.close();
                } catch (final IOException e) {
                    LOGGER.warn("Could not close resources : " + e);
                }
                
            } catch (final Exception e) {
                LOGGER.warn("Could not handle request: ", e);
            }
        }
        
        stopServer();
    }
    
    private void stopServer() {
        LOGGER.info("Server stopped");
        System.exit(0);
    }
    
    private void sendResponseForAllStaticJavaScriptFiles(final OutputStream out, String request) throws Exception {
        for (String name : STATIC_JS_FILES) {
            if (request.contains(name)) {
                sendResponseForStaticFile(out, request, "external/" + name, "application/javascript");
            }
        }
    }
    
    private void sendResponseForModelRequest(final OutputStream out) throws Exception {
        
        // Create content of response
        final String jsonpModel = model.getJSONPModel();
        
        // For HTTP/1.0 or later send a MIME header
        final String headerString = "HTTP/1.1 200 OK" + NL //
                + "Server: YacaAgent 2.0" + NL //
                + "Content-Type: application/json" + NL // 
                + "Content-Length: " + jsonpModel.toString().getBytes("UTF-8").length + NL + NL;
        
        // Write response
        out.write(headerString.getBytes("UTF-8"));
        out.write(jsonpModel.toString().getBytes("UTF-8"));
        out.flush();
    }
    
    private void sendResponseForProcessIdRequest(final OutputStream out) throws Exception {
        
        // Find all process IDs
        CallStackAnalyzer.findOtherAttachableJavaVMs();
        LOGGER.info("VirtualMachines=" + CallStackAnalyzer.allVirtualMachines);
        
        // Give analyzer some time to collect data
        Thread.sleep(10);
        
        // Create content of response
        final String jsonpModel = model.getJSONPVM();
        
        // For HTTP/1.0 or later send a MIME header
        final String headerString = "HTTP/1.1 200 OK" + NL //
                + "Server: YacaAgent 2.0" + NL //
                + "Content-Type: application/json" + NL //
                + "Content-Length: " + jsonpModel.toString().getBytes("UTF-8").length + NL + NL;
        
        // Write response
        out.write(headerString.getBytes("UTF-8"));
        out.write(jsonpModel.toString().getBytes("UTF-8"));
        out.flush();
    }
    
    private void sendResponseForStaticFile(final OutputStream out, String request, String name, String mimeType) throws Exception {
        
        // Create content of requested file
        InputStream fileContent = getClass().getResourceAsStream("/" + name);
        byte[] bytesBody = readStream(fileContent);
        
        // For HTTP/1.0 or later send a MIME header
        final String headerString = "HTTP/1.1 200 OK" + NL //
                + "Server: YacaAgent 2.0" + NL //
                + "Content-Type: " + mimeType + NL //
                + "Content-Length: " + bytesBody.length + NL + NL;
        byte[] bytesHeader = headerString.getBytes();
        
        // Write response
        out.write(bytesHeader);
        out.write(bytesBody);
        out.flush();
        
        LOGGER.info("Rent " + bytesBody.length + " bytes as resonse for request=" + request);
    }
    
    private void sendResponseForString(final OutputStream out, String body) throws IOException {
        
        // For HTTP/1.0 or later send a MIME header
        final String headerString = "HTTP/1.1 200 OK" + NL //
                + "Server: YacaAgent 2.0" + NL //
                + "Content-Type: application/json" + NL //
                + "Content-Length: " + body.getBytes("UTF-8").length + NL + NL;
        
        // Write response
        out.write(headerString.getBytes("UTF-8"));
        out.write(body.toString().getBytes("UTF-8"));
        out.flush();
    }
    
    private static byte[] readStream(InputStream ins) {
        byte[] result = new byte[0];
        try {
            byte[] buffer = new byte[4096];
            ByteArrayOutputStream outs = new ByteArrayOutputStream();
            
            int read = 0;
            while ((read = ins.read(buffer)) != -1) {
                outs.write(buffer, 0, read);
            }
            
            ins.close();
            outs.close();
            result = outs.toByteArray();
        } catch (Exception e) {
            LOGGER.error("Can't read bytes from stream", e);
        }
        return result;
    }
    
}
