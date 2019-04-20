package tiki.syntax.node;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import tiki.syntax.Analyzer;
import tiki.syntax.Evalue;
import tiki.syntax.SymbolManager;
import tiki.uitls.Formator;

public class exp_STRING extends Exp {
	private String value;

	@Override
	public void doCheck(Analyzer analyzer, SymbolManager symbolManager) {
		// trim "
		String text = value;
		text = text.substring(1, text.length() - 1);
		// unescape
		String text_unescape = Formator.unescapeJava(text);

		setEvalue(Evalue.newConstantString(text_unescape));
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