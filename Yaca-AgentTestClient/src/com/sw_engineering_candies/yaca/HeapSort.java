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

import java.util.ArrayList;

public class HeapSort implements ISort {

	private final ArrayList<Integer> heap = new ArrayList<Integer>();

	private int length = 0;

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.sprunck.yaca.tests.ISort#sort(java.lang.Integer[])
	 */
	@Override
	public void sort(final int[] unsorted) {

		final Integer[] sorted = new Integer[unsorted.length];

		// Insert each number in the list into the heap
		for (final int element : unsorted) {
			insert(element);
		}

		// Remove the smallest value from the heap until the heap is empty
		for (int i = 0; i < unsorted.length; i++) {
			sorted[i] = pop();
		}
	}

	private int getLeftChildIndex(final int parent) {
		return (2 * parent);
	}

	private int getRightChildIndex(final int parent) {
		return ((2 * parent) + 1);
	}

	private int getParentIndex(final int node) {
		return (node / 2);
	}

	private Integer getValueAtIndex(final int index) {
		return heap.get(index - 1);
	}

	private void swap(int index_a, int index_b) {
		// Convert indecies to an n-1 scheme
		index_a--;
		index_b--;

		final Integer tmp = heap.get(index_a);
		heap.set(index_a, heap.get(index_b));
		heap.set(index_b, tmp);
	}

	private void insert(final Integer value) {
		// Appends the specified element to the end of this list.
		heap.add(value);

		// Set current to length + 1
		int current = ++length;

		// Get index of the parent element
		int parent = getParentIndex(current);
		while ((current > 1) && (getValueAtIndex(current) <= getValueAtIndex(parent))) {
			// While the current index is not the head, and the parent is
			// greater than the child
			swap(current, parent);
			current = parent;
			parent = getParentIndex(current);
		}
	}

	private Integer pop() {
		// Pops the head of the heap, and then moves the last value added to
		// the top of the heap. Finally, that value is percolated downwards
		// accordingly

		// Value to be returned
		final Integer ret = getValueAtIndex(1);

		// Index to begin percolating
		int current = 1;

		// Move last value added to the head
		heap.set(0, getValueAtIndex(length));

		// Remove the last value added
		heap.remove(--length);

		// While a left child exists from the current
		while (getLeftChildIndex(current) <= length) {
			final int left = getLeftChildIndex(current);
			final int right = getRightChildIndex(current);
			int move = left; // Index where the percolating number will move
								// next

			// If the left child is larger than the right child
			if ((right <= length) && (getValueAtIndex(left) > getValueAtIndex(right))) {
				move = right; // Set the next move to the right child
			}

			// If the parent is greater than the lesser of the two children
			if (getValueAtIndex(current) >= getValueAtIndex(move)) {
				swap(current, move); // Swap the parent with the lesser of the
										// two children
			}
			current = move; // Set current index to the position moved to
		}
		return ret; // Return top of the heap
	}

}
