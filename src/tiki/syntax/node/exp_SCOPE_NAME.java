package tiki.syntax.node;

import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import tiki.syntax.instruction.Operator;
import tiki.syntax.Analyzer;
import tiki.syntax.Clazz;
import tiki.syntax.Evalue;
import tiki.syntax.Method;
import tiki.syntax.SymbolManager;
import tiki.syntax.scope.Symbol;
import tiki.syntax.type.IType;
import tiki.syntax.type.Kind;
import tiki.syntax.type.TypeFactory;
import tiki.uitls.PackagenameUtils;

public class exp_SCOPE_NAME extends Exp {
	public static Evalue checkField(Analyzer analyzer, Evalue baseEvalue, String name, String position) {
		if (baseEvalue == null)
			return null;

		IType baseType = baseEvalue.getType();
		IType typeClass = null;
		if (TypeFactory.test(baseType, Kind.ARRAY)) {
			switch (name) {
			case "length": {
				Evalue tempEv = analyzer.newTempEvalue(TypeFactory.INT);
				analyzer.emit(Operator.REF, tempEv, baseEvalue, Evalue.NEGONE,Evalue.newConstantString(position));
				return Evalue.newReference(tempEv, TypeFactory.INT);
			}
			default:
				String msg = String.format("%s is not a member", name);
				analyzer.error(msg, position);
				return null;
			}
		}
		if (TypeFactory.test(baseType, Kind.CLASS)) {
			typeClass = baseType;
		}
		if (typeClass == null) {
			String msg = String.format("left expression can not resolve to a class");
			analyzer.error(msg, position);
			return null;
		}
		Clazz clazz = analyzer.getDefinedClass(typeClass.getName());
		if (clazz == null) {
			String msg = String.format("%s cannot be resolved to a type", typeClass.getName());
			analyzer.error(msg, position);
			return null;
		}

		Symbol member = clazz.getMember(name);
		if (member == null) {
			String msg = String.format("%s is not a member", name);
			analyzer.error(msg, position);
			return null;
		}

		Evalue result;
		if (baseEvalue.isStaticScope()) {
			if (!member.isStatic()) {
				String msg = String.format("Cannot make a static reference to the non-static member '%s'",
						member.getName());
				analyzer.error(msg, position);
				return null;
			}

			Evalue tempEv = analyzer.newTempEvalue(TypeFactory.INT);
			analyzer.emit(Operator.REF, tempEv, baseEvalue,
					Evalue.newConstant(String.valueOf(member.getOffset()), TypeFactory.INT),Evalue.newConstantString(position));

			Evalue resultAddress = Evalue.newReference(tempEv, member.getType());
			result = resultAddress;
		} else {
			Evalue resultAddress;
			if (member.isStatic()) {
				String msg = String.format("The static member '%s' of %s should be accessed in a static way",
						member.getName(), typeClass.getName());
				analyzer.warning(msg, position);

				resultAddress = staticMember(analyzer, typeClass.getName(), member, position);
			} else {
				if (TypeFactory.test(member.getType(), Kind.METHOD)) {
					resultAddress = staticMember(analyzer, typeClass.getName(), member, position);
					resultAddress.setExtra(baseEvalue);
				} else {
					Evalue tempEv = analyzer.newTempEvalue(TypeFactory.INT);
					analyzer.emit(Operator.REF, tempEv, baseEvalue,
							Evalue.newConstant(String.valueOf(member.getOffset()), TypeFactory.INT),Evalue.newConstantString(position));

					resultAddress = Evalue.newReference(tempEv, member.getType());
				}
			}
			result = resultAddress;
		}

		if (result != null) {
			result.setLvalue(true);
		}
		return result;
	}

