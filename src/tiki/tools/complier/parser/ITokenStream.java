package tiki.tools.complier.parser;

import java.io.IOException;

public interface ITokenStream {
	public void ini(String content);

	public Token NextToken() throws IOException;
}
