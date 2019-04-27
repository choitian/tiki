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
	ITokenStream tokenStream;

	public Parser(InputStream lalr, ITokenStream token_stream) {
		tokenStream = token_stream;


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
			String ls = lookahead.type;
			String action = state.ActionOf(ls);

			if (action == null || action.isEmpty()) {
				errorMsg(state, lookahead);
				break;
			}

			if (action.startsWith("shift")) {
				OnShift(pt_nodes, lookahead);
				State target = getState(state.Goto(ls));
				if (target == null) {
					errorMsg(state, lookahead);
					break;
				}
				track.push(target);

				lookahead = NextToken();

			} else if (action.startsWith("reduce")) {

				String production_id = action.substring(6);

				Production production = getProduction(Integer.parseInt(production_id));
				if (production == null) {
					errorMsg(state, lookahead);
					break;
				}

				String head = production.head;
				if (head == null) {
					errorMsg(state, lookahead);
					break;
				}
				String script = production.script;
				int plen = production.len;

				if (pt_nodes.size() < plen) {
					errorMsg(state, lookahead);
					break;
				}

				OnReduce(pt_nodes, head, plen, script);

				if (track.size() <= plen) {
					errorMsg(state, lookahead);
					break;
				}
				track.setSize(track.size() - plen);

				// State target = Goto(track.peek(),head);
				State target = getState(track.peek().Goto(head));
				track.push(target);

			} else if (action.startsWith("accept")) {
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
			String head = production.getAttribute("head");

			String nodes = production.getAttribute("nodes");
			String script = production.getAttribute("script");

			Production p = new Production(id, len, head, script);
			productionMap.put(p.id, p);

			String[] parts = nodes.split("\\|");
			for (int index0 = 0; index0 < len; index0++) {
				p.nodes.add(parts[index0]);
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

			String on = gotoElement.getAttribute("on");
			int state = Integer.parseInt(gotoElement.getAttribute("state"));

			s.gotoTable.put(on, state);
		}
	}

	void setupLALRStateParsingTable(State s, NodeList actions) throws Exception {
		for (int index = 0; index < actions.getLength(); index++) {
			Element element = (Element) actions.item(index);

			String on = element.getAttribute("on");
			String doWhat = element.getAttribute("do");

			s.actionTable.put(on, doWhat);
		}
	}
}

class Production {
	String head;
	int id;
	int len;
	ArrayList<String> nodes;
	String script;

	Production(int id, int len, String head, String script) {
		this.id = id;
		this.len = len;

		this.head = head;
		this.script = script;

		nodes = new ArrayList<String>();
	}
}

class State {
	LinkedHashMap<String, String> actionTable;

	LinkedHashMap<String, Integer> gotoTable;

	int id;

	State(int id) {
		this.id = id;

		gotoTable = new LinkedHashMap<String, Integer>();
		actionTable = new LinkedHashMap<String, String>();
	}

	String ActionOf(String symbol) {
		if (symbol == null)
			return null;
		return actionTable.get(symbol);
	}

	Integer Goto(String symbol) {
		if (symbol == null)
			return null;
		return gotoTable.get(symbol);
	}
}
