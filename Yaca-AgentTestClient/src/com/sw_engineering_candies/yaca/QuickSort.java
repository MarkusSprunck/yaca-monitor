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
