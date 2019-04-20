package tiki.syntax.node;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import tiki.syntax.Analyzer;
import tiki.syntax.SymbolManager;
import tiki.syntax.type.IType;
import tiki.syntax.type.TypeFactory;
import tiki.syntax.type.TypeMethod;

public class decl_declarator_method extends decl_declarator {
	ListNode parameters;

	decl_vararg_parameter vararg_parameter;

	@Override
	public void doCheck(Analyzer analyzer, SymbolManager symbolManager) {
		TypeMethod tm = TypeFactory.getMethod(this.baseType);

		symbolManager.enterScope(tm);

		if (parameters != null) {
			parameters.check();
		}
		if (vararg_parameter != null) // varargs
		{
			decl_specifiers ds = vararg_parameter.specifiers;
			ds.check();

			// add vararg_name to scope.
			IType varargsType = TypeFactory.getArray(ds.type);

			symbolManager.addSymbol(vararg_parameter.name, varargsType, ds.isStatic, this.getPosition());
			tm.setVararg(ds.type);
		}
		symbolManager.exitScope();
		this.resultType = tm;
	}

	public void setParameters(ListNode parameters) {
		this.parameters = parameters;
	}

	public void setVararg_parameter(decl_vararg_parameter vararg_parameter) {
		this.vararg_parameter = vararg_parameter;
	}

	@Override
	public void toXML(Element upper) {
		Document doc = upper.getOwnerDocument();
		Element ele = doc.createElement(this.getClass().getSimpleName());
		Attr attr;

		attr = doc.createAttribute("id");
		attr.setValue(id);
		ele.setAttributeNode(attr);

		if (parameters != null) {
			parameters.toXML(ele);
		}
		if (vararg_parameter != null) // varargs
		{
			vararg_parameter.toXML(ele);
		}
		upper.appendChild(ele);
	}
	@Override
	public String getId() {
		return id;
	}
}