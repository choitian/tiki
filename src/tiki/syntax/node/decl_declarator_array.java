package tiki.syntax.node;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import tiki.syntax.Analyzer;
import tiki.syntax.SymbolManager;
import tiki.syntax.type.IType;
import tiki.syntax.type.Kind;
import tiki.syntax.type.TypeFactory;

public class decl_declarator_array extends decl_declarator {
	ListNode arrays;

	decl_declarator direct_declarator;

	private IType decode_array_list(ListNode al, IType type) {
		type = TypeFactory.getArray(type);
		if (al.list != null)
			return decode_array_list(al.list, type);
		return type;
	}

	@Override
	public void doCheck(Analyzer analyzer, SymbolManager symbolManager) {
		if (TypeFactory.test(baseType, Kind.VOID)) {
			analyzer.error("illegal,array of void", this.getPosition());
		}
		this.resultType = decode_array_list(arrays, this.baseType);

		if (direct_declarator instanceof decl_declarator_id) {
			id = direct_declarator.id;
		} else {

			direct_declarator.setBaseType(this.resultType);
			direct_declarator.check();
			this.resultType = direct_declarator.getResultType();
		}
	}

	public void setArrays(ListNode arrays) {
		this.arrays = arrays;
	}

	public void setDirect_declarator(decl_declarator direct_declarator) {
		this.direct_declarator = direct_declarator;
	}

	@Override
	public void toXML(Element upper) {
		Document doc = upper.getOwnerDocument();
		Element ele = doc.createElement(this.getClass().getSimpleName());
		Attr attr;

		if (arrays != null) {
			attr = doc.createAttribute("dimension");
			attr.setValue(String.valueOf(arrays.nodes().size()));
			ele.setAttributeNode(attr);
		}
		if (direct_declarator != null) {
			direct_declarator.toXML(ele);
		}
		upper.appendChild(ele);
	}

	@Override
	public String getId() {
		return direct_declarator.getId();
	}
}