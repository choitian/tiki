package tiki.syntax.node;

import tiki.syntax.instruction.Operator;
import tiki.syntax.Analyzer;
import tiki.syntax.Evalue;
import tiki.syntax.SymbolManager;
import tiki.syntax.type.Kind;
import tiki.syntax.type.TypeFactory;

public class exp_NOT extends Exp {

	@Override
	public void doCheck(Analyzer analyzer, SymbolManager symbolManager) {
		getOp0().check();
		if (getOp0().getEvalue() == null)
			return;

		// check constraint
		if (TypeFactory.test(getOp0().getEvalue().getType(), Kind.BOOLEAN)) {
			Evalue result = analyzer.emit(Operator.NOT, TypeFactory.BOOLEAN, getOp0().getEvalue());
			setEvalue(result);
		} else {
			String msg = String.format("NOT: need operand be bool type");
			analyzer.error(msg, this.getPosition());
		}
	}
}