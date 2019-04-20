package tiki.syntax.node;

import tiki.syntax.Analyzer;
import tiki.syntax.SymbolManager;

public class exp_COMMA extends Exp {

	@Override
	public void doCheck(Analyzer analyzer, SymbolManager symbolManager) {
		getOp0().check();
		if (getOp0().getEvalue() == null)
			return;
		getOp1().check();
		if (getOp1().getEvalue() == null)
			return;

		setEvalue(getOp1().getEvalue());
	}
}