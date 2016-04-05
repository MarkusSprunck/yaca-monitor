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

public class QuickSort implements ISort {

	private int[] numbers;

	private int size;

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.sprunck.yaca.tests.ISort#sort(java.lang.Integer[])
	 */
	@Override
	public void sort(final int[] values) {
		numbers = values;
		size = values.length;
		quicksort(0, size - 1);
	}

	private void quicksort(final int left, final int right) {

		if (left < right) {

			final int pivot_index = left + ((right - left) / 2);
			final int pivot_value = numbers[pivot_index];

			int l = left;
			int r = right;

			while (l < r) {
				while (numbers[l] < pivot_value) {
					l++;
				}
				while (numbers[r] > pivot_value) {
					r--;
				}

				if (l <= r) {
					exchange(l, r);
					l++;
					r--;
				}
			}

			quicksort(left, r); // Sort the left side of the array
			quicksort(l, right); // Sort the right side of the array
		}
	}

	private void exchange(final int i, final int j) {
		final int temp = numbers[i];
		numbers[i] = numbers[j];
		numbers[j] = temp;
	}
}
