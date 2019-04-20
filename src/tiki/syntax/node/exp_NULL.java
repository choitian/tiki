package tiki.syntax.node;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import tiki.syntax.Analyzer;
import tiki.syntax.Evalue;
import tiki.syntax.SymbolManager;

public class exp_NULL extends Exp {
	@Override
	public void doCheck(Analyzer analyzer, SymbolManager symbolManager) {
		setEvalue(Evalue.NULL);
	}

	@Override
	public void toXML(Element upper) {
		Document doc = upper.getOwnerDocument();
		Element ele = doc.createElement(this.getClass().getSimpleName());

		upper.appendChild(ele);
	}
}