package tiki.runtime;

public interface IRuntime {
	int ADD__ap_up = -3;
	int ADD__bp_up = -4;
	int ADD__is_VARARGS = -6;
	int ADD__next_ins = -1;
	int ADD__retv = -5;
	int ADD__sp_up = -2;

	int getParameterAddress(int index);

	int getAp();

	String read(int memAddress);

	boolean readBoolean(int memAddress);

	float readFloat(int memAddress);

	int readInteger(int memAddress);

	void write(int memAddress, String value);
}
