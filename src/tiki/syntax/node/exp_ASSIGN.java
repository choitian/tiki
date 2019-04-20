package tiki.syntax.node;

import tiki.syntax.instruction.Operator;
import tiki.syntax.Analyzer;
import tiki.syntax.Evalue;
import tiki.syntax.SymbolManager;
import tiki.syntax.type.IType;
import tiki.syntax.type.TypeFactory;

public class exp_ASSIGN extends Exp {
	@Override
	public void doCheck(Analyzer analyzer, SymbolManager symbolManager) {
		getOp0().check();
		if (getOp0().getEvalue() == null)
			return;

		getOp1().check();
		if (getOp1().getEvalue() == null)
			return;

		Evalue to = getOp0().getEvalue();
		Evalue from = getOp1().getEvalue();

		if (!to.isLvalue()) {
			String msg = String.format("The left-hand side of an assignment must be a variable");
			analyzer.error(msg, this.getPosition());
			return;
		}
		IType toType = to.getType();
		IType fromType = from.getType();
		// check constraint
		if (TypeFactory.isAssignableType(toType, fromType)) {

			analyzer.emit(Operator.MOV, to, from);

			setEvalue(analyzer.emit(Operator.MOV, toType, to));
		} else {
			String msg = String.format("Type mismatch: cannot convert from %s to %s", fromType.description(),
					toType.description());
			analyzer.error(msg, this.getPosition());
			return;
		}
	}
}