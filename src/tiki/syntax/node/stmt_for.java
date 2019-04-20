package tiki.syntax.node;

import org.w3c.dom.Element;

import tiki.syntax.Analyzer;
import tiki.syntax.SymbolManager;

public class stmt_for extends Stmt {
	ListNode ini_exp;

	Stmt loop_stmt;

	ListNode reini_exp;

	ListNode test;

	public void iniToListNode(ListNode exp) {
		if (exp.list != null) {
			iniToListNode(exp.list);
		}
		if (exp.node != null)
		{
			if(exp.node instanceof Exp)
			{
				stmt_exp se = new stmt_exp();
				se.setTree(getTree());
				se.setPosition(getPosition());
				se.setExp((Exp) exp.node);	
				exp.node = se;
			}
		}
	}
	public SyntaxNode transform()
	{
		stmt_while sw = new stmt_while();
		sw.setTree(getTree());
		sw.setPosition(getPosition());
		if (test.node != null)
			sw.setTest((Exp) test.node);
		
		stmt_compound sc_loop_stmt = new stmt_compound();
		sc_loop_stmt.setTree(getTree());
		sc_loop_stmt.setPosition(getPosition());
		sc_loop_stmt.setDeclaration_stmts(new ListNode(loop_stmt,new stmt_exp((Exp) reini_exp.node)));
		sw.setLoop_stmt(sc_loop_stmt);
		
		
		stmt_compound sc_outer = new stmt_compound();
		sc_outer.setTree(getTree());
		sc_outer.setPosition(getPosition());		
		
		iniToListNode(ini_exp);
		sc_outer.setDeclaration_stmts(new ListNode(ini_exp,sw));
		
		return sc_outer;
	}

	public void setIni(ListNode test) {
		this.ini_exp = test;
	}

	public void setLoop_stmt(Stmt loop_stmt) {
		this.loop_stmt = loop_stmt;
	}

	public void setReini(ListNode test) {
		this.reini_exp = test;
	}

	public void setTest(ListNode test) {
		this.test = test;
	}
	@Override
	void doCheck(Analyzer analyzer, SymbolManager symbolManager) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void toXML(Element upper) {
		// TODO Auto-generated method stub
		
	}
}