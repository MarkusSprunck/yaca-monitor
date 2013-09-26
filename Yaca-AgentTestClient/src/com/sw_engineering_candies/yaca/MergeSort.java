package com.sw_engineering_candies.yaca;


public class MergeSort implements ISort {

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
		mergesort(0, size - 1);
	}

	private void mergesort(final int left, final int right) {
		// Check if low is smaller then high, if not then the array is sorted
		if (left < right) {
			// Get the index of the element which is in the middle
			final int middle = (left + right) / 2;
			// Sort the left side of the array
			mergesort(left, middle);
			// Sort the right side of the array
			mergesort(middle + 1, right);
			// Combine them both
			merge(left, middle, right);
		}
	}

	private void merge(final int low, final int middle, final int high) {

		// Helperarray
		final int[] helper = new int[size];

		// Copy both parts into the helper array
		for (int i = low; i <= high; i++) {
			helper[i] = numbers[i];
		}

		int i = low;
		int j = middle + 1;
		int k = low;
		// Copy the smallest values from either the left or the right side back
		// to the original array
		while ((i <= middle) && (j <= high)) {
			if (helper[i] <= helper[j]) {
				numbers[k] = helper[i];
				i++;
			} else {
				numbers[k] = helper[j];
				j++;
			}
			k++;
		}
		// Copy the rest of the left side of the array into the target array
		while (i <= middle) {
			numbers[k] = helper[i];
			k++;
			i++;
		}
	}

}
