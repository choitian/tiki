package tiki.syntax.node;

import org.w3c.dom.Element;

import tiki.syntax.Analyzer;
import tiki.syntax.SymbolManager;

public class decl_package_name extends SyntaxNode {
	String id;

	decl_package_name list;

	@Override
	void doCheck(Analyzer analyzer, SymbolManager symbolManager) {
		throw new RuntimeException("unexpected invocation");
	}

	public String getPackageName() {
		String name = id;
		decl_package_name level = this.list;
		while (level != null) {
			name = level.id + "." + name;
			level = level.list;
		}
		return name;
	}

	public void setId(String id) {
		this.id = id;
	}

	public void setList(decl_package_name list) {
		this.list = list;
	}

	@Override
	public void toXML(Element upper) {
		throw new RuntimeException("unexpected invocation");
	}
}
