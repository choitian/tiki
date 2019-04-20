package tiki.syntax.node;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import tiki.syntax.instruction.Operator;
import tiki.syntax.Analyzer;
import tiki.syntax.Evalue;
import tiki.syntax.Method;
import tiki.syntax.SymbolManager;
import tiki.syntax.scope.IScope;
import tiki.syntax.scope.ScopeCompound;
import tiki.syntax.scope.Symbol;
import tiki.syntax.type.IType;
import tiki.syntax.type.Kind;
import tiki.syntax.type.TypeFactory;

public class decl_declaration extends Decl {
	decl_declarator declarator;

	decl_initializer initializer;

	decl_specifiers specifiers;
	@Override
	public void doCheck(Analyzer analyzer, SymbolManager symbolManager) {
		specifiers.check();

		declarator.setBaseType(specifiers.type);
		declarator.check();
		IType type = declarator.getResultType();

		String name = declarator.id;
		boolean isStatic = specifiers.isStatic;
		String position = this.getPosition();

		if (TypeFactory.test(type, Kind.VOID)) {
			String msg = "void is invalid type";
			analyzer.error(msg, this.getPosition());
			return;
		}
		Symbol symbol = symbolManager.addSymbol(name, type, isStatic, position);

		if (initializer != null) {

			IScope sc = symbolManager.getCurrentScope();
			if (!(sc instanceof Method) && !(sc instanceof ScopeCompound)) {

				String msg = String.format("%s: can not be initialized", name);
				analyzer.error(msg, this.getPosition());
				return;
			}
			initializer.setDefinedType(symbol.getType());
			initializer.check();
			if (initializer.getEvalue() == null)
				return;
			if (symbol != null) {
				Evalue ev = Evalue.newVariable(symbol.getOffset(), symbol.getType());
				ev.setLvalue(true);

				Evalue to = ev;
				Evalue from = initializer.getEvalue();
				if (!to.isLvalue()) {
					String msg = String.format("The left-hand side of an assignment must be a variable");
					analyzer.error(msg, position);
					return;
				}
				IType toType = to.getType();
				IType fromType = from.getType();
				// check constraint
				if (TypeFactory.isAssignableType(toType, fromType)) {
					analyzer.emit(Operator.MOV, to, from);
				} else {
					String msg = String.format("Type mismatch: cannot convert from %s to %s", fromType.description(),
							toType.description());
					analyzer.error(msg, position);
					return;
				}
			}
		}
	}

	public void setDeclarator(decl_declarator declarator) {
		this.declarator = declarator;
	}
	public void setInitializer(decl_initializer initializer) {
		this.initializer = initializer;
	}
	
	public void setSpecifiers(decl_specifiers specifiers) {
		this.specifiers = specifiers;
	}

	@Override
	public void toXML(Element upper) {
		Document doc = upper.getOwnerDocument();
		Element ele = doc.createElement(this.getClass().getSimpleName());

		if (specifiers != null)
			specifiers.toXML(ele);
		if (declarator != null)
			declarator.toXML(ele);
		if (initializer != null)
			initializer.toXML(ele);
		upper.appendChild(ele);
	}
}