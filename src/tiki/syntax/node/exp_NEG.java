package tiki.syntax.node;

import tiki.syntax.instruction.Operator;
import tiki.syntax.Analyzer;
import tiki.syntax.Evalue;
import tiki.syntax.SymbolManager;
import tiki.syntax.type.IType;
import tiki.syntax.type.Kind;
import tiki.syntax.type.TypeFactory;

public class exp_NEG extends Exp {

	@Override
	public void doCheck(Analyzer analyzer, SymbolManager symbolManager) {
		getOp0().check();
		if (getOp0().getEvalue() == null)
			return;
		// check constraint
		if (TypeFactory.test(getOp0().getEvalue().getType(), Kind.ARITHMETIC)) {
			IType resultType = TypeFactory.test(getOp0().getEvalue().getType(), Kind.FLOAT) ? TypeFactory.FLOAT
					: TypeFactory.INT;
			Operator op = TypeFactory.test(getOp0().getEvalue().getType(), Kind.FLOAT) ? Operator.SUB_F
					: Operator.SUB_I;
			Evalue result = analyzer.emit(op, resultType, Evalue.ZERO, getOp0().getEvalue());
			setEvalue(result);
		} else {
			String msg = String.format("NEG: need operand be arithmetic type");
			analyzer.error(msg, this.getPosition());
		}
	}
}