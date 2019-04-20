package tiki.syntax.node;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import tiki.syntax.Analyzer;
import tiki.syntax.SymbolManager;

public class stmt_exp extends Stmt {
	Exp exp;

	@Override
	public void doCheck(Analyzer analyzer, SymbolManager symbolManager) {
		if (exp == null)
			return;

		exp.check();
	}
	public stmt_exp(){}
	public stmt_exp(Exp exp)
	{
		this.exp =exp;
		this.setTree(exp.getTree());
		this.setPosition(exp.getPosition());
	}
	public void setExp(Exp exp) {
		this.exp = exp;
	}

	@Override
	public void toXML(Element upper) {
		Document doc = upper.getOwnerDocument();
		Element ele = doc.createElement(this.getClass().getSimpleName());

		if (exp != null)
			exp.toXML(ele);
		upper.appendChild(ele);
	}
}