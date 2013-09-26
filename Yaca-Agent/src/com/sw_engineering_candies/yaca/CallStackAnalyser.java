/*
 * Copyright (C) 2012-2013, Markus Sprunck <sprunck.markus@gmail.com>
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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import sun.tools.attach.HotSpotVirtualMachine;

import com.sun.tools.attach.AttachNotSupportedException;
import com.sun.tools.attach.VirtualMachine;

/**
 * The class collects call stack data from the VM
 */
public class CallStackAnalyser {

	/**
	 * Constants
	 */
	private static final Log LOGGER = LogFactory.getLog(CallStackAnalyser.class);
	private static final String NL = System.getProperty("line.separator");

	/**
	 * Model with data for export
	 */
	private final Model model;

	@SuppressWarnings("unused")
	private CallStackAnalyser() {
		this.model = null;
	}

	public CallStackAnalyser(final Model analyser) {
		assert analyser != null : "Null is not allowed";
		//
		this.model = analyser;
	}

	public void run(final String processId) {
		assert processId != null : "Null is not allowed";
		assert !processId.isEmpty() : "Empty is not allowed";
		//
		try {
			LOGGER.info("start for process id " + processId + NL);

			final VirtualMachine vm = VirtualMachine.attach(processId);
			final HotSpotVirtualMachine hsVm = (HotSpotVirtualMachine) vm;
			do {
				final List<Model.Item> entryList = new ArrayList<Model.Item>(10);
				final InputStream in = hsVm.remoteDataDump(new Object[0]);
				final BufferedReader br = new BufferedReader(new InputStreamReader(in));
				String line = "";
				while ((line = br.readLine()) != null) {
					if (line.startsWith("\tat ")) {
						final String temp2 = line.substring(4, line.lastIndexOf('('));
						final String[] split = temp2.split("\\.");
						if (split.length > 2) {
							final Model.Item entry = model.new Item();

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
							LOGGER.warn("can't process line '" + line + "'" + NL);
						}
					}
				}
				model.append(entryList, true, true);

				br.close();
				in.close();
			} while (true);
		} catch (final IOException e) {
			LOGGER.error(e.getMessage() + NL);
		} catch (final AttachNotSupportedException e) {
			LOGGER.error(e.getMessage() + NL);
		}
	}
}
