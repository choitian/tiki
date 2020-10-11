package tiki.tools.complier.lex.node;

import java.util.List;
import java.util.TreeSet;
import java.util.stream.Collectors;

public class SyntaxTreeNodeSet {
	private TreeSet<String> nodeIds = new TreeSet<String>();

	public void add(SyntaxTree node) {
		nodeIds.add(node.id);
	}
	public void add(SyntaxTreeNodeSet ns) {
		nodeIds.addAll(ns.nodeIds);
	}

	public List<SyntaxTree> toCollection() {
		return nodeIds.stream().map(n -> SyntaxTree.NODE_MAP.get(n)).collect(Collectors.toList());
	}
	public String hashString() {
		StringBuilder sb = new StringBuilder();
		for (String id : nodeIds) {
			sb.append(".");
			sb.append(id);
		}
		return sb.toString();
	}
}