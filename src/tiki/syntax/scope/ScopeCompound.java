package tiki.syntax.scope;

import tiki.syntax.Method;

public class ScopeCompound extends Scope {
	private Method getScopeMethod(IScope scope) {
		if (scope == null)
			return null;
		if (scope instanceof Method)
			return (Method) scope;
		return getScopeMethod(scope.getTop());
	}

	@Override
	public void insertSymbol(Symbol symbol) {
		super.insertSymbol(symbol);
		Method sm = getScopeMethod(this.getTop());
		sm.addLocal(symbol);
	}
}
