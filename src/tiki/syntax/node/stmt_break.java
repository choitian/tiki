package tiki.syntax.node;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import tiki.syntax.instruction.Operator;
import tiki.syntax.Analyzer;
import tiki.syntax.Evalue;
import tiki.syntax.Method;
import tiki.syntax.SymbolManager;

public class stmt_break extends Stmt {
	@Override
	public void doCheck(Analyzer analyzer, SymbolManager symbolManager) {
		Method cm = symbolManager.getCurrentMethod();
		if (cm.breakableStack.empty()) {
			String msg = String.format("break cannot be used outside of a loop");
			analyzer.error(msg, this.getPosition());
		} else {
			String label = cm.breakableStack.peek().getLabel();
			analyzer.emit(Operator.JUMP, Evalue.newConstantString(label));
		}
	}

	@Override
	public void toXML(Element upper) {
		Document doc = upper.getOwnerDocument();
		Element ele = doc.createElement(this.getClass().getSimpleName());

		upper.appendChild(ele);
	}
}