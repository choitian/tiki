package tiki.syntax.node;

import java.util.LinkedList;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import tiki.syntax.Analyzer;
import tiki.syntax.SymbolManager;

public class ListValue extends SyntaxNode {
	ListValue list;
	
	String value;

	public ListValue(){}	
	@Override
	public void doCheck(Analyzer analyzer, SymbolManager symbolManager) {
	}

	public LinkedList<String> values() {
		LinkedList<String> ret = new LinkedList<String>();
		walk(ret);
		return ret;
	}

	public void setList(ListValue list) {
		this.list = list;
	}

	public void setValue(String value) {
		this.value = value;
	}

	@Override
	public void toXML(Element upper) {
		Document doc = upper.getOwnerDocument();
		for (String value : values()) {
			Element attr = doc.createElement("value");
			attr.setTextContent(value);
			upper.appendChild(attr);	
		}
	}

	private void walk(LinkedList<String> values) {
		if (list != null) {
			list.walk(values);
		}
		if (value != null) {
			values.add(value);
		}
	}
}
