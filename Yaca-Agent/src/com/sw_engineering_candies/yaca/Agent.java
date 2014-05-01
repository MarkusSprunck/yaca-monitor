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

import java.net.InetAddress;
import java.net.UnknownHostException;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.Parser;
import org.apache.commons.cli.PosixParser;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * The main class of the application
 */
public class Agent {

    /**
     * Constants
     */
    private static final Log LOGGER = LogFactory.getLog(Agent.class);
    private static final String NL = System.getProperty("line.separator");
    private static final String OPTION_PORT = "port";
    private static final String OPTION_HELP = "help";
    private static final String HELP_TEXT_SHOW_HELP = "Show help information.";
    private static final String HELP_TEXT_PORT = "Mandatory parameter port for webserver.";

    public static final Model MODEL = new Model();

    public static CallStackAnalyser analysisCallStack = new CallStackAnalyser();

    /**
     * Port for this HTTP server
     */
    private static int port = 33333;

    private static Options createCommandLineOptions() {
	final Options options = new Options();
	options.addOption(OPTION_PORT, true, HELP_TEXT_PORT);
	options.addOption(OPTION_HELP, false, HELP_TEXT_SHOW_HELP);
	return options;
    }

    private static void outputCommandLineHelp(final Options options) {
	final HelpFormatter formater = new HelpFormatter();
	formater.printHelp("The YacaAgent connects to a JVM, runs a callstack "
		+ "analysis and provides result the yaca client.", options);
    }

    private static void startHTTPServer() {
	String hostname = "localhost";
	try {
	    InetAddress addr = InetAddress.getLocalHost();
	    hostname = addr.getCanonicalHostName();
	} catch (UnknownHostException e) {
	    LOGGER.error(e.getMessage() + NL);
	}
	LOGGER.info("start for url: http:\\\\" + hostname + ':' + port + NL);
	final Thread serverThread = new WebServer(port);
	serverThread.start();
    }

    private static void processCommandline(final CommandLine cl) throws IllegalArgumentException {
	if ((null != cl) && cl.hasOption(OPTION_PORT)) {
	    try {
		port = Integer.parseInt(cl.getOptionValue(OPTION_PORT));
		if ((port < 1) || (port > 65535)) {
		    port = 8080;
		}
	    } catch (final NumberFormatException e) {
		LOGGER.error(e.getMessage() + NL);
		throw new IllegalArgumentException();
	    }
	}
    }

    public static void main(final String[] args) {

	LOGGER.info("(c) 2012-2014 by Markus Sprunck, v2.1 - 27.04.2014" + NL);

	// 1) Parse command line and store parameter in attributes
	final Parser commandlineparser = new PosixParser();
	final Options options = createCommandLineOptions();
	CommandLine cl = null;
	try {
	    cl = commandlineparser.parse(options, args, true);
	} catch (final ParseException exp) {
	    LOGGER.error("Unexpected exception:" + exp.getMessage() + NL);
	}
	try {
	    if (null != cl) {
		processCommandline(cl);
	    }
	} catch (final IllegalArgumentException e) {
	    outputCommandLineHelp(options);
	    LOGGER.warn("Illegal arguments on command line: " + e.getMessage() + NL);
	    return;
	}
	if ((null != cl) && cl.hasOption(OPTION_HELP) && (args.length >= 4)) {
	    outputCommandLineHelp(options);
	    return;
	}

	// 2) Starts new thread for Http Server
	startHTTPServer();

	// 3) Start dynamic code analysis
	analysisCallStack.run();
    }

}
