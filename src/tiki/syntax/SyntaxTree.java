package tiki.syntax;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.LinkedHashMap;
import java.util.Map.Entry;
import java.util.Stack;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import tiki.syntax.node.SyntaxNode;
import tiki.syntax.node.decl_unit;
import tiki.tools.complier.parser.ParseTree;

class Script {
	LinkedHashMap<String, String> attributes = new LinkedHashMap<String, String>();
	String name;
}

public class SyntaxTree {
	private Analyzer analyzer;
	private Stack<SyntaxNode> track;

	private decl_unit unit;

	public SyntaxTree(ParseTree tree) {
		track = new Stack<SyntaxNode>();
		build(tree);
		if (track.size() != 1) {
			throw new IllegalArgumentException("build syntax tree failed.");
		}
		if (!(track.peek() instanceof decl_unit)) {
			throw new IllegalArgumentException("build syntax tree failed.");
		}
		unit = (decl_unit) track.peek();
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

	public SyntaxNode create(String name, String position) {
		SyntaxNode node = null;
		try {
			Class<?> clazz = Class.forName("tiki.syntax.node." + name);
			Constructor<?> ctor = clazz.getConstructor();
			node = (SyntaxNode) ctor.newInstance();
		} catch (Exception e) {
			e.printStackTrace();
		}
		node.setTree(this);
		node.setPosition(position);
		return node;
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

	public Analyzer getAnalyzer() {
		return analyzer;
	}

	public Class<?> getClass(String name) {
		Class<?> clazz = null;
		try {
			clazz = Class.forName("tiki.syntax.node." + name);

		} catch (Exception e) {
			e.printStackTrace();
		}
		return clazz;
	}

	private Method getMethod(Class<?> clazz, String name, Class<?>... parameterTypes) {
		Method method = null;
		try {
			method = clazz.getDeclaredMethod(name, parameterTypes);
		} catch (NoSuchMethodException e) {
			Class<?> superClass = clazz.getSuperclass();
			if (superClass != null) {
				return getMethod(superClass, name, parameterTypes);
			} else {
				// e.printStackTrace();
			}
		} catch (SecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return method;
	}

	private String getSetterName(String variable) {
		return "set" + Character.toUpperCase(variable.charAt(0)) + variable.substring(1);
	}

	public decl_unit getUnit() {
		return unit;
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
		SyntaxNode node = create(script.name, tree.position);

		// get how many child without notes,which means it is in syntax nodes.
		Stack<SyntaxNode> syntax_nodes = new Stack<SyntaxNode>();
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
				SyntaxNode fst = syntax_nodes.pop();

				String regx = "(?<name>[\\w]+)(<(?<type>[\\w]+)>)?";
				Matcher ret = regxDecode(key, regx);
				if (ret.matches()) {
					String name = ret.group("name");
					String type = ret.group("type");
					Class<?> clazz;
					if (type == null || type.isEmpty()) {
						clazz = SyntaxNode.class;
					} else {
						clazz = getClass(type);
					}
					try {
						Method method = getMethod(node.getClass(), getSetterName(name), clazz);
						method.invoke(node, fst);
					} catch (Exception e) {
						e.printStackTrace();
					}
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

					try {
						Method method = getMethod(node.getClass(), getSetterName(key), String.class);
						method.invoke(node, keyValue);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			} else {
				try {
					Method method = getMethod(node.getClass(), getSetterName(key), String.class);
					method.invoke(node, value);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}

		try {
			Method method = getMethod(node.getClass(), "transform");
			if (method != null)
				node = (SyntaxNode) method.invoke(node);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		track.push(node);
	}

	public void setAnalyzer(Analyzer analyzer) {
		this.analyzer = analyzer;
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
