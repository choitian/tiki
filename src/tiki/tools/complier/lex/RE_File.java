package tiki.tools.complier.lex;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import tiki.tools.complier.lalr.LALR;
import tiki.tools.complier.parser.ITokenStream;
import tiki.tools.complier.parser.Parser;
import tiki.tools.complier.parser.Token;
import tiki.uitls.IOUtils;

public class RE_File {
	BufferedReader br_;
	
	public RE_File() {
		br_ = null;
	}

	Parser getParser() {
		InputStream re_grammar = this.getClass().getResourceAsStream("/re_grammar.txt");
		LALR lalr = new LALR();
		try {
			lalr.build(re_grammar);
		} catch (Exception e) {
			e.printStackTrace();
		}
		String result = IOUtils.toString(lalr.toXML());
		Parser parser = new Parser(new ByteArrayInputStream(result.getBytes()), new RETokenStream());
		return parser;
	}
	SyntaxTree LinkEnd(SyntaxTree node, String script) {
		SyntaxTree end = new SyntaxTree(SyntaxTree.END);
		end.priority = SyntaxTree.PRIORITY++;
		end.extra = script;

		SyntaxTree cat = new SyntaxTree(SyntaxTree.CAT);
		cat.ch0 = node;
		cat.ch1 = end;

		return cat;
	}

	String[] NextItem() throws Exception {
		String item[] = new String[2];

		try {
			String regx = "^(?<text>.+)\\s+(?<script>\\{.+\\})";
			Pattern pattern = Pattern.compile(regx);

			String line = null;
			while (true) {
				line = br_.readLine();

				if (line == null)
					return null;

				line = line.trim();
				if (!line.isEmpty() && !line.startsWith("//")) {
					Matcher matcher = pattern.matcher(line);
					if (matcher.find()) {
						String text = matcher.group("text");
						String script = matcher.group("script");

						item[0] = text;
						item[1] = script;

						break;
					} else {
						throw new Exception("Lexical Regular Expression File: bad (abnormal) formation.");
					}

				}
			}

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return item;
	}

	public SyntaxTree ToSyntaxTree(InputStream lex_re) throws Exception {
		br_ = new BufferedReader(new InputStreamReader(lex_re));
		Parser parser = getParser();

		String[] item = NextItem();

		SyntaxTree tree = null;
		while (item != null) {

			SyntaxTree node = LinkEnd(SyntaxTreeBuilder.Build(parser.Parse(item[0])), item[1]);
			if (tree == null) {
				tree = node;
			} else {
				SyntaxTree or = new SyntaxTree(SyntaxTree.OR);
				or.ch0 = tree;
				or.ch1 = node;
				tree = or;
			}
			item = NextItem();
		}
		return tree;
	}
}

class RETokenStream implements ITokenStream {
	public static final String ARRAY = "ARRAY";
	public static final String CHAR = "CHAR";

	public static final String DOT = "DOT";
	public static final String END = "__END__";
	public static final String ERR = "__ERR__";
	public static final String LITERAL = "LITERAL";
	public static final String LPAREN = "LPAREN";
	public static final String OR = "OR";
	public static final String PLUS = "PLUS";
	public static final String QUESTION = "QUESTION";
	public static final String RPAREN = "RPAREN";
	public static final String STAR = "STAR";

	private String content_;
	private int cur_;

	public RETokenStream() {
		content_ = null;
		cur_ = 0;
	}

	char Char() {
		return content_.charAt(cur_);
	}

	String GetUntil(char ch) {
		int begin_index = cur_;
		NextChar();
		while (true) {
			if (OutOfChar()) {
				return null;
			}

			if (Char() == ch) {
				NextChar();
				return content_.substring(begin_index, cur_);
			}
			NextChar();
		}
	}

	@Override
	public void ini(String content) {
		content_ = content;
		cur_ = 0;
	}

	Token NewToken(String type, String extra) {
		Token tok = new Token();
		tok.type = type;
		tok.extra = extra;

		return tok;
	}

	void NextChar() {
		cur_++;
	}

	@Override
	public Token NextToken() {
		SkipWhiteSpace();

		if (OutOfChar())
			return NewToken(RETokenStream.END, "");

		switch (Char()) {
		case '|':
			NextChar();
			return NewToken(RETokenStream.OR, "");

		case '*':
			NextChar();
			return NewToken(RETokenStream.STAR, "");

		case '+':
			NextChar();
			return NewToken(RETokenStream.PLUS, "");

		case '?':
			NextChar();
			return NewToken(RETokenStream.QUESTION, "");

		case '.':
			NextChar();
			return NewToken(RETokenStream.DOT, "");

		case '(':
			NextChar();
			return NewToken(RETokenStream.LPAREN, "");

		case ')':
			NextChar();
			return NewToken(RETokenStream.RPAREN, "");

		default: {
			if (Char() == '\"') {
				String content = GetUntil('\"');
				if (content == null) {
					return NewToken(RETokenStream.ERR, "");
				} else {
					return NewToken(RETokenStream.LITERAL, content);
				}
			} else if (Char() == '[') {
				String content = GetUntil(']');
				if (content == null) {
					return NewToken(RETokenStream.ERR, "");
				} else {
					return NewToken(RETokenStream.ARRAY, content);
				}
			} else if (Char() == '\\') {
				int begin_index = cur_;
				NextChar();
				NextChar();

				return NewToken(RETokenStream.CHAR, content_.substring(begin_index, cur_));
			} else {
				int begin_index = cur_;
				NextChar();

				return NewToken(RETokenStream.CHAR, content_.substring(begin_index, cur_));
			}
		}
		}
	}

	boolean OutOfChar() {
		return content_ == null || cur_ >= content_.length();
	}

	void SkipWhiteSpace() {
		while (!OutOfChar()) {
			if (Character.isWhitespace(Char())) {
				NextChar();
			} else {
				break;
			}
		}
	}
}
