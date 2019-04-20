package tiki.syntax.node;

import java.util.LinkedList;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import tiki.syntax.Analyzer;
import tiki.syntax.SymbolManager;
import tiki.syntax.scope.IScope;
import tiki.syntax.scope.ScopeCompound;

public class stmt_compound extends Stmt {
	LinkedList<SyntaxNode> declaration_stmts;
	public IScope scope;

	public void checkAsMethodBody(Analyzer analyzer) {
		if (declaration_stmts != null) {
			for(SyntaxNode item:declaration_stmts)
			{
				item.check();
			}
		}
	}

	@Override
	public void doCheck(Analyzer analyzer, SymbolManager symbolManager) {
		if (declaration_stmts != null) {
			ScopeCompound scope_c = new ScopeCompound();
			symbolManager.enterScope(scope_c);

			for(SyntaxNode item:declaration_stmts)
			{
				item.check();
			}

			symbolManager.exitScope();
		}
	}

	public void setDeclaration_stmts(ListNode declaration_stmts) {
		this.declaration_stmts = declaration_stmts.nodes();
	}

	@Override
	public void toXML(Element upper) {
		Document doc = upper.getOwnerDocument();
		Element ele = doc.createElement(this.getClass().getSimpleName());

		if (declaration_stmts != null) {
			for(SyntaxNode item:declaration_stmts)
			{
				item.toXML(ele);
			}
		}
		upper.appendChild(ele);
	}
}