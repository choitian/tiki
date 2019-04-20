package tiki.syntax.node;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import tiki.syntax.Analyzer;
import tiki.syntax.Evalue;
import tiki.syntax.SymbolManager;

public class exp_FSELECT extends Exp {
	Exp base;

	String name;

	@Override
	public void doCheck(Analyzer analyzer, SymbolManager symbolManager) {
		base.check();
		if (base.getEvalue() == null)
			return;

		Evalue result = exp_SCOPE_NAME.checkField(analyzer, base.getEvalue(), name, this.getPosition());
		setEvalue(result);
	}

	public void setBase(Exp base) {
		this.base = base;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Override
	public void toXML(Element upper) {
		Document doc = upper.getOwnerDocument();
		Element ele = doc.createElement(this.getClass().getSimpleName());
		Attr attr;

		if (base != null)
			base.toXML((Element) ele.appendChild(doc.createElement("base")));

		attr = doc.createAttribute("name");
		attr.setValue(name);
		ele.setAttributeNode(attr);

		upper.appendChild(ele);
	}
}