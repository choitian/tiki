package tiki.syntax.node;

import tiki.syntax.instruction.Operator;
import tiki.syntax.Analyzer;
import tiki.syntax.Evalue;
import tiki.syntax.SymbolManager;
import tiki.syntax.type.IType;
import tiki.syntax.type.Kind;
import tiki.syntax.type.TypeFactory;

public class exp_BINARY extends Exp {
	public static Operator constructOperator(String baseOp, IType opType) {
		return Operator.valueOf(baseOp + "_" + TypeFactory.getTypeCode(opType));
	}

	private void binaryOperator(Analyzer analyzer, String opName, IType resultType, IType opType, Exp op0, Exp op1) {
		Operator op = constructOperator(opName, opType);
		Evalue result = analyzer.emit(op, resultType, op0.getEvalue(), op1.getEvalue());
		setEvalue(result);
	}

	private boolean binaryOperatorCheck() {
		getOp0().check();
		if (getOp0().getEvalue() == null)
			return false;
		getOp1().check();
		if (getOp1().getEvalue() == null)
			return false;
		return true;
	}

	@Override
	public void doCheck(Analyzer analyzer, SymbolManager symbolManager) {
		if (!binaryOperatorCheck())
			return;

		IType op0_type = getOp0().getEvalue().getType();
		IType op1_type = getOp1().getEvalue().getType();

		// check constraint
		String op = getOp();
		switch (op) {
		case "OR":
		case "AND":
			if (TypeFactory.test(op0_type, Kind.BOOLEAN) && TypeFactory.test(op1_type, Kind.BOOLEAN)) {
				binaryOperator(analyzer, op, TypeFactory.BOOLEAN, TypeFactory.BOOLEAN, getOp0(), getOp1());
			} else {
				String msg = String.format("%s: need both operand be bool type", op);
				analyzer.error(msg, this.getPosition());
			}
			break;
		case "EQUAL":
		case "UNEQUAL":
			if (TypeFactory.isComparableType(op0_type, op1_type)) {
				binaryOperator(analyzer, op, TypeFactory.BOOLEAN, TypeFactory.conversionCommonType(op0_type, op1_type),
						getOp0(), getOp1());
			} else {
				String msg = String.format("%s: need both operand be comparable type", op);
				analyzer.error(msg, this.getPosition());
			}
			break;
		case "LESS":
		case "GREAT":
		case "LESSE":
		case "GREATE":
			if (TypeFactory.test(op0_type, Kind.ARITHMETIC) && TypeFactory.test(op1_type, Kind.ARITHMETIC)) {
				binaryOperator(analyzer, op, TypeFactory.BOOLEAN, TypeFactory.conversionCommonType(op0_type, op1_type),
						getOp0(), getOp1());
			} else {
				String msg = String.format("%s: need both operand be arithmetic type", op);
				analyzer.error(msg, this.getPosition());
			}
			break;
		case "ADD":
			if (TypeFactory.test(op0_type, Kind.ARITHMETIC) && TypeFactory.test(op1_type, Kind.ARITHMETIC)) {
				IType commonType = TypeFactory.conversionCommonType(op0_type, op1_type);
				binaryOperator(analyzer, op, commonType, commonType, getOp0(), getOp1());
			} else if (TypeFactory.test(op0_type, Kind.STRING) || TypeFactory.test(op1_type, Kind.STRING)) {
				IType resultType = analyzer.getTypeClass("String");
				binaryOperator(analyzer, op, resultType, resultType, getOp0(), getOp1());
			} else {
				String msg = String.format("ADD: need both operand be arithmetic type or String");
				analyzer.error(msg, this.getPosition());
			}
			break;
		case "SUB":
		case "MUL":
		case "DIV":
		case "MOD":
			if (TypeFactory.test(op0_type, Kind.ARITHMETIC) && TypeFactory.test(op1_type, Kind.ARITHMETIC)) {

				IType commonType = TypeFactory.conversionCommonType(op0_type, op1_type);

				binaryOperator(analyzer, op, commonType, commonType, getOp0(), getOp1());
			} else {
				String msg = String.format("%s: need both operand be arithmetic type", op);
				analyzer.error(msg, this.getPosition());
			}
			break;
		}
	}
}