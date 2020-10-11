package tiki.tools.complier.lex;

import java.io.InputStream;
import java.util.LinkedHashMap;
import java.util.Map.Entry;
import java.util.Stack;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import tiki.uitls.collection.HashSet;
import tiki.uitls.collection.Pair;
import tiki.tools.complier.lex.node.NodeType;
import tiki.tools.complier.lex.node.SyntaxTree;

public class DFA {
	DState dstate_initial_;
	public LinkedHashMap<String, DState> dstate_map_;

	public DFA() {
		dstate_map_ = new LinkedHashMap<String, DState>();
		dstate_initial_ = null;
	}

	void CatalogueFollowpos(DState state, LinkedHashMap<Character, DState> catalogue) {
		catalogue.clear();

		for (SyntaxTree node : state.nodes.set()) {
			if (node.type.equals(NodeType.CHAR)) {
				char value = node.value;
				if (!catalogue.containsKey(value)) {
					catalogue.put(value, new DState());
				}
				DState catalogue_state = catalogue.get(value);
				catalogue_state.nodes.add(node.follow_pos);
			}
		}
	}

	void Compute(SyntaxTree tree) {
		if (tree == null)
			return;
		if (tree.ch0 != null)
			Compute(tree.ch0);
		if (tree.ch1 != null)
			Compute(tree.ch1);

		if (tree.type.equals(NodeType.END)) {
			tree.first_pos.add(tree);
			tree.last_pos.add(tree);
		} else if (tree.type.equals(NodeType.NULL)) {
			tree.nullable = true;
		} else if (tree.type.equals(NodeType.CHAR)) {
			tree.first_pos.add(tree);
			tree.last_pos.add(tree);
		} else if (tree.type.equals(NodeType.OR)) {
			tree.nullable = tree.ch0.nullable || tree.ch1.nullable;

			tree.first_pos.add(tree.ch0.first_pos);
			tree.first_pos.add(tree.ch1.first_pos);

			tree.last_pos.add(tree.ch0.last_pos);
			tree.last_pos.add(tree.ch1.last_pos);
		} else if (tree.type.equals(NodeType.CAT)) {
			tree.nullable = tree.ch0.nullable && tree.ch1.nullable;

			tree.first_pos.add(tree.ch0.first_pos);
			if (tree.ch0.nullable)
				tree.first_pos.add(tree.ch1.first_pos);

			tree.last_pos.add(tree.ch1.last_pos);
			if (tree.ch1.nullable)
				tree.last_pos.add(tree.ch0.last_pos);

			for (SyntaxTree node : tree.ch0.last_pos.set()) {
				node.follow_pos.add(tree.ch1.first_pos);
			}
		}else if (tree.type.equals(NodeType.PLUS)) {
			tree.nullable = tree.ch0.nullable;

			tree.first_pos.add(tree.ch0.first_pos);
			tree.last_pos.add(tree.ch0.last_pos);

			for (SyntaxTree node : tree.last_pos.set()) {
				node.follow_pos.add(tree.first_pos);
			}
		}
	}

	public void Construct(InputStream lex_re) throws Exception {
		RE_File re = new RE_File();
		SyntaxTree tree = re.ToSyntaxTree(lex_re);
		Compute(tree);

		dstate_initial_ = new DState();
		dstate_initial_.nodes.add(tree.first_pos);
		TryAddState(dstate_initial_);

		Stack<DState> unmarkeds = new Stack<DState>();
		unmarkeds.push(dstate_initial_);

		LinkedHashMap<Character, DState> catalogue = new LinkedHashMap<Character, DState>();
		while (!unmarkeds.isEmpty()) {
			DState unmarked = unmarkeds.pop();

			CatalogueFollowpos(unmarked, catalogue);
			for (Entry<Character, DState> entry : catalogue.entrySet()) {
				char key = entry.getKey();
				DState new_state = entry.getValue();
				Pair<DState, Boolean> result = TryAddState(new_state);
				if (result.second) {
					unmarkeds.push(new_state);
				}
				unmarked.transition.put(key, result.first);
			}
		}
	}

	public Document toXML() {
		try {
			DocumentBuilderFactory doc_builder_factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder doc_builder = doc_builder_factory.newDocumentBuilder();
			Document doc = doc_builder.newDocument();

			Element root = doc.createElement("root");
			doc.appendChild(root);

			for (Entry<String, DState> entry : dstate_map_.entrySet()) {
				DState state = entry.getValue();

				Element state_e = state.ToXML(doc);
				root.appendChild(state_e);
			}
			return doc;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	Pair<DState, Boolean> TryAddState(DState state) {
		String hash_string = state.nodes.hashString();
		if (dstate_map_.containsKey(hash_string)) {
			return new Pair<DState, Boolean>(dstate_map_.get(hash_string), Boolean.FALSE);
		} else {
			state.assignID(dstate_map_.size());
			state.assignAcceptance();

			dstate_map_.put(hash_string, state);
			return new Pair<DState, Boolean>(state, Boolean.TRUE);
		}
	}
}

class DState {
	SyntaxTree acceptance;
	int id;
	HashSet<SyntaxTree> nodes;
    
	LinkedHashMap<Character, DState> transition;

	DState() {
		nodes = new HashSet<>();
		transition = new LinkedHashMap<Character, DState>();
		acceptance = null;
	}

	public boolean Acceptable() {
		return acceptance != null;
	}

	public void assignAcceptance() {
		for (SyntaxTree node : nodes.set()) {
			if (node.type.equals(NodeType.END)) {
				if (acceptance == null || acceptance.priority > node.priority)
					acceptance = node;
			}
		}
	}

	public void assignID(int id) {
		this.id = id;
	}
	public Element ToXML(Document doc) {
		Element state_e = doc.createElement("state");
		Attr attr = null;

		attr = doc.createAttribute("id");
		attr.setValue(String.format("%X", id));
		state_e.setAttributeNode(attr);

		if (Acceptable()) {
			attr = doc.createAttribute("acceptable");
			attr.setValue("true");
			state_e.setAttributeNode(attr);

			attr = doc.createAttribute("acceptance");
			attr.setValue(acceptance.extra);
			state_e.setAttributeNode(attr);
		}

		for (Entry<Character, DState> entry : transition.entrySet()) {
			Element item_e = doc.createElement("goto");

			int key = entry.getKey();

			attr = doc.createAttribute("on");
			attr.setValue(String.format("%X", key));
			item_e.setAttributeNode(attr);

			attr = doc.createAttribute("to");
			attr.setValue(String.format("%X", entry.getValue().id));
			item_e.setAttributeNode(attr);

			state_e.appendChild(item_e);
		}
		return state_e;
	}
}
