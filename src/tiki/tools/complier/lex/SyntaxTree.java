package tiki.tools.complier.lex;

import java.io.File;
import java.util.LinkedHashMap;
import java.util.TreeSet;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

enum NodeType
{
	NULL,
	CHAR,
	PLUS,
	END,
	CAT,
	OR,
}
class SyntaxTree {
	public static final LinkedHashMap<Integer, SyntaxTree> NODE_MAP = new LinkedHashMap<Integer, SyntaxTree>();

	public static void SaveAsXML(SyntaxTree tree, String file) {
		try {
			DocumentBuilderFactory doc_builder_factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder doc_builder = doc_builder_factory.newDocumentBuilder();
			Document doc = doc_builder.newDocument();

			Element root = doc.createElement("root");
			doc.appendChild(root);

			ToXML(tree, root, doc);

			TransformerFactory transformer_factory = TransformerFactory.newInstance();
			Transformer transformer = transformer_factory.newTransformer();
			DOMSource source = new DOMSource(doc);
			StreamResult result = new StreamResult(new File(file));

			transformer.transform(source, result);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	static void ToXML(SyntaxTree tree, Element root, Document doc) {
		if (tree == null)
			return;

		NodeType type = tree.type;
		Element node_e = doc.createElement(type.name());
		if (type.equals(NodeType.CHAR)) {
			Attr attr = doc.createAttribute("value");
			attr.setValue(String.valueOf((int) tree.value));
			node_e.setAttributeNode(attr);
		} else {
			ToXML(tree.ch0, node_e, doc);
			ToXML(tree.ch1, node_e, doc);
		}
		root.appendChild(node_e);
	}

	public SyntaxTree ch0;
	public SyntaxTree ch1;
	public String extra;
	TreeSet<Integer> first_pos;

	TreeSet<Integer> follow_pos;
	public int id;
	TreeSet<Integer> last_pos;
	boolean nullable;
	public int priority;

	NodeType type;

	public char value;

	private static int NO = 0;
	public SyntaxTree(NodeType type) {
		this.id = NO++;
		this.type = type;
		ch0 = null;
		ch1 = null;
		value = '0';
		extra = null;

		priority = 0;
		nullable = false;
		first_pos = new TreeSet<Integer>();
		last_pos = new TreeSet<Integer>();
		follow_pos = new TreeSet<Integer>();

		NODE_MAP.put(this.id, this);
	}
	private static int PRIORITY = 0;
	SyntaxTree LinkEnd(String script) {
		SyntaxTree end = new SyntaxTree(NodeType.END);
		end.priority = SyntaxTree.PRIORITY++;
		end.extra = script;

		SyntaxTree cat = new SyntaxTree(NodeType.CAT);
		cat.ch0 = this;
		cat.ch1 = end;

		return cat;
	}
}