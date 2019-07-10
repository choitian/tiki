package tiki.runtime;

public class Context {
	private IRuntime rt;

	public Context(IRuntime MEM) {
		this.rt = MEM;
	}

	public String getArgumentArrayElement(int arrayIndex, int elementIndex) {
		int a_base = getArgumentInteger(arrayIndex);

		return rt.read(a_base + elementIndex);
	}

	public boolean getArgumentBoolean(int index) {
		return rt.readBoolean(rt.getParameterAddress(index));
	}

	public int getArgumentInteger(int index) {
		return rt.readInteger(rt.getParameterAddress(index));
	}

	public String getArgument(int index) {
		return rt.read(rt.getParameterAddress(index));
	}

	public String getCurrentObject() {
		return rt.read(rt.getParameterAddress(-1));
	}

	public String[] getVarargsArguments() {

		boolean isVARARGS = rt.readBoolean(rt.getAp() + IRuntime.ADD__is_VARARGS);
		if (!isVARARGS) {
			return null;
		}
		int base_value = rt.getAp();
		int vararg_length = rt.readInteger(base_value+1);

		String[] varargs = new String[vararg_length];
		for (int i = 0; i < vararg_length; i++) {
			varargs[i] = rt.read(base_value + 2 + i);
		}
		return varargs;
	}

	public void retv(String value) {
		int retvAdd = rt.readInteger(rt.getAp() + IRuntime.ADD__retv);
		if (retvAdd != -1)
			rt.write(retvAdd, value);
	}

	public void setArgumentArrayElement(int arrayIndex, int elementIndex, String value) {
		int a_base = getArgumentInteger(arrayIndex);

		rt.write(a_base + elementIndex, value);
	}
}
