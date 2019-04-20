package tiki.tools.complier.parser;

import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedHashMap;
import java.util.Stack;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

class DFAState {
	boolean acceptable;

	String acceptance;

	int id;
	LinkedHashMap<Character, Integer> transitionMap;

	DFAState(int id, boolean acceptable, String acceptance) {
		this.id = id;
		this.acceptable = acceptable;
		this.acceptance = acceptance;

		transitionMap = new LinkedHashMap<Character, Integer>();
	}

	Integer TransitionOf(char on) {
		return transitionMap.get(on);
	}

}

public class LexicalAnalyzer implements ITokenStream {
	public static final String END = "__END__";
	public static final String ERR = "__ERR__";

	private StringBuffer content_;

	private int cur_;

	private int debugSourceSnippetLineRange;

	LinkedHashMap<Integer, DFAState> dfaStateMap;
	private int lno_;
	private String location_;
	private Pattern scriptPattern;

	public LexicalAnalyzer(InputStream dfa, int debugSourceSnippetLineRange) throws Exception {
		this.debugSourceSnippetLineRange = debugSourceSnippetLineRange;

		this.content_ = null;
		this.cur_ = 0;
		this.lno_ = 1;

		String regx = "\\{\\s*\\\"(?<token>\\w+)\\\"\\s*\\}";
		this.scriptPattern = Pattern.compile(regx);
		this.dfaStateMap = new LinkedHashMap<Integer, DFAState>();
		setupDFAFromXML(dfa);
	}

	@Override
	public void ini(String content) {
		content_ = new StringBuffer(content);
		cur_ = 0;
		lno_ = 1;
	}

	void NewDFAState(Element state) throws Exception {
		int id = Integer.parseInt(state.getAttribute("id"), 16);
		boolean acceptable = state.getAttribute("acceptable").equalsIgnoreCase("true");
		String acceptance = state.getAttribute("acceptance");

		DFAState ds = new DFAState(id, acceptable, acceptance);

		dfaStateMap.put(ds.id, ds);

		NodeList gotos = state.getElementsByTagName("goto");

		for (int index = 0; index < gotos.getLength(); index++) {
			Element gotoElement = (Element) gotos.item(index);
			Character on = (char) Integer.parseInt(gotoElement.getAttribute("on"), 16);
			Integer state_id = Integer.parseInt(gotoElement.getAttribute("to"), 16);

			ds.transitionMap.put(on, state_id);
		}
	}

	Token NewToken(String type, String text) {
		Token tok = new Token();
		tok.type = type;
		tok.extra = text;
		if (location_ == null || location_.isEmpty()) {
			tok.position = String.format("[line:%d]", lno_);
		} else {
			tok.position = String.format("[%s: %d]", location_, lno_);
		}
		if (debugSourceSnippetLineRange != 0) {
			int start = preLineStart(debugSourceSnippetLineRange);
			int end = nextLineEnd(debugSourceSnippetLineRange);

			tok.snippet = content_.substring(start, end);
		}

		return tok;
	}

	int nextLineEnd(int numberOfLine) {
		int index = cur_;
		while (true) {
			if (index >= content_.length())
				break;

			char ch = content_.charAt(index);
			if (ch == '\n') {
				numberOfLine--;

				if (numberOfLine == 0) {
					return index;
				}
			}
			index++;
		}

		return content_.length();
	}

	@Override
	public Token NextToken() throws IOException {
		// Token tok = NextToken_XML();
		Token tok = NextToken_DFAState();
		if (tok.type.equals("NEWLINE")) {
			this.lno_++;
			return NextToken();
		} else if (tok.type.equals("COMMENT") || tok.type.equals("MULTILINE_COMMENT")) {
			String comment = tok.extra;
			for (char ch : comment.toCharArray()) {
				if (ch == '\n')
					this.lno_++;
			}
			return NextToken();
		} else if (tok.type.equals("SKIP")) {
			return NextToken();
		} else if (tok.type.equals(LexicalAnalyzer.ERR)) {
			Logger.getGlobal().info(String.format("L:%d   unexpect Token. Skip", lno_));
			return NextToken();
		}

		return tok;
	}

	public Token NextToken_DFAState() {
		if (cur_ >= content_.length()) {
			return NewToken(LexicalAnalyzer.END, "");
		}

		Stack<DFAState> track = new Stack<DFAState>();

		DFAState top = this.dfaStateMap.get(0);

		char lookahead;
		int start = cur_;
		while (true) {
			if (cur_ >= content_.length())
				break;

			lookahead = content_.charAt(cur_);

			DFAState next = TransitionOf(top, lookahead);

			if (next == null)
				break;
			else {
				top = next;
				track.push(top);
				cur_++;
			}
			// MULTILINE_COMMENT,MIN length rule,not MAX length rule
			if (top.acceptable && ScriptToToken(top.acceptance).equals("MULTILINE_COMMENT"))
				break;

		}
		while (!track.isEmpty()) {
			top = track.pop();
			if (top.acceptable) {
				String token_type = ScriptToToken(top.acceptance);
				if (token_type == null) {
					return NewToken(LexicalAnalyzer.ERR, content_.substring(start, cur_));
				} else {
					return NewToken(token_type, content_.substring(start, cur_));
				}
			}
			cur_--;
		}
		cur_++;
		return NewToken(LexicalAnalyzer.ERR, content_.substring(start, cur_));
	}

	int preLineStart(int numberOfLine) {
		// skip current char.
		int index = cur_ - 1;
		while (true) {
			if (index <= 0)
				break;

			char ch = content_.charAt(index);
			if (ch == '\n') {
				numberOfLine--;

				if (numberOfLine == 0) {
					return index;
				}
			}
			index--;
		}

		return 0;
	}

	String ScriptToToken(String script) {
		Matcher matcher = scriptPattern.matcher(script);
		if (matcher.find()) {
			return matcher.group("token");
		}
		return null;
	}

	void setupDFAFromXML(InputStream dfa) throws Exception {
		DocumentBuilderFactory doc_builder_factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder doc_builder;
		doc_builder = doc_builder_factory.newDocumentBuilder();
		Document doc = doc_builder.parse(dfa);

		Element root = doc.getDocumentElement();
		NodeList states = root.getElementsByTagName("state");

		for (int index = 0; index < states.getLength(); index++) {
			NewDFAState((Element) states.item(index));
		}
	}

	DFAState TransitionOf(DFAState state, char on) {
		Integer next_state_id = state.TransitionOf(on);
		return this.dfaStateMap.get(next_state_id);
	}
}
