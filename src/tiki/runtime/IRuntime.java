package tiki.runtime;

public interface IRuntime {
	int ADD__next_ins = 0;
	int ADD__retv = -1;
	int ADD__is_VARARGS = -2;

	int getParameterAddress(int index);

	int getAp();

	String read(int memAddress);

	boolean readBoolean(int memAddress);

	float readFloat(int memAddress);

	int readInteger(int memAddress);

	void write(int memAddress, String value);
}
