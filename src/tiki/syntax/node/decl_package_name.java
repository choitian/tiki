package tiki.syntax.node;

import org.w3c.dom.Element;

import tiki.syntax.Analyzer;
import tiki.syntax.SymbolManager;

public class decl_package_name extends SyntaxNode {
	private exp_SCOPE_NAME name;

	@Override
	void doCheck(Analyzer analyzer, SymbolManager symbolManager) {
		throw new RuntimeException("unexpected invocation");
	}

	public String getPackageName() {
		return name.getName();
	}

	public void setName(exp_SCOPE_NAME name) {
		this.name = name;
	}

	@Override
	public void toXML(Element upper) {
		throw new RuntimeException("unexpected invocation");
	}
}
