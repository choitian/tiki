package tiki.syntax.node;

import org.w3c.dom.Element;

import tiki.syntax.Analyzer;
import tiki.syntax.SymbolManager;
import tiki.syntax.SyntaxTree;

public abstract class SyntaxNode {
	private String position;
	private SyntaxTree tree;

	public final void check() {
		doCheck(tree.getAnalyzer(), tree.getAnalyzer().getSymbolManager());
	}

	abstract void doCheck(Analyzer analyzer, SymbolManager symbolManager);
	public final String getPosition() {
		return position;
	}

	public final SyntaxTree getTree() {
		return tree;
	}

	public final void setPosition(String value) {
		position = value;
	}

	public final void setTree(SyntaxTree tree) {
		this.tree = tree;
	}

	public abstract void toXML(Element upper);
}