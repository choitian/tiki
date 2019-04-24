package tiki.tools.complier.lalr;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map.Entry;
import java.util.TreeSet;

class BNF {
	public static final String END = "__END__";
	public static final String NULL = "__NULL__";
	public static final String START = "__START__";

	static public boolean isNullable(ArrayList<Symbol> symbol_list) {
		for (Symbol symbol : symbol_list) {
			if (!isNullable(symbol))
				return false;
		}
		return true;
	}

	static public boolean isNullable(Symbol symbol) {
		return symbol.nullable;
	}

	static public boolean isNULLProduction(Production production) {
		return production.nodes.size() == 1 && production.nodes.get(0).text == NULL;
	}

	public Production production_start_;
	private ArrayList<Production> productions;
	boolean something_changed_;

	Symbol symbol_cur_;
	public Symbol symbol_end_;

	public LinkedHashMap<String, Symbol> symbol_map_;

	public Symbol symbol_null_;

	public Symbol symbol_start_;

	TokenStream token_stream_;

	public BNF(InputStream file) {
		token_stream_ = new TokenStream(file);
		symbol_cur_ = null;

		symbol_start_ = null;
		symbol_end_ = null;
		symbol_null_ = null;
		production_start_ = null;

		symbol_map_ = new LinkedHashMap<String, Symbol>();
		productions = new ArrayList<Production>();

		something_changed_ = false;
	}

	void Build() throws Exception {
		symbol_start_ = TryMakeSymbol(START);
		symbol_end_ = TryMakeSymbol(END);
		symbol_null_ = TryMakeSymbol(NULL);
		production_start_ = MakeProduction(symbol_start_);

		token_stream_.NextToken();
		Symbol first_symbol = BuildSymbol();
		while (!token_stream_.token.equals("END")) {
			BuildSymbol();
		}

		production_start_.nodes.add(first_symbol);
	}

	Production BuildProduction(Symbol head) throws Exception {
		Production prod = MakeProduction(head);

		Expect("ID");
		while (token_stream_.token.equals("ID")) {
			prod.AddNode(TryMakeSymbol(token_stream_.extra));
			token_stream_.NextToken();
		}
		if (token_stream_.token.equals("SRCIPT")) {
			prod.script = token_stream_.extra;
			token_stream_.NextToken();
		}

		return prod;
	}

	Symbol BuildSymbol() throws Exception {
		Expect("ID");

		Symbol head = TryMakeSymbol(token_stream_.extra);

		token_stream_.NextToken();
		Expect("COLON");
		do {
			token_stream_.NextToken();
			BuildProduction(head);
		} while (token_stream_.token.equals("OR"));

		Expect("SEMICOLON");
		token_stream_.NextToken();

		return head;
	}

	void ComputeFST() {
		for (Entry<String, Symbol> entry : symbol_map_.entrySet()) {
			Symbol symbol = entry.getValue();

			if (symbol.isTerminal()) {
				symbol.first_set.add(symbol.text);
			}
		}

		something_changed_ = true;
		while (something_changed_) {
			something_changed_ = false;
			for (Entry<String, Symbol> entry : symbol_map_.entrySet()) {
				Symbol symbol = entry.getValue();

				if (!symbol.isTerminal()) {
					ComputeFSTNonTerminal(symbol);
				}
			}
		}
		/*
		for (Entry<String, Symbol> entry : symbol_map_.entrySet()) {
			Symbol symbol = entry.getValue();
			TreeSet<String> fst = symbol.first_set;
			String[] vs = fst.toArray(new String[fst.size()]);
			Arrays.sort(vs);
			String jd = String.join(" ", vs);
			System.out.print(String.format("%s [%s]\n", symbol.text,jd));
		}	
		*/	

	}

	void ComputeFSTNonTerminal(Symbol symbol_non_terminal) {
		int old_size = symbol_non_terminal.first_set.size();
		boolean old_nullable = symbol_non_terminal.nullable;

		for (Production production : symbol_non_terminal.productions) {
			ComputeFSTNonTerminalByProduction(production);
		}

		if (symbol_non_terminal.first_set.size() > old_size || old_nullable != symbol_non_terminal.nullable)
			something_changed_ = true;
	}

	void ComputeFSTNonTerminalByProduction(Production production) {
		Symbol head = production.head;
		if (isNULLProduction(production)) {
			head.nullable = true;
		} else {
			for (Iterator<Symbol> it = production.nodes.iterator();;) {
				Symbol node = it.next();

				head.first_set.addAll(node.first_set);
				if (!isNullable(node))
					break;

				if (!it.hasNext()) {
					head.nullable = true;
					break;
				}
			} // end while
		}
	}

	void Expect(String token) throws Exception {
		if (!token_stream_.token.equals(token)) {
			throw new Exception("LINE:" + token_stream_.line + " EXPECT: " + token + "\tGOT: " + token_stream_.token);
		}
	}

