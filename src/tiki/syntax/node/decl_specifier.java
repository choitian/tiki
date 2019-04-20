package tiki.syntax.node;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import tiki.syntax.Analyzer;
import tiki.syntax.SymbolManager;

public class decl_specifier extends Decl {
	String kind;

	exp_SCOPE_NAME scope_name;

	String value;

	@Override
	void doCheck(Analyzer analyzer, SymbolManager symbolManager) {
	}

	public String getScopeName() {
		return scope_name.getName();
	}

	public void setKind(String kind) {
		this.kind = kind;
	}

	public void setScope_name(exp_SCOPE_NAME scope_name) {
		this.scope_name = scope_name;
	}

	public void setValue(String value) {
		this.value = value;
	}

	@Override
	public void toXML(Element upper) {
		Document doc = upper.getOwnerDocument();
		Element ele = doc.createElement(this.getClass().getSimpleName());
		Attr attr;

		if (scope_name != null) {
			scope_name.toXML(upper);
		}

		if (kind != null) {
			attr = doc.createAttribute("kind");
			attr.setValue(kind);
			ele.setAttributeNode(attr);
		}

		attr = doc.createAttribute("value");
		attr.setValue(value);
		ele.setAttributeNode(attr);

		upper.appendChild(ele);
	}
}
