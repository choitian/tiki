package tiki.corePlugin;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import tiki.runtime.BaseComponent;
import tiki.runtime.Context;
import tiki.runtime.IMethod;

public class TSystem extends BaseComponent {
	private class print implements IMethod {
		@Override
		public void invoke(Context con) {
			String format = con.getArgument(0);
			String[] argList = con.getVarargsArguments();

			List<Object> args = new ArrayList<Object>();
			Pattern pattern;
			String regx = "(?<specifier>%(?<flags>[-#+ 0,(])?(?<width>[\\d]+)?(.(?<precision>[\\d]+))?(?<conversion>[dfbsx]))";
			pattern = Pattern.compile(regx);
			Matcher matcher = pattern.matcher(format);			
			int index = 0;
			while (matcher.find() && index < argList.length) {
				String conversion = matcher.group("conversion");

				String arg = argList[index++];
				Object argValue;
				switch (conversion) {
				case "d":
					argValue = toInt(arg);
					break;
				case "f":
					argValue = toFloat(arg);
					break;
				case "b":
					argValue = toBoolean(arg);
					break;
				case "s":
					argValue = String.valueOf(arg);
					break;
				case "x":
					argValue = toInt(arg);
					break;
				default:
					argValue = arg;
				}
				args.add(argValue);
			}
			System.out.print(String.format(format, args.toArray()));
		}

		@Override
		public String signature() {
			return "static void %s(String format,Object... arg_list)";
		}

		private boolean toBoolean(String value) {
			if (value == null)
				return false;

			return Boolean.parseBoolean(value);
		}

		private float toFloat(String value) {
			if (value == null)
				return 0;

			return Float.parseFloat(value);
		}

		private int toInt(String value) {
			if (value == null)
				return 0;

			return Integer.parseInt(value);
		}
	}

	public TSystem() {
		registerMethod(new print(), "print");
	}
}
