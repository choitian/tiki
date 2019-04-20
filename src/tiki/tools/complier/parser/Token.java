package tiki.tools.complier.parser;

public class Token {
	public String extra;
	public String position;
	public String snippet;
	public String type;

	public Token() {
		type = null;
		extra = null;
		position = null;
		snippet = null;
	}
}
