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

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.util.HashMap;
import java.util.Map;

import javax.swing.Box;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.Timer;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;

/**
 * The main class of the application.
 */
public class YacaAgentTestClient {
	
	private static final Log	LOGGER					= LogFactory.getLog(YacaAgentTestClient.class);
	
	private static final String	BEAN_NAME_MERGE_SORT	= "mergeSort";
	
	private static final String	BEAN_NAME_QUICK_SORT	= "quickSort";
	
	private static final String	BEAN_NAME_HEAP_SORT		= "heapSort";
	
	private static final int	INPUT_ARRAY_SIZE		= 10000;
	
	private ISort				sortBean;
	
	private ApplicationContext	springContext			= null;
	
	boolean						isConnected				= false;
	
	Map<String, Long>			counter					= new HashMap<String, Long>(10);
	
	Timer						timer;
	JButton						stopButton;
	JButton						startButton;
	ButtonGroup					buttonGroup;
	JRadioButton				heapsButton;
	JRadioButton				quickButton;
	JRadioButton				mergeButton;
	
	public YacaAgentTestClient() {
	}
	
	public static void main(final String[] args) throws IOException {
		LOGGER.info("(c) 2012-2016 by Markus Sprunck, v1.1");
		
		final YacaAgentTestClient yacaAgentTest = new YacaAgentTestClient();
		yacaAgentTest.run();
	}
	
	private int[] createSortInput() {
		final int[] result = new int[INPUT_ARRAY_SIZE];
		for (int i = 0; i < INPUT_ARRAY_SIZE; i++) {
			result[i] = (int) Math.round(100.0f * Math.random());
		}
		return result;
	}
	
	private void run() throws IOException {
		
		// show process id in the title bar
		final String id = ManagementFactory.getRuntimeMXBean().getName().split("@")[0];
		final JFrame frame = new JFrame("PID = " + id + " YacaAgentTest");
		
		buildContent(frame);
		frame.setMinimumSize(new Dimension(300, 180));
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.pack();
		frame.setLocation(400, 400);
		frame.setVisible(true);
		
		// working is started with this timer
		timer = new Timer(500, new TimerActionListener());
		
		if (springContext == null) {
			springContext = new FileSystemXmlApplicationContext("classpath*:com/sw_engineering_candies/yaca/*config.xml");
			sortBean = (ISort) springContext.getBean(BEAN_NAME_HEAP_SORT);
		}
		timer.start();
		isConnected = true;
		updateButtons();
	}
	
	private void buildContent(final JFrame aFrame) {
		final Box panelRadioButtons = Box.createVerticalBox();
		
		final JPanel panelAll = new JPanel(new BorderLayout());
		buttonGroup = new ButtonGroup();
		
		heapsButton = new JRadioButton("Heap Sort", true);
		heapsButton.setActionCommand(BEAN_NAME_HEAP_SORT);
		counter.put("HeapSort", 0L);
		final UIActionListener uiActionListener = new UIActionListener();
		heapsButton.addActionListener(uiActionListener);
		buttonGroup.add(heapsButton);
		panelRadioButtons.add(heapsButton);
		
		quickButton = new JRadioButton("Quick Sort");
		quickButton.setActionCommand(BEAN_NAME_QUICK_SORT);
		counter.put("QuickSort", 0L);
		quickButton.addActionListener(uiActionListener);
		buttonGroup.add(quickButton);
		panelRadioButtons.add(quickButton);
		
		mergeButton = new JRadioButton("Merge Sort");
		mergeButton.setActionCommand(BEAN_NAME_MERGE_SORT);
		counter.put("MergeSort", 0L);
		mergeButton.addActionListener(uiActionListener);
		buttonGroup.add(mergeButton);
		panelRadioButtons.add(mergeButton);
		panelAll.add(panelRadioButtons, BorderLayout.CENTER);
		
		final JPanel panelButtons = new JPanel(new FlowLayout());
		startButton = new JButton("Start");
		startButton.addActionListener(uiActionListener);
		panelButtons.add(startButton);
		
		stopButton = new JButton("Stop");
		stopButton.addActionListener(uiActionListener);
		stopButton.setEnabled(false);
		panelButtons.add(stopButton);
		panelAll.add(panelButtons, BorderLayout.SOUTH);
		
		aFrame.getContentPane().add(panelAll);
		
	}
	
	private void updateButtons() {
		stopButton.setEnabled(isConnected);
		startButton.setEnabled(!isConnected);
	}
	
	private final class TimerActionListener implements ActionListener {
		@Override
		public void actionPerformed(final ActionEvent e) {
			callSortBean();
		}
	}
	
	private void callSortBean() {
		final int[] sortInput = createSortInput();
		if (null != springContext && null != sortBean) {
			sortBean.sort(sortInput);
			incrementCounter();
		}
	}
	
	private void incrementCounter() {
		final String className = sortBean.getClass().getSimpleName();
		if (counter.containsKey(className)) {
			counter.put(className, counter.get(className) + 1L);
		}
		updateLabels();
	}
	
	private void updateLabels() {
		heapsButton.setText("Heap Sort  =" + counter.get("HeapSort"));
		quickButton.setText("Quick Sort =" + counter.get("QuickSort"));
		mergeButton.setText("Merge Sort =" + counter.get("MergeSort"));
	}
	
	private final class UIActionListener implements ActionListener {
		@Override
		public void actionPerformed(final ActionEvent e) {
			if ("javax.swing.JRadioButton".equals(e.getSource().getClass().getName())) {
				sortBean = (ISort) springContext.getBean(e.getActionCommand());
			}
			if ("javax.swing.JButton".equals(e.getSource().getClass().getName())) {
				if ("Start".equals(e.getActionCommand())) {
					timer.start();
					isConnected = true;
				} else if ("Stop".equals(e.getActionCommand())) {
					timer.stop();
					isConnected = false;
				}
				updateButtons();
			}
		}
	}
	
}