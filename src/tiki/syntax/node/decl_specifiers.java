package tiki.syntax.node;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import tiki.syntax.Analyzer;
import tiki.syntax.SymbolManager;
import tiki.syntax.type.IType;
import tiki.syntax.type.TypeFactory;

public class decl_specifiers extends Decl {
	boolean isStatic;

	decl_specifier storage_specifier;

	IType type;

	decl_specifier type_specifier;

	public decl_specifiers() {
		type = TypeFactory.VOID;
	}

	@Override
	public void doCheck(Analyzer analyzer, SymbolManager symbolManager) {
		if (storage_specifier != null) {
			getStorage(storage_specifier.value);
		}
		switch (type_specifier.kind) {
		case "class":
			String scopeName = type_specifier.getScopeName();
			type = analyzer.getTypeClass(scopeName);
			if (type == null) {
				String msg = String.format("%s cannot be resolved to a type", scopeName);
				analyzer.error(msg, this.getPosition());
			}
			break;
		case "primitive":
			type = TypeFactory.getPrimitiveType(type_specifier.value);
			break;
		default:
			type = null;
		}
		if (type == null) {
			type = TypeFactory.VOID;
		}
	}

	private void getStorage(String token) {
		if (token.equals("static")) {
			isStatic = true;
		}
	}

	public void setStorage_specifier(decl_specifier storage_specifier) {
		this.storage_specifier = storage_specifier;
	}

	public void setType_specifier(decl_specifier type_specifier) {
		this.type_specifier = type_specifier;
	}

	@Override
	public void toXML(Element upper) {
		Document doc = upper.getOwnerDocument();
		Element ele = doc.createElement(this.getClass().getSimpleName());

		if (type_specifier != null) {
			type_specifier.toXML(ele);
		}
		if (storage_specifier != null) {
			storage_specifier.toXML(ele);
		}
		upper.appendChild(ele);
	}
}