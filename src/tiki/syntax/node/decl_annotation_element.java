package tiki.syntax.node;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import tiki.syntax.Analyzer;
import tiki.syntax.SymbolManager;

public class decl_annotation_element extends Decl {
	String key;

	String value;

	@Override
	void doCheck(Analyzer analyzer, SymbolManager symbolManager) {
	}

	public void setKey(String key) {
		this.key = key;
	}

	public void setValue(String value) {
		this.value = value;
	}

	@Override
	public void toXML(Element upper) {
		Document doc = upper.getOwnerDocument();
		Element ele = doc.createElement(key);
		ele.setTextContent(value);
		upper.appendChild(ele);
	}
}
