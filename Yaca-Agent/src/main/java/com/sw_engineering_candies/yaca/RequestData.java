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
