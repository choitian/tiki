package tiki.syntax.node;

import java.util.LinkedList;
import java.util.Stack;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import tiki.syntax.instruction.Operator;
import tiki.syntax.Analyzer;
import tiki.syntax.Clazz;
import tiki.syntax.Evalue;
import tiki.syntax.SymbolManager;
import tiki.syntax.scope.Symbol;
import tiki.syntax.type.IType;
import tiki.syntax.type.Kind;
import tiki.syntax.type.TypeFactory;
import tiki.syntax.type.TypeMethod;

public class exp_NEW extends Exp {
	ListNode argument_exps;

	decl_specifiers specifiers;

	private void checkArguments(ListNode argument_exp_list, Stack<Exp> arguments) {
		if (argument_exp_list.list != null) {
			checkArguments(argument_exp_list.list, arguments);
		}
		if (argument_exp_list.node != null) {
			Exp value = (Exp) argument_exp_list.node;
			value.check();
			if (value.getEvalue() != null)
				arguments.push(value);
		}
	}

	public static void emitNEW(Analyzer analyzer, Evalue base, int size, LinkedList<Evalue> inis) {
		analyzer.emit(Operator.NEW, base, Evalue.newConstant(String.valueOf(size), TypeFactory.INT));
		if (!inis.isEmpty()) {
			Evalue addr = analyzer.newTempEvalue(TypeFactory.INT);
			analyzer.emit(Operator.MOV, addr, base);
			for (Evalue ini : inis) {
				analyzer.emit(Operator.MOV, Evalue.newReference(addr, ini.getType()), ini);
				analyzer.emit(Operator.ADD_I, addr, addr, Evalue.ONE);
			}
		}
	}

	@Override
	public void doCheck(Analyzer analyzer, SymbolManager symbolManager) {
		specifiers.check();

		if (!TypeFactory.test(specifiers.type, Kind.CLASS)) {
			String msg = String.format("%s: is not Class Type", specifiers.type.description());
			analyzer.error(msg, this.getPosition());
			return;
		}
		IType typeClass = specifiers.type;
		String className = typeClass.getName();

		Clazz clazz = analyzer.getDefinedClass(className);
		if (clazz == null) {
			String msg = String.format("%s cannot be resolved to a type", className);
			analyzer.error(msg, this.getPosition());
			return;
		}

		Evalue baseEvalue = Evalue.newVariable(symbolManager.newTemp(typeClass).getOffset(), typeClass);
		int nonstatics = clazz.getNonstatics().size();

		LinkedList<Evalue> iniEvalues = new LinkedList<Evalue>();
		for (Symbol f : clazz.getNonstatics()) {
			iniEvalues.add(Evalue.defaultValue(f.getType()));
		}

		emitNEW(analyzer, baseEvalue, nonstatics, iniEvalues);

		// check construct
		Stack<Exp> arguments = new Stack<Exp>();
		if (argument_exps != null)
			checkArguments(argument_exps, arguments);

		Symbol construct = clazz.getConstruct();
		if (construct != null) {

			Evalue methodEvalue = exp_SCOPE_NAME.staticMember(analyzer, typeClass.getName(), construct,
					this.getPosition());
			methodEvalue.setExtra(baseEvalue);

			TypeMethod tm = ((TypeMethod) construct.getType());
			Symbol tresult = symbolManager.newTemp(tm.getBase());
			Evalue result = Evalue.newVariable(tresult.getOffset(), TypeFactory.INT);

			if (!exp_INVOKE.check_arguments(analyzer, tm, methodEvalue, arguments, result)) {
				String msg = String.format("The construct %s%s is not applicable for the arguments%s", className,
						tm.description(), exp_INVOKE.argumentsTypeName(arguments));
				analyzer.error(msg, this.getPosition());
			}
		} else {
			if (!arguments.isEmpty()) {
				String msg = String.format("The constructor %s %s is undefined", className,
						exp_INVOKE.argumentsTypeName(arguments));
				analyzer.error(msg, this.getPosition());
				return;
			}
		}

		setEvalue(baseEvalue);
	}

	public void setArgument_exps(ListNode argument_exps) {
		this.argument_exps = argument_exps;
	}

	public void setSpecifiers(decl_specifiers specifiers) {
		this.specifiers = specifiers;
	}

	@Override
	public void toXML(Element upper) {
		Document doc = upper.getOwnerDocument();
		Element ele = doc.createElement(this.getClass().getSimpleName());
		specifiers.toXML(ele);
		if (argument_exps != null)
			argument_exps.toXML(ele);

		upper.appendChild(ele);
	}
}