package tiki.syntax.scope;

import java.util.Collection;
import java.util.LinkedHashMap;

import tiki.syntax.Analyzer;

public class Scope implements IScope {
	private Analyzer analyzer;
	protected LinkedHashMap<String, Symbol> name2symbolMap;

	private IScope top;

	public Scope() {
		name2symbolMap = new LinkedHashMap<String, Symbol>();
	}

	@Override
	public final Analyzer getAnalyzer() {
		return analyzer;
	}

	@Override
	public final Symbol getSymbol(String name) {
		Symbol self = name2symbolMap.get(name);
		if (self != null) {
			return self;
		}
		if (top != null) {
			return top.getSymbol(name);
		}
		return null;
	}

	@Override
	public final Collection<Symbol> getSymbols() {
		return name2symbolMap.values();
	}

	@Override
	public final IScope getTop() {
		return top;
	}

	@Override
	public void insertSymbol(Symbol symbol) {
		Symbol exist = name2symbolMap.get(symbol.getName());
		if (exist != null) {
			String msg = String.format("%s: already defined in %s,redefinition", exist.getName(), exist.getPosition());
			getAnalyzer().error(msg, symbol.getPosition());
			return;
		}
		symbol.setScope(this);
		this.name2symbolMap.put(symbol.getName(), symbol);
	}

	@Override
	public final void setAnalyzer(Analyzer analyzer) {
		this.analyzer = analyzer;
	}

	@Override
	public final void setTop(IScope top) {
		this.top = top;
	}
}