	/**
	 * @return the production_list_
	 */
	public ArrayList<Production> getProductions() {
		return productions;
	}

	Production MakeProduction(Symbol head) {
		Production p = new Production(productions.size());
		p.head = head;
		head.productions.add(p);

		productions.add(p);
		return p;
	}

	public void Setup() throws Exception {
		Build();
		ComputeFST();
	}

	Symbol TryMakeSymbol(String text) {
		if (symbol_map_.containsKey(text))
			return symbol_map_.get(text);

		symbol_cur_ = new Symbol(symbol_map_.size());
		symbol_cur_.text = text;

		symbol_map_.put(text, symbol_cur_);
		return symbol_cur_;
	}
}

class Production {
	public Symbol head;
	public int id;
	public ArrayList<Symbol> nodes;
	public String script;

	public Production(int id) {
		this.id = id;
		this.script = "";
		this.head = null;
		this.nodes = new ArrayList<Symbol>();
	}

	void AddNode(Symbol node) {
		if (node != null) {
			node.ref++;
			nodes.add(node);
		}
	}

	int NodeSize() {
		return nodes.size();
	}

	public String ToText() {
		StringBuilder sb = new StringBuilder();

		sb.append(head.text);
		sb.append(":");
		for (Symbol node : nodes) {
			sb.append(node.text);
			sb.append(" ");
		}
		return sb.toString();
	}
}

class Symbol {
	public TreeSet<String> first_set;
	public TreeSet<String> follow_set;
	public int id;
	public boolean nullable;

	public ArrayList<Production> productions;
	public int ref;
	public String text;

	public Symbol(int id) {
		this.id = id;
		this.ref = 0;
		this.text = "";
		this.productions = new ArrayList<Production>();

		this.nullable = false;
		this.first_set = new TreeSet<String>();
		this.follow_set = new TreeSet<String>();

	}

	public boolean isTerminal() {
		return productions.isEmpty();
	}

	public String ToText() {
		StringBuilder sb = new StringBuilder();

		for (Production production : productions) {
			sb.append(production.ToText());
			sb.append("|");
		}
		return sb.toString();
	}
}

class TokenStream {
	private String content_;
	private int cur_;

	String extra; // store extra info of the current token.
	int line; // token's line no.
	String token;

	TokenStream(InputStream content) {
		try {
			content_ = convertStreamToString(content);
			cur_ = 0;

			line = 0;
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	char Char() {
		return content_.charAt(cur_);
	}

	public String convertStreamToString(InputStream is) throws Exception {
		Writer writer = new StringWriter();

		char[] buffer = new char[1024];
		try {
			Reader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
			int n;
			while ((n = reader.read(buffer)) != -1) {
				writer.write(buffer, 0, n);
			}
		} finally {
			is.close();
		}
		return writer.toString();
	}// End method convertStreamToString

	String FileToString(String fileName) throws IOException {
		BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(new File(fileName)), "utf-8"));
		try {
			StringBuilder sb = new StringBuilder();
			String line = br.readLine();

			while (line != null) {
				sb.append(line);
				sb.append("\n");
				line = br.readLine();
			}
			return sb.toString();
		} finally {
			br.close();
		}
	}

	void NextChar() {
		cur_++;
	}

	public void NextToken() {
		SkipWhiteSpace();

		if (OutOfChar()) {
			this.token = "END";
			return;
		}

		switch (Char()) {
		case ':':
			NextChar();
			this.token = "COLON";
			break;
		case '|':
			NextChar();
			this.token = "OR";
			break;
		case ';':
			NextChar();
			this.token = "SEMICOLON";
			break;
		default: {
			if (Character.isLetter(Char()) || Char() == '_') {
				int begin_index = cur_;

				NextChar();
				while (!OutOfChar()) {
					if (!(Character.isLetter(Char()) || Character.isDigit(Char()) || Char() == '_'))
						break;
					NextChar();
				}
				this.extra = content_.substring(begin_index, cur_);
				this.token = "ID";
				return;
			} else if (Char() == '{') {
				int begin_index = cur_;
				NextChar();
				while (true) {
					if (OutOfChar()) {
						this.token = "ERR";
						return;
					}

					if (Char() == '\n')
						line++;

					if (Char() == '}') {
						NextChar();

						this.extra = content_.substring(begin_index, cur_);
						this.token = "SRCIPT";
						return;
					}
					NextChar();
				}

			} else {
				NextChar();
				this.token = "ERR";
				return;
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
				if (Char() == '\n')
					line++;

				NextChar();
			} else if ((cur_ + 1) < content_.length() && content_.charAt(cur_) == '/'
					&& content_.charAt(cur_ + 1) == '/') {
				while (!OutOfChar()) {
					if (Char() == '\n') {
						line++;
						NextChar();
						break;
					}
					NextChar();
				}
			} else {
				break;
			}
		}
	}
}