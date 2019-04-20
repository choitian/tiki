package tiki.tools.complier.parser;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Stack;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import tiki.tools.complier.parser.ParseTree;

class Script {
	LinkedHashMap<String, String> attributes = new LinkedHashMap<String, String>();
	String name;
}

class ASTN {
	LinkedHashMap<String, Object> attrs = new LinkedHashMap<>();

	private void toListNodeXML(ASTN ln, List<ASTN> nodes) {
		if (ln.attrs.containsKey("list")) {
			ASTN list = (ASTN) ln.attrs.get("list");
			toListNodeXML(list, nodes);
		}
		if (ln.attrs.containsKey("node")) {
			ASTN node = (ASTN) ln.attrs.get("node");
			nodes.add(node);
		}
	}

	public void toXML(Element upper) {
		Document doc = upper.getOwnerDocument();
		Element cur = doc.createElement(attrs.get("a").toString());
		for (Entry<String, Object> e : attrs.entrySet()) {
			String key = e.getKey();
			Object value = e.getValue();
			if (key.equals("a")) {
				continue;
			}
			if (key.equals("i")) {
				continue;
			}
			if (value instanceof String) {
				Element attr = doc.createElement(key);
				attr.setTextContent((String) value);
				cur.appendChild(attr);
			} else {
				ASTN child = (ASTN) value;
				if (child.attrs.get("a").equals("ListNode")) {
					LinkedList<ASTN> nodes = new LinkedList<>();
					toListNodeXML(child, nodes);
					for (ASTN node : nodes) {
						node.toXML(cur);
					}
				} else {
					child.toXML(cur);
				}
			}
		}
		upper.appendChild(cur);
	}
}

public class AST {
	private Stack<ASTN> track;

	private ASTN unit;

	public AST(ParseTree tree) {
		track = new Stack<ASTN>();
		build(tree);
		if (track.size() != 1) {
			throw new IllegalArgumentException("error : build syntax tree failed.");
		}
		if (!(track.peek().attrs.get("a").equals("decl_unit"))) {
			throw new IllegalArgumentException("error : build syntax tree failed.");
		}
		unit = track.peek();
	}

	void build(ParseTree tree) {
		String node_type = tree.nodeType;
		if (node_type.equals(ParseTree.INTERIOR)) {
			for (ParseTree node : tree.nodes) {
				build(node);
			}
			if (tree.script != null && !tree.script.isEmpty()) {
				Handler(tree);
			}
		} else if (node_type.equals(ParseTree.LEAF)) {
			// nothing
		}
	}

	Script DecodeScript(String script) {
		Script result = null;

		Pattern pattern;
		String regx = "\\{\\s*(?<name>[\\w.]+)(\\s*\\(\\s*(?<attributes>([\\w<>]+(=[$0-9\\w<>]*)?\\s*,?\\s*)+)?\\s*\\)\\s*)\\s*\\}";
		pattern = Pattern.compile(regx);
		Matcher matcher = pattern.matcher(script);
		if (matcher.find()) {
			result = new Script();

			result.name = matcher.group("name");

			String attributes = matcher.group("attributes");
			if (attributes != null && !attributes.trim().isEmpty()) {
				String[] parts = attributes.split(",");
				Pattern attributePattern = Pattern.compile("(?<name>[\\w<>]+)(=(?<value>[$0-9\\w<>]*))?");
				for (String part : parts) {
					Matcher attributeMatcher = attributePattern.matcher(part);
					if (attributeMatcher.find()) {
						result.attributes.put(attributeMatcher.group("name"), attributeMatcher.group("value"));
					}
				}
			}
		}

		return result;
	}

	public void Handler(ParseTree tree) {
		Script script = DecodeScript(tree.script);
		if (script != null) {
			scriptHandler(script, tree);
			return;
		}
	}

	Matcher regxDecode(String str, String regx) {
		Pattern pattern;
		pattern = Pattern.compile(regx);

		return pattern.matcher(str);
	}

	private void scriptHandler(Script script, ParseTree tree) {
		ASTN n = new ASTN();
		n.attrs.put("a", script.name);
		n.attrs.put("i", tree.position);

		// get how many child without notes,which means it is in syntax nodes.
		Stack<ASTN> syntax_nodes = new Stack<ASTN>();
		for (Entry<String, String> attribute : script.attributes.entrySet()) {
			String value = attribute.getValue();
			if (value == null || value.isEmpty()) {
				syntax_nodes.push(track.pop());
			}
		}

		for (Entry<String, String> attribute : script.attributes.entrySet()) {
			String key = attribute.getKey();
			String value = attribute.getValue();

			if (value == null || value.isEmpty()) {
				ASTN fst = syntax_nodes.pop();

				String regx = "(?<name>[\\w]+)(<(?<type>[\\w]+)>)?";
				Matcher ret = regxDecode(key, regx);
				if (ret.matches()) {
					String name = ret.group("name");
					String type = ret.group("type");
					n.attrs.put(name, fst);
				}
			} else if (value.startsWith("$")) {
				String regx = "\\$(?<index>[0-9]+)(<(?<member>[\\w]+)>)?";
				Matcher ret = regxDecode(value, regx);
				if (ret.matches()) {
					String index = ret.group("index");
					String member = ret.group("member");

					ParseTree parse_node = tree.nodes.get(Integer.parseInt(index));
					String keyValue;
					member = member == null ? "extra" : member;
					switch (member) {
					case "name":
						keyValue = parse_node.name;
						break;
					default:
						keyValue = parse_node.extra;
						break;
					}

					n.attrs.put(key, keyValue);
				}
			} else {
				n.attrs.put(key, value);
			}
		}
		track.push(n);
	}

	public Document toXML() {
		try {
			DocumentBuilderFactory doc_builder_factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder doc_builder = doc_builder_factory.newDocumentBuilder();
			Document doc = doc_builder.newDocument();

			Element root = doc.createElement("root");
			doc.appendChild(root);

			unit.toXML(root);

			return doc;

		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
}
