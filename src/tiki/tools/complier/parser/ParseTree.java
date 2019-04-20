package tiki.tools.complier.parser;

import java.util.LinkedList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class ParseTree {
	public static final String INTERIOR = "INTERIOR";
	public static final String LEAF = "LEAF";

	private static void ToXML(ParseTree tree, Element root, Document doc) {
		String node_type = tree.nodeType;

		if (node_type.equals(ParseTree.INTERIOR)) {
			Element node_e = doc.createElement("node");
			Attr attr = null;

			for (ParseTree node : tree.nodes) {
				ToXML(node, node_e, doc);
			}

			attr = doc.createAttribute("name");
			attr.setValue(tree.name);
			node_e.setAttributeNode(attr);

			if (tree.script != null && !tree.script.isEmpty()) {
				attr = doc.createAttribute("script");
				attr.setValue(tree.script);
				node_e.setAttributeNode(attr);
				root.appendChild(node_e);
			}

			root.appendChild(node_e);
		} else if (node_type.equals(ParseTree.LEAF)) {
			Element node_e = doc.createElement("node");
			Attr attr = null;

			attr = doc.createAttribute("name");
			attr.setValue(tree.name);
			node_e.setAttributeNode(attr);

			if (tree.extra != null && !tree.extra.isEmpty()) {
				attr = doc.createAttribute("extra");
				attr.setValue(tree.extra);
				node_e.setAttributeNode(attr);
			}
			if (tree.position != null && !tree.position.isEmpty()) {
				attr = doc.createAttribute("position");
				attr.setValue(tree.position);
				node_e.setAttributeNode(attr);
			}

			root.appendChild(node_e);
		}
	}

	public String extra;

	public String name;
	public LinkedList<ParseTree> nodes;
	public String nodeType;
	public String position;

	public String script;

	public ParseTree(String nodeType, String position) {
		this.nodeType = nodeType;
		this.position = position;

		nodes = new LinkedList<ParseTree>();
		script = null;
		extra = null;
	}

	public void AddNode(ParseTree node) {
		nodes.addFirst(node);
	}

	public Document toXML() {
		try {
			DocumentBuilderFactory doc_builder_factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder doc_builder = doc_builder_factory.newDocumentBuilder();
			Document doc = doc_builder.newDocument();

			Element root = doc.createElement("root");
			doc.appendChild(root);

			ToXML(this, root, doc);

			return doc;

		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
}
