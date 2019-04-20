package tiki.syntax.node;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import tiki.syntax.instruction.Operator;
import tiki.syntax.Analyzer;
import tiki.syntax.Evalue;
import tiki.syntax.Method;
import tiki.syntax.SymbolManager;
import tiki.syntax.instruction.InstructionBlock;

public class stmt_if extends Stmt {
	Stmt false_stmt;

	Exp test;

	Stmt true_stmt;

	@Override
	public void doCheck(Analyzer analyzer, SymbolManager symbolManager) {
		Method cm = symbolManager.getCurrentMethod();
		/*
		 * jmp L_test L_true_stmt: true_stmt jmp L_next L_test: test_exp
		 * L_true_stmt false_stmt L_next:
		 */
		InstructionBlock L_true_stmt = cm.newInstructionBlock();
		InstructionBlock L_test = cm.newInstructionBlock();
		InstructionBlock L_next = cm.newInstructionBlock();

		analyzer.emit(Operator.JUMP, Evalue.newConstantString(L_test.getLabel()));
		L_true_stmt.start();
		true_stmt.check();
		analyzer.emit(Operator.JUMP, Evalue.newConstantString(L_next.getLabel()));
		L_test.start();
		check_as_branch(analyzer, test, L_true_stmt);
		if (false_stmt != null) {
			false_stmt.check();
		}

		L_next.start();
	}

	public void setFalse_stmt(Stmt false_stmt) {
		this.false_stmt = false_stmt;
	}

	public void setTest(Exp test) {
		this.test = test;
	}

	public void setTrue_stmt(Stmt true_stmt) {
		this.true_stmt = true_stmt;
	}

	@Override
	public void toXML(Element upper) {
		Document doc = upper.getOwnerDocument();
		Element ele = doc.createElement(this.getClass().getSimpleName());

		test.toXML((Element) ele.appendChild(doc.createElement("test")));

		true_stmt.toXML((Element) ele.appendChild(doc.createElement("true_stmt")));

		if (false_stmt != null) {
			false_stmt.toXML((Element) ele.appendChild(doc.createElement("false_stmt")));
		}

		upper.appendChild(ele);
	}
}