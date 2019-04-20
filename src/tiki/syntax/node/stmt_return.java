package tiki.syntax.node;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import tiki.syntax.instruction.Operator;
import tiki.syntax.Analyzer;
import tiki.syntax.Evalue;
import tiki.syntax.Method;
import tiki.syntax.SymbolManager;
import tiki.syntax.type.IType;
import tiki.syntax.type.Kind;
import tiki.syntax.type.TypeFactory;

public class stmt_return extends Stmt {
	Exp value;

	@Override
	public void doCheck(Analyzer analyzer, SymbolManager symbolManager) {
		Method cm = symbolManager.getCurrentMethod();
		IType return_type = cm.getType().getBase();

		IType exp_type = TypeFactory.VOID;
		if (value != null) {
			value.check();
			if (value.getEvalue() == null)
				return;

			exp_type = value.getEvalue().getType();

			if (TypeFactory.isAssignableType(return_type, exp_type)) {
				analyzer.emit(Operator.RETV, value.getEvalue());
				analyzer.emit(Operator.JUMP, Evalue.newConstantString(cm.end.getLabel()));
			} else {
				String msg = String.format("Type mismatch: cannot convert from %s to %s", exp_type.description(),
						return_type.description());
				analyzer.error(msg, this.getPosition());
				return;
			}
		} else {
			if (TypeFactory.test(return_type, Kind.VOID)) {
				analyzer.emit(Operator.RETV);
				analyzer.emit(Operator.JUMP, Evalue.newConstant(cm.end.getLabel(), TypeFactory.VOID));
			} else {
				String msg = String.format("This method must return a result of %s", return_type.description());
				analyzer.error(msg, this.getPosition());
				return;
			}
		}
	}

	public void setValue(Exp value) {
		this.value = value;
	}

	@Override
	public void toXML(Element upper) {
		Document doc = upper.getOwnerDocument();
		Element ele = doc.createElement(this.getClass().getSimpleName());

		if (value != null) {
			value.toXML(ele);
		}

		upper.appendChild(ele);
	}
}