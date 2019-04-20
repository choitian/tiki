package tiki.syntax.node;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import tiki.syntax.Analyzer;
import tiki.syntax.SymbolManager;

public class decl_unit extends SyntaxNode {
	ListNode class_definitions;

	ListNode imports;

	@Override
	void doCheck(Analyzer analyzer, SymbolManager symbolManager) {
		throw new RuntimeException("unexpected invocation");
	}

	public ListNode getClass_definitions() {
		return class_definitions;
	}

	public ListNode getImports() {
		return imports;
	}

	public void setClass_definitions(ListNode class_definitions) {
		this.class_definitions = class_definitions;
	}

	public void setImports(ListNode imports) {
		this.imports = imports;
	}

	@Override
	public void toXML(Element upper) {
		Document doc = upper.getOwnerDocument();
		Element ele = doc.createElement(this.getClass().getSimpleName());

		if (imports != null)
			imports.toXML(ele);

		class_definitions.toXML(ele);

		upper.appendChild(ele);
	}
}
