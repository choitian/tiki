package tiki.syntax.node;

import tiki.syntax.instruction.Operator;
import tiki.syntax.Analyzer;
import tiki.syntax.Evalue;
import tiki.syntax.instruction.InstructionBlock;
import tiki.syntax.type.Kind;
import tiki.syntax.type.TypeFactory;

public abstract class Stmt extends SyntaxNode {
	public final void check_as_branch(Analyzer analyzer, Exp exp, InstructionBlock if_true) {
		exp.check();
		if (exp.getEvalue() == null)
			return;

		if (TypeFactory.test(exp.getEvalue().getType(), Kind.BOOLEAN)) {
			analyzer.emit(Operator.TRUEJUMP, Evalue.newConstantString(if_true.getLabel()), exp.getEvalue());
		} else {
			String msg = String.format("Type mismatch: cannot convert from %s to bool",
					exp.getEvalue().getType().description());
			analyzer.error(msg, exp.getPosition());
		}
	}
}