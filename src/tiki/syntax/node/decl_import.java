package tiki.syntax.node;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import tiki.syntax.Analyzer;
import tiki.syntax.SymbolManager;

public class decl_import extends SyntaxNode {
	private String package_name;

	@Override
	void doCheck(Analyzer analyzer, SymbolManager symbolManager) {
		throw new RuntimeException("unexpected invocation");
	}

	public String getPackage_name() {
		return package_name;
	}

	public void setPackage_name(decl_package_name package_name) {
		this.package_name = package_name.getPackageName();
	}

	@Override
	public void toXML(Element upper) {
		Document doc = upper.getOwnerDocument();
		Element ele = doc.createElement(this.getClass().getSimpleName());

		Attr attr = doc.createAttribute("package");
		attr.setValue(package_name);
		ele.setAttributeNode(attr);

		upper.appendChild(ele);
	}
}
