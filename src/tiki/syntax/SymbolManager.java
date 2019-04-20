package tiki.syntax;

import java.util.Stack;

import tiki.syntax.scope.IScope;
import tiki.syntax.scope.Symbol;
import tiki.syntax.type.IType;

public class SymbolManager {
	private static int SymbolTempNO = 0;
	private Analyzer analyzer;
	private IScope currentScope;
	private Stack<PackageScope> packageENV;

	public SymbolManager(Analyzer analyzer) {
		this.analyzer = analyzer;

		this.packageENV = new Stack<PackageScope>();
	}

	public Symbol addSymbol(String name, IType type, boolean isStatic, String position) {
		Symbol symbol = new Symbol(name, type, isStatic, position);
		currentScope.insertSymbol(symbol);
		return symbol;
	}

	public PackageScope currentPackageScope() {
		return packageENV.peek();
	}

	public void enterClassScope(Clazz clazz) {
		enterScope(clazz);
		currentPackageScope().currentClass = clazz;
	}

	public void enterMethodScope(Method method) {
		enterScope(method);
		currentPackageScope().currentMethod = method;
	}

	public void enterScope(IScope scope) {
		scope.setTop(this.currentScope);
		scope.setAnalyzer(this.analyzer);

		this.currentScope = scope;
	}

	public PackageScope entryPackageScope(String name) {
		packageENV.push(new PackageScope(name));
		return packageENV.peek();
	}

	public void exitClassScope() {
		exitScope();
		currentPackageScope().currentClass = null;
	}

	public void exitMethodScope() {
		exitScope();
		currentPackageScope().currentMethod = null;
	}

	public PackageScope exitPackageScope() {
		return packageENV.pop();
	}

	public void exitScope() {
		currentScope = currentScope.getTop();
	}

	public Clazz getCurrentClass() {
		return currentPackageScope().currentClass;
	}

	public Method getCurrentMethod() {
		return currentPackageScope().currentMethod;
	}

	public IScope getCurrentScope() {
		return this.currentScope;
	}

	public Symbol getSymbol(String name) {
		return currentScope.getSymbol(name);
	}

	public Symbol newTemp(IType type) {
		Symbol symbol = new Symbol(String.format("__t__%d", SymbolTempNO++), type, false, "TMP");
		currentScope.insertSymbol(symbol);
		return symbol;
	}
}
