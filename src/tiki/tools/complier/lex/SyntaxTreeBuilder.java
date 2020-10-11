package tiki.tools.complier.lex;

import java.util.Stack;
import java.util.TreeSet;

import tiki.tools.complier.lex.node.NodeType;
import tiki.tools.complier.lex.node.SyntaxTree;
import tiki.tools.complier.parser.ParseTree;

class SyntaxTreeBuilder {
	public static final int ASCII_RANGE = 128;

	public static SyntaxTree Build(ParseTree tree) throws Exception {
		Stack<SyntaxTree> track = new Stack<SyntaxTree>();

		ToTree(tree, track);

		if (track.size() != 1) {
			throw new Exception(String.format("BuildSyntaxTree,ERR"));
		}
		return track.peek();
	}

	public static String ExcludeByASCIIRange(String value) {
		StringBuilder sb = new StringBuilder();

		TreeSet<Integer> excluded = new TreeSet<Integer>();
		for (char ch : value.toCharArray()) {
			int i = ch;
			excluded.add(i);
		}
		for (int i = 0; i < ASCII_RANGE; i++) {
			if (!excluded.contains(i)) {
				char ch = (char) i;
				sb.append(ch);
			}
		}
		return sb.toString();
	}

	public static void Handler(ParseTree tree, Stack<SyntaxTree> track) {
		String script = tree.script;

		if (script.contains("LITERAL")) {
			SyntaxTree node = Handler_LITERAL(tree.nodes.getFirst().extra);

			track.push(node);
		} else if (script.contains("ARRAY")) {
			SyntaxTree node = Handler_ARRAY(tree.nodes.getFirst().extra);

			track.push(node);
		} else if (script.contains("DOT")) {
			SyntaxTree node = Handler_DOT(tree.nodes.getFirst().extra);

			track.push(node);
		} else if (script.contains("CHAR")) {
			SyntaxTree node = Handler_CHAR(tree.nodes.getFirst().extra);

			track.push(node);
		} else if (script.contains("STAR")) {
			track.push(new SyntaxTree(NodeType.OR, new SyntaxTree(NodeType.PLUS, track.pop()),
					new SyntaxTree(NodeType.NULL)));
		} else if (script.contains("PLUS")) {
			track.push(new SyntaxTree(NodeType.PLUS, track.pop()));
		} else if (script.contains("QUESTION")) {
			track.push(new SyntaxTree(NodeType.OR, track.pop(), new SyntaxTree(NodeType.NULL)));
		} else if (script.contains("CAT")) {
			SyntaxTree node = new SyntaxTree(NodeType.CAT);

			node.ch1 = track.pop();
			node.ch0 = track.pop();

			track.push(node);
		} else if (script.contains("OR")) {
			SyntaxTree node = new SyntaxTree(NodeType.OR);

			node.ch1 = track.pop();
			node.ch0 = track.pop();

			track.push(node);
		}
	}

	public static SyntaxTree Handler_ARRAY(String extra) {
		String char_list = extra.substring(1, extra.length() - 1);

		char_list = char_list.replaceAll("0-9", "0123456789");
		char_list = char_list.replaceAll("a-z", "abcdefghijklmnopqrstuvwxyz");
		char_list = char_list.replaceAll("A-Z", "ABCDEFGHIJKLMNOPQRSTUVWXYZ");
		char_list = char_list.replaceAll("a-f", "abcdef");
		char_list = char_list.replaceAll("A-F", "ABCDEF");

		char_list = StringUtils.unescapeString(char_list);

		if (char_list.startsWith("^")) {
			char_list = ExcludeByASCIIRange(char_list.substring(1));
		}

		SyntaxTree tree = null;
		for (char ch : char_list.toCharArray()) {
			SyntaxTree node = new SyntaxTree(NodeType.CHAR);
			node.value = ch;

			if (tree == null) {
				tree = node;
			} else {
				SyntaxTree or = new SyntaxTree(NodeType.OR);
				or.ch0 = tree;
				or.ch1 = node;
				tree = or;
			}
		}
		return tree;
	}

	public static SyntaxTree Handler_CHAR(String extra) {
		SyntaxTree tree = new SyntaxTree(NodeType.CHAR);
		if (extra.length() > 1 && extra.startsWith("\\"))
			tree.value = StringUtils.unescapeCharacter(extra.charAt(1));
		else
			tree.value = extra.charAt(0);

		return tree;
	}

	public static SyntaxTree Handler_DOT(String extra) {
		SyntaxTree tree = null;
		for (int ch = 0; ch < ASCII_RANGE; ch++) {
			char value = (char) ch;

			if (value == '\n')
				continue;

			SyntaxTree node = new SyntaxTree(NodeType.CHAR);
			node.value = value;

			if (tree == null) {
				tree = node;
			} else {
				SyntaxTree or = new SyntaxTree(NodeType.OR);
				or.ch0 = tree;
				or.ch1 = node;
				tree = or;
			}
		}
		return tree;
	}

	public static SyntaxTree Handler_LITERAL(String extra) {
		String char_list = extra.substring(1, extra.length() - 1);

		SyntaxTree tree = null;
		for (char ch : char_list.toCharArray()) {
			SyntaxTree node = new SyntaxTree(NodeType.CHAR);
			node.value = ch;

			if (tree == null) {
				tree = node;
			} else {
				SyntaxTree cat = new SyntaxTree(NodeType.CAT);
				cat.ch0 = tree;
				cat.ch1 = node;
				tree = cat;
			}
		}
		return tree;
	}

	static void ToTree(ParseTree tree, Stack<SyntaxTree> track) {
		String node_type = tree.nodeType;

		if (node_type.equals(ParseTree.INTERIOR)) {
			for (ParseTree node : tree.nodes) {
				ToTree(node, track);
			}
			if (tree.script != null && !tree.script.isEmpty()) {
				Handler(tree, track);
			}
		} else if (node_type.equals(ParseTree.LEAF)) {
			// nothing
		}
	}
}