package tiki.syntax.node;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import tiki.syntax.Evalue;

public abstract class Exp extends SyntaxNode {
	private Evalue evalue;

	private String op;

	private Exp op0;

	private Exp op1;

	public final Evalue getEvalue() {
		return evalue;
	}

	public final String getOp() {
		return op;
	}

	public final Exp getOp0() {
		return op0;
	}

	public final Exp getOp1() {
		return op1;
	}

	public final void setEvalue(Evalue e) {
		this.evalue = e;
	}

	public final void setOp(String op) {
		this.op = op;
	}

	public final void setOp0(Exp op0) {
		this.op0 = op0;
	}

	public final void setOp1(Exp op1) {
		this.op1 = op1;
	}

	@Override
	public void toXML(Element upper) {
		Document doc = upper.getOwnerDocument();
		Element ele = doc.createElement(this.getClass().getSimpleName());
		Attr attr;

		if (op != null) {
			attr = doc.createAttribute("op");
			attr.setValue(op);
			ele.setAttributeNode(attr);
		}

		if (op0 != null) {
			op0.toXML(ele);
		}

		if (op1 != null) {
			op1.toXML(ele);
		}

		upper.appendChild(ele);
	}
}
