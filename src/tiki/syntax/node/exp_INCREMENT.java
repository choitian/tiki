package tiki.syntax.node;

import tiki.syntax.instruction.Operator;
import tiki.syntax.Analyzer;
import tiki.syntax.Evalue;
import tiki.syntax.SymbolManager;
import tiki.syntax.type.IType;
import tiki.syntax.type.Kind;
import tiki.syntax.type.TypeFactory;

public class exp_INCREMENT extends Exp {

	@Override
	public void doCheck(Analyzer analyzer, SymbolManager symbolManager) {
		String op = this.getOp();
		getOp0().check();
		if (getOp0().getEvalue() == null) {
			String msg = String.format("%s: The operand can not resolve to address", op);
			analyzer.error(msg, this.getPosition());
			return;
		}

		if (!getOp0().getEvalue().isLvalue()) {
			String msg = String.format("%s: The operand is not assignable", op);
			analyzer.error(msg, this.getPosition());
			return;
		}
		// check constraint
		if (TypeFactory.test(getOp0().getEvalue().getType(), Kind.INT)) {
			IType resultType = TypeFactory.INT;
			Evalue result = analyzer.newTempEvalue(resultType);
			Evalue op0Evalue = getOp0().getEvalue();

			switch (op) {
			case "INC":
				analyzer.emit(Operator.ADD_I, op0Evalue, op0Evalue, Evalue.ONE);
				analyzer.emit(Operator.MOV, result, op0Evalue);
				break;
			case "DEC":
				analyzer.emit(Operator.SUB_I, op0Evalue, op0Evalue, Evalue.ONE);
				analyzer.emit(Operator.MOV, result, op0Evalue);
				break;
			case "POST_INC":
				analyzer.emit(Operator.MOV, result, op0Evalue);
				analyzer.emit(Operator.ADD_I, op0Evalue, op0Evalue, Evalue.ONE);
				break;
			case "POST_DEC":
				analyzer.emit(Operator.MOV, result, op0Evalue);
				analyzer.emit(Operator.SUB_I, op0Evalue, op0Evalue, Evalue.ONE);
				break;
			}
			setEvalue(result);
		} else {
			String msg = String.format("%s: operand is not int type", op);
			analyzer.error(msg, this.getPosition());
		}
	}
}