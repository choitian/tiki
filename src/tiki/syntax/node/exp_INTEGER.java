package tiki.syntax.node;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import tiki.syntax.Analyzer;
import tiki.syntax.Evalue;
import tiki.syntax.SymbolManager;
import tiki.syntax.type.TypeFactory;

public class exp_INTEGER extends Exp {
	private String value;

	@Override
	public void doCheck(Analyzer analyzer, SymbolManager symbolManager) {
		setEvalue(Evalue.newConstant(value, TypeFactory.INT));
	}

	public void setValue(String value) {
		this.value = value;
	}

	@Override
	public void toXML(Element upper) {
		Document doc = upper.getOwnerDocument();
		Element ele = doc.createElement(this.getClass().getSimpleName());

		ele.setTextContent(value);
		upper.appendChild(ele);
	}
}