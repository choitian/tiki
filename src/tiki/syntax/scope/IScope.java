package tiki.syntax.scope;

import java.util.Collection;

import tiki.syntax.Analyzer;

public interface IScope {

	Analyzer getAnalyzer();

	Symbol getSymbol(String name);

	Collection<Symbol> getSymbols();

	IScope getTop();

	void insertSymbol(Symbol symbol);

	void setAnalyzer(Analyzer analyzer);

	void setTop(IScope top);

}