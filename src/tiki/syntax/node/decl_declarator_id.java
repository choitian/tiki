package tiki.syntax.node;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import tiki.syntax.Analyzer;
import tiki.syntax.SymbolManager;

public class decl_declarator_id extends decl_declarator {
	@Override
	public void doCheck(Analyzer analyzer, SymbolManager symbolManager) {
		this.resultType = this.baseType;
	}

	@Override
	public void toXML(Element upper) {
		Document doc = upper.getOwnerDocument();
		Element ele = doc.createElement(this.getClass().getSimpleName());
		Attr attr;

		attr = doc.createAttribute("id");
		attr.setValue(id);
		ele.setAttributeNode(attr);

		upper.appendChild(ele);
	}

	@Override
	public String getId() {
		return id;
	}
}