package tiki.corePlugin;

import java.text.SimpleDateFormat;
import java.util.Date;

import tiki.runtime.BaseComponent;
import tiki.runtime.Context;
import tiki.runtime.IMethod;

public class TDate extends BaseComponent {
	private class now implements IMethod {
		@Override
		public void invoke(Context con) {
			String format = con.getArgument(0);

			con.retv(new SimpleDateFormat(format).format(new Date()));
		}

		@Override
		public String signature() {
			return "static String %s(String format)";
		}
	}

	public TDate() {
		registerMethod(new now(), "now");
	}
}
