package tiki.syntax.node;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import tiki.syntax.instruction.Operator;
import tiki.syntax.Analyzer;
import tiki.syntax.Evalue;
import tiki.syntax.Method;
import tiki.syntax.SymbolManager;
import tiki.syntax.instruction.InstructionBlock;

public class stmt_while extends Stmt {
	Stmt loop_stmt;

	Exp test;

	@Override
	public void doCheck(Analyzer analyzer, SymbolManager symbolManager) {
		Method cm = symbolManager.getCurrentMethod();
		/*
		 * jmp L_test L_loop_stmt: loop_stmt L_test: test_exp L_loop_stmt
		 * L_next:
		 */
		InstructionBlock L_loop_stmt = cm.newInstructionBlock();
		InstructionBlock L_test = cm.newInstructionBlock();
		InstructionBlock L_next = cm.newInstructionBlock();

		cm.continuableStack.push(L_test);
		cm.breakableStack.push(L_next);

		analyzer.emit(Operator.JUMP, Evalue.newConstantString(L_test.getLabel()));
		L_loop_stmt.start();
		loop_stmt.check();
		L_test.start();
		check_as_branch(analyzer, test, L_loop_stmt);
		L_next.start();

		cm.continuableStack.pop();
		cm.breakableStack.pop();
	}

	public void setLoop_stmt(Stmt loop_stmt) {
		this.loop_stmt = loop_stmt;
	}

	public void setTest(Exp test) {
		this.test = test;
	}

	@Override
	public void toXML(Element upper) {
		Document doc = upper.getOwnerDocument();
		Element ele = doc.createElement(this.getClass().getSimpleName());

		test.toXML((Element) ele.appendChild(doc.createElement("test")));

		loop_stmt.toXML((Element) ele.appendChild(doc.createElement("loop_stmt")));

		upper.appendChild(ele);
	}
}