package tiki.syntax.node;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import tiki.syntax.Analyzer;
import tiki.syntax.SymbolManager;

public class decl_vararg_parameter extends Decl {
	String name;

	decl_specifiers specifiers;

	@Override
	void doCheck(Analyzer analyzer, SymbolManager symbolManager) {
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setSpecifiers(decl_specifiers specifiers) {
		this.specifiers = specifiers;
	}

	@Override
	public void toXML(Element upper) {
		Document doc = upper.getOwnerDocument();
		Element ele = doc.createElement(this.getClass().getSimpleName());
		Attr attr;

		specifiers.toXML(ele);

		attr = doc.createAttribute("name");
		attr.setValue(name);
		ele.setAttributeNode(attr);

		upper.appendChild(ele);
	}
}
