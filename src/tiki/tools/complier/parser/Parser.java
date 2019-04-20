package tiki.tools.complier.parser;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Stack;
import java.util.logging.Logger;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class Parser {
	LinkedHashMap<Integer, Production> productionMap;

	LinkedHashMap<Integer, State> stateMap;
	LinkedHashMap<Integer, Symbol> symbolMap;
	LinkedHashMap<String, Symbol> symbolMapByText;
	ITokenStream tokenStream;

	public Parser(InputStream lalr, ITokenStream token_stream) {
		tokenStream = token_stream;

		symbolMap = new LinkedHashMap<Integer, Symbol>();
		symbolMapByText = new LinkedHashMap<String, Symbol>();
		productionMap = new LinkedHashMap<Integer, Production>();
		stateMap = new LinkedHashMap<Integer, State>();

		setupLALRFromXML(lalr);
	}

	void errorMsg(State state, Token lookahead) {
		Logger.getGlobal().info(String.format("error: STATE %d, LOOKAHEAD: %s: '%s', LOCATION: %s, SNIPPET:\n%s",
				state.id, lookahead.type, lookahead.extra, lookahead.position, lookahead.snippet));
	}

	Production getProduction(Integer production_id) {
		if (production_id == null)
			return null;
		return this.productionMap.get(production_id);
	}

	State getState(Integer state_id) {
		if (state_id == null)
			return null;
		return this.stateMap.get(state_id);
	}

	Symbol getSymbol(Integer symbol_id) {
		return this.symbolMap.get(symbol_id);
	}

	Symbol getSymbol__by_text(String symbol_text) {
		return this.symbolMapByText.get(symbol_text);
	}

	void newLALRState(Element element) throws Exception {
		int id = Integer.parseInt(element.getAttribute("id"));

		State s = new State(id);
		stateMap.put(s.id, s);

		setupLALRStateGotoTable(s, element.getElementsByTagName("goto"));

		setupLALRStateParsingTable(s, element.getElementsByTagName("action"));
	}

	Token NextToken() throws IOException {
		return tokenStream.NextToken();
	}

	void OnAccept() {

	}

	void OnReduce(Stack<ParseTree> pt_nodes, String head_text, int plen, String script) {

		ParseTree node = new ParseTree(ParseTree.INTERIOR, pt_nodes.peek().position);
		node.name = head_text;
		while (plen-- > 0) {
			node.AddNode(pt_nodes.pop());
		}
		node.script = script;
		pt_nodes.push(node);

	}

	void OnShift(Stack<ParseTree> pt_nodes, Token tok) {
		ParseTree node = new ParseTree(ParseTree.LEAF, tok.position);
		node.name = tok.type;
		node.extra = tok.extra;

		pt_nodes.push(node);
	}

	public ParseTree Parse(String content) throws Exception {
		tokenStream.ini(content);
		Token lookahead = NextToken();
		Stack<State> track = new Stack<State>();
		Stack<ParseTree> pt_nodes = new Stack<ParseTree>();

		track.push(this.stateMap.get(0));
		while (true) {
			State state = track.peek();
			Symbol ls = getSymbol__by_text(lookahead.type);
			String action = state.ActionOf(ls);

			if (action == null || action.isEmpty()) {
				errorMsg(state, lookahead);
				break;
			}

			if (action.startsWith("s")) {
				OnShift(pt_nodes, lookahead);
				State target = getState(state.Goto(ls));
				if (target == null) {
					errorMsg(state, lookahead);
					break;
				}
				track.push(target);

				lookahead = NextToken();

			} else if (action.startsWith("r")) {

				String production_id = action.substring(1);

				Production production = getProduction(Integer.parseInt(production_id));
				if (production == null) {
					errorMsg(state, lookahead);
					break;
				}

				Symbol head = getSymbol(production.head_id);
				if (head == null) {
					errorMsg(state, lookahead);
					break;
				}
				String head_text = head.text;
				String script = production.script;
				int plen = production.len;

				if (pt_nodes.size() < plen) {
					errorMsg(state, lookahead);
					break;
				}

				OnReduce(pt_nodes, head_text, plen, script);

				if (track.size() <= plen) {
					errorMsg(state, lookahead);
					break;
				}
				track.setSize(track.size() - plen);

				// State target = Goto(track.peek(),head);
				State target = getState(track.peek().Goto(head));
				track.push(target);

			} else if (action.startsWith("a")) {
				if (pt_nodes.size() != 1) {
					errorMsg(state, lookahead);
					break;
				}
				OnAccept();

				return pt_nodes.pop();
			}
		}
		return null;
	}

	void setupLALRFromXML(InputStream lalr) {
		try {
			DocumentBuilderFactory doc_builder_factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder doc_builder;
			doc_builder = doc_builder_factory.newDocumentBuilder();
			Document doc = doc_builder.parse(lalr);

			Element root = doc.getDocumentElement();

			setupLALRSymbol(root.getElementsByTagName("symbol"));

			setupLALRProduction(root.getElementsByTagName("production"));

			setupLALRState(root.getElementsByTagName("state"));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	void setupLALRProduction(NodeList productions) throws Exception {
		for (int index = 0; index < productions.getLength(); index++) {
			Element production = (Element) productions.item(index);

			int id = Integer.parseInt(production.getAttribute("id"));
			int len = Integer.parseInt(production.getAttribute("len"));
			int head_id = Integer.parseInt(production.getAttribute("head"));

			String nodes = production.getAttribute("nodes");
			String script = production.getAttribute("script");

			Production p = new Production(id, len, head_id, script);
			productionMap.put(p.id, p);

			String[] parts = nodes.split("\\|");
			for (int index0 = 0; index0 < len; index0++) {
				p.nodes.add(Integer.parseInt(parts[index0]));
			}
		}
	}

	void setupLALRState(NodeList states) throws Exception {
		for (int index = 0; index < states.getLength(); index++) {
			newLALRState((Element) states.item(index));
		}
	}

	void setupLALRStateGotoTable(State s, NodeList gotos) throws Exception {
		for (int index = 0; index < gotos.getLength(); index++) {
			Element gotoElement = (Element) gotos.item(index);

			int on = Integer.parseInt(gotoElement.getAttribute("on"));
			int state = Integer.parseInt(gotoElement.getAttribute("state"));

			s.gotoTable.put(on, state);
		}
	}

	void setupLALRStateParsingTable(State s, NodeList actions) throws Exception {
		for (int index = 0; index < actions.getLength(); index++) {
			Element element = (Element) actions.item(index);

			int on = Integer.parseInt(element.getAttribute("on"));
			String doWhat = element.getAttribute("do");

			s.actionTable.put(on, doWhat);
		}
	}

	void setupLALRSymbol(NodeList symbols) throws Exception {
		for (int index = 0; index < symbols.getLength(); index++) {
			Element symbol = (Element) symbols.item(index);

			int id = Integer.parseInt(symbol.getAttribute("id"));
			String text = symbol.getAttribute("text");
			Symbol s = new Symbol(id, text);
			symbolMap.put(s.id, s);
			symbolMapByText.put(s.text, s);
		}
	}
}

class Production {
	int head_id;
	int id;
	int len;
	ArrayList<Integer> nodes;
	String script;

	Production(int id, int len, int head_id, String script) {
		this.id = id;
		this.len = len;

		this.head_id = head_id;
		this.script = script;

		nodes = new ArrayList<Integer>();
	}
}

class State {
	LinkedHashMap<Integer, String> actionTable;

	LinkedHashMap<Integer, Integer> gotoTable;

	int id;

	State(int id) {
		this.id = id;

		gotoTable = new LinkedHashMap<Integer, Integer>();
		actionTable = new LinkedHashMap<Integer, String>();
	}

	String ActionOf(Symbol symbol) {
		if (symbol == null)
			return null;
		return actionTable.get(symbol.id);
	}

	Integer Goto(Symbol symbol) {
		if (symbol == null)
			return null;
		return gotoTable.get(symbol.id);
	}
}

class Symbol {
	int id;
	String text;

	Symbol(int id, String text) {
		this.id = id;
		this.text = text;
	}
}
