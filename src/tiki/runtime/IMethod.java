package tiki.runtime;

public interface IMethod {
	void invoke(Context con);

	String signature();
}
