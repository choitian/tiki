package tiki.corePlugin;

import tiki.runtime.BaseComponent;
import tiki.runtime.Context;
import tiki.runtime.IMethod;

public class TString extends BaseComponent {
	private class equals implements IMethod {
		@Override
		public void invoke(Context con) {
			String self = con.getCurrentObject();
			String anString = con.getArgument(0);
			con.retv(String.valueOf(self.equals(anString)));
		}

		@Override
		public String signature() {
			return "boolean %s(String anString)";
		}
	}

	private class equalsIgnoreCase implements IMethod {
		@Override
		public void invoke(Context con) {
			String self = con.getCurrentObject();
			String anotherString = con.getArgument(0);
			con.retv(String.valueOf(self.equalsIgnoreCase(anotherString)));
		}

		@Override
		public String signature() {
			return "boolean %s(String anotherString)";
		}
	}

	private class indexOf implements IMethod {
		@Override
		public void invoke(Context con) {
			String self = con.getCurrentObject();
			String str = con.getArgument(0);
			int fromIndex = con.getArgumentInteger(1);

			con.retv(String.valueOf(self.indexOf(str, fromIndex)));
		}

		@Override
		public String signature() {
			return "int %s(String  str ,int fromIndex)";
		}
	}

	private class isEmpty implements IMethod {
		@Override
		public void invoke(Context con) {
			String self = con.getCurrentObject();
			con.retv(String.valueOf(self.isEmpty()));
		}

		@Override
		public String signature() {
			return "boolean %s()";
		}
	}

	private class length implements IMethod {
		@Override
		public void invoke(Context con) {
			String self = con.getCurrentObject();
			con.retv(String.valueOf(self.length()));
		}

		@Override
		public String signature() {
			return "int %s()";
		}
	}

	private class substring implements IMethod {
		@Override
		public void invoke(Context con) {
			String self = con.getCurrentObject();
			int beginIndex = con.getArgumentInteger(0);
			int endIndex = con.getArgumentInteger(1);

			con.retv(self.substring(beginIndex, endIndex));
		}

		@Override
		public String signature() {
			return "String %s(int beginIndex,int endIndex)";
		}
	}

	private class toInt implements IMethod {
		@Override
		public void invoke(Context con) {
			String self = con.getCurrentObject();
			int defaultValue = con.getArgumentInteger(0);

			con.retv(String.valueOf(StrtoInt(self, defaultValue)));
		}

		@Override
		public String signature() {
			return "int %s(int defaultValue)";
		}

		private int StrtoInt(String str, int defaultValue) {
			try {
				int result = Integer.parseInt(str);
				return result;
			} catch (NumberFormatException e) {
				return defaultValue;
			}
		}
	}

	private class valueOf implements IMethod {
		@Override
		public void invoke(Context con) {
			String value = con.getArgument(0);
			con.retv(value);
		}

		@Override
		public String signature() {
			return "static String %s(Object value)";
		}
	}

	public TString() {
		registerMethod(new isEmpty(), "isEmpty");
		registerMethod(new length(), "length");

		registerMethod(new equals(), "equals");
		registerMethod(new equalsIgnoreCase(), "equalsIgnoreCase");

		registerMethod(new toInt(), "toInt");
		registerMethod(new substring(), "substring");

		registerMethod(new indexOf(), "indexOf");

		registerMethod(new valueOf(), "valueOf");
	}
}
