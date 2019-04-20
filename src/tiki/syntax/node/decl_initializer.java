package tiki.syntax.node;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import tiki.syntax.Analyzer;
import tiki.syntax.Evalue;
import tiki.syntax.SymbolManager;
import tiki.syntax.type.IType;

public class decl_initializer extends SyntaxNode {
	Exp exp;
	decl_array_initializer array_initializer;
	private IType definedType;
	@Override
	void doCheck(Analyzer analyzer, SymbolManager symbolManager) {
		if(exp != null)
		{
			exp.check();
		}
		if(array_initializer != null)
		{
			array_initializer.setDefinedType(definedType);
			array_initializer.check();
		}
	}

	public void setExp(Exp exp) {
		this.exp = exp;
	}
	public void setArray_initializer(decl_array_initializer array_initializer) {
		this.array_initializer = array_initializer;
	}
	@Override
	public void toXML(Element upper) {
		Document doc = upper.getOwnerDocument();
		Element ele = doc.createElement(this.getClass().getSimpleName());

		if (exp != null)
			exp.toXML(ele);

		if (array_initializer != null)
			array_initializer.toXML(ele);

		upper.appendChild(ele);
	}

	public Evalue getEvalue() {
		if(exp != null)
		{
			return exp.getEvalue();
		}
		if(array_initializer != null)
		{
			return array_initializer.getEvalue();
		}
		return null;
	}

	public void setDefinedType(IType definedType) {
		this.definedType = definedType;
	}
}