	public static Evalue checkVariableBase(Analyzer analyzer, Symbol symbol, String position) {
		boolean isStaticContext = analyzer.getSymbolManager().getCurrentMethod().isStatic();

		Evalue result;
		if (symbol.getScope() instanceof Clazz) {
			Clazz sc = (Clazz) symbol.getScope();
			if (symbol.isStatic()) {
				Evalue resultAddress = staticMember(analyzer, sc.getClassName(), symbol, position);
				result = resultAddress;
			} else {
				if (isStaticContext) {
					String msg = String.format("Cannot make a static reference to the non-static member '%s'",
							symbol.getName());
					analyzer.error(msg, position);
					return null;
				}
				Symbol thisObject = analyzer.getSymbolManager().getSymbol(Method.THIS);
				Evalue resultAddress;
				if (TypeFactory.test(symbol.getType(), Kind.METHOD)) {
					resultAddress = staticMember(analyzer, sc.getClassName(), symbol, position);
					resultAddress.setExtra(Evalue.newVariable(thisObject.getOffset(), thisObject.getType()));
				} else {
					Evalue tempEv = analyzer.newTempEvalue(TypeFactory.INT);
					analyzer.emit(Operator.REF, tempEv, Evalue.newVariable(thisObject.getOffset(), TypeFactory.INT),
							Evalue.newConstant(String.valueOf(symbol.getOffset()), TypeFactory.INT),Evalue.newConstantString(position));
					resultAddress = Evalue.newReference(tempEv, symbol.getType());
				}
				result = resultAddress;
			}
		} else {
			if (isStaticContext && symbol.getName().equals(Method.THIS)) {
				String msg = String.format("Cannot use 'this' in a static context");
				analyzer.error(msg, position);
				return null;
			}
			result = Evalue.newVariable(symbol.getOffset(), symbol.getType());
		}
		if (result != null) {
			result.setLvalue(true);
		}
		return result;
	}

	public static Evalue staticMember(Analyzer analyzer, String className, Symbol member, String position) {
		Evalue baseEvalue = Evalue.newClass(className, TypeFactory.INT);

		Evalue tempEv = analyzer.newTempEvalue(TypeFactory.INT);
		analyzer.emit(Operator.REF, tempEv, baseEvalue,
				Evalue.newConstant(String.valueOf(member.getOffset()), TypeFactory.INT),Evalue.newConstantString(position));

		return Evalue.newReference(tempEv, member.getType());
	}

	private String id;

	exp_SCOPE_NAME list;

	@Override
	void doCheck(Analyzer analyzer, SymbolManager symbolManager) {
		ArrayList<String> ids = ids();
		String baseName = ids.get(0);

		Symbol symbol = symbolManager.getSymbol(baseName);
		List<String> subName = null;
		Evalue baseEvalue;
		if (symbol != null) {
			subName = ids.subList(1, ids.size());
			baseEvalue = checkVariableBase(analyzer, symbol, this.getPosition());
		} else {
			IType typeClass = null;
			for (int i = 1; i < ids.size(); i++) {
				String pName = PackagenameUtils.link(ids.subList(0, i), ".");
				typeClass = analyzer.getTypeClass(pName);
				if (typeClass != null) {
					subName = ids.subList(i, ids.size());
					break;
				}
			}
			if (typeClass == null) {
				String msg = String.format("%s: cannot be resolved to a variable", baseName);
				analyzer.error(msg, this.getPosition());
				return;
			}
			baseEvalue = Evalue.newClass(typeClass.getName(), typeClass);
			baseEvalue.setStaticScope(true);
		}

		if (subName == null) {
			this.setEvalue(baseEvalue);
		} else {
			for (String sub : subName) {
				baseEvalue = checkField(analyzer, baseEvalue, sub, this.getPosition());
			}
			this.setEvalue(baseEvalue);
		}

	}

	public String getName() {
		return PackagenameUtils.link(this.ids(), ".");
	}

	private ArrayList<String> ids() {
		ArrayList<String> result = new ArrayList<String>();
		walk(result);
		return result;
	}

	public void setId(String id) {
		this.id = id;
	}

	public void setList(exp_SCOPE_NAME list) {
		this.list = list;
	}

	@Override
	public void toXML(Element upper) {
		Document doc = upper.getOwnerDocument();
		Element ele = doc.createElement(this.getClass().getSimpleName());

		ele.setTextContent(getName());
		upper.appendChild(ele);
	}

	private void walk(ArrayList<String> result) {
		if (list != null) {
			list.walk(result);
		}
		if (id != null) {
			result.add(id);
		}
	}
}
