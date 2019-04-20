package tiki.corePlugin;

import tiki.runtime.BaseComponent;
import tiki.runtime.Context;
import tiki.runtime.IMethod;

public class TMath extends BaseComponent {
	private class compare implements IMethod {
		private static final int great = 2;
		private static final int greatEqual = 3;
		private static final int less = 0;
		private static final int lessEqual = 1;

		private int operator;

		compare(int operator) {
			this.operator = operator;
		}

		@Override
		public void invoke(Context con) {
			int value0 = con.getArgumentInteger(0);
			int value1 = con.getArgumentInteger(1);

			switch (operator) {
			case less:
				con.retv(String.valueOf(value0 < value1));
				break;
			case lessEqual:
				con.retv(String.valueOf(value0 <= value1));
				break;
			case great:
				con.retv(String.valueOf(value0 > value1));
				break;
			case greatEqual:
				con.retv(String.valueOf(value0 >= value1));
				break;
			}

		}

		@Override
		public String signature() {
			return "static boolean %s(int value0, int value1)";
		}
	}

	private class qsort implements IMethod {
		@Override
		public void invoke(Context con) {
			int low = con.getArgumentInteger(1);
			int high = con.getArgumentInteger(2);

			int[] a = new int[high + 1];

			// clone it
			for (int index = 0; index < a.length; index++) {
				a[index] = Integer.parseInt(con.getArgumentArrayElement(0, index));
			}

			JQsort(a, low, high);

			// copy back
			for (int index = 0; index < a.length; index++) {
				con.setArgumentArrayElement(0, index, String.valueOf(a[index]));
			}
		}

		void JQsort(int a[], int low, int high) {
			if (low >= high) {
				return;
			}
			int first = low;
			int last = high;
			int key = a[first];

			while (first < last) {
				while (first < last && a[last] >= key) {
					last--;
				}

				a[first] = a[last];

				while (first < last && a[first] <= key) {
					first++;
				}

				a[last] = a[first];
			}
			a[first] = key;
			JQsort(a, low, first - 1);
			JQsort(a, first + 1, high);
		}

		@Override
		public String signature() {
			return "static void %s(int []a, int low, int high)";
		}
	}

	private class random implements IMethod {
		@Override
		public void invoke(Context con) {
			con.retv(String.valueOf(randomWithRange(con.getArgumentInteger(0), con.getArgumentInteger(1))));
		}

		int randomWithRange(int min, int max) {
			int range = Math.abs(max - min) + 1;
			return (int) (Math.random() * range) + (min <= max ? min : max);
		}

		@Override
		public String signature() {
			return "static int %s(int min, int max)";
		}
	}

	private class relational implements IMethod {
		static final int and = 0;
		static final int or = 1;

		private int operator;

		relational(int operator) {
			this.operator = operator;
		}

		@Override
		public void invoke(Context con) {
			boolean value0 = con.getArgumentBoolean(0);
			boolean value1 = con.getArgumentBoolean(1);

			switch (operator) {
			case and:
				con.retv(String.valueOf(value0 && value1));
				break;
			case or:
				con.retv(String.valueOf(value0 || value1));
				break;
			}

		}

		@Override
		public String signature() {
			return "static boolean %s(boolean value0, boolean value1)";
		}
	}

	public TMath() {
		registerMethod(new random(), "random");
		registerMethod(new compare(compare.less), "less");
		registerMethod(new compare(compare.lessEqual), "lessEqual");
		registerMethod(new compare(compare.great), "great");
		registerMethod(new compare(compare.greatEqual), "greatEqual");
		registerMethod(new relational(relational.and), "and");
		registerMethod(new relational(relational.or), "or");
		registerMethod(new qsort(), "qsort");
	}
}
