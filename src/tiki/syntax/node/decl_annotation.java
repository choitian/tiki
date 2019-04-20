package tiki.syntax.node;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import tiki.syntax.Analyzer;
import tiki.syntax.SymbolManager;
import tiki.uitls.Formator;

public class decl_annotation extends Decl {
	ListNode elements;
	String info;

	String key;

	private void checkElements(ListNode elements) {
		ListNode list = elements.list;
		decl_annotation_element node = (decl_annotation_element) elements.node;

		if (list != null) {
			checkElements(list);
		}

		String key = node.key;
		String value = node.value;

		// trim " and unescape
		String text = value;
		text = text.substring(1, text.length() - 1);
		String text_unescape = Formator.unescapeJava(text);

		info += "@" + key + "=" + text_unescape;
	}

	@Override
	public void doCheck(Analyzer analyzer, SymbolManager symbolManager) {
		info = key;
		if (elements != null) {
			checkElements(elements);
		}
	}

	public void setElements(ListNode elements) {
		this.elements = elements;
	}

	public void setKey(String key) {
		this.key = key;
	}

	@Override
	public void toXML(Element upper) {
		Document doc = upper.getOwnerDocument();
		Element ele = doc.createElement(this.getClass().getSimpleName());

		Attr attr = doc.createAttribute("key");
		attr.setValue(key);
		ele.setAttributeNode(attr);

		if (elements != null)
			elements.toXML(ele);

		upper.appendChild(ele);
	}
}