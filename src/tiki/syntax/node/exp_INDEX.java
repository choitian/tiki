package tiki.syntax.node;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import tiki.syntax.instruction.Operator;
import tiki.syntax.Analyzer;
import tiki.syntax.Evalue;
import tiki.syntax.SymbolManager;
import tiki.syntax.type.IType;
import tiki.syntax.type.Kind;
import tiki.syntax.type.TypeFactory;

public class exp_INDEX extends Exp {
	Exp base;

	Exp index;

	@Override
	public void doCheck(Analyzer analyzer, SymbolManager symbolManager) {
		base.check();
		if (base.getEvalue() == null)
			return;

		if (!TypeFactory.test(base.getEvalue().getType(), Kind.ARRAY)) {
			String msg = String.format("left expression can not resolve to a array");
			analyzer.error(msg, this.getPosition());
			return;
		}

		index.check();
		if (index.getEvalue() == null)
			return;

		if (!TypeFactory.test(index.getEvalue().getType(), Kind.INT)) {
			String msg = String.format("Type mismatch: cannot convert from %s to int",
					index.getEvalue().getType().description());
			analyzer.error(msg, this.getPosition());
			return;
		}
		IType elementType = TypeFactory.getArrayElementType(base.getEvalue().getType());

		Evalue tempEv = analyzer.newTempEvalue(TypeFactory.INT);
		analyzer.emit(Operator.REF, tempEv, base.getEvalue(), index.getEvalue(),Evalue.newConstantString(this.getPosition()));
		Evalue resultReference = Evalue.newReference(tempEv, elementType);
		setEvalue(resultReference);

		Evalue result = this.getEvalue();
		if (result != null) {
			result.setLvalue(true);
		}
	}

	public void setBase(Exp base) {
		this.base = base;
	}

	public void setIndex(Exp index) {
		this.index = index;
	}

	@Override
	public void toXML(Element upper) {
		Document doc = upper.getOwnerDocument();
		Element ele = doc.createElement(this.getClass().getSimpleName());
		base.toXML(ele);
		index.toXML(ele);

		upper.appendChild(ele);
	}
}