package tiki.syntax.node;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Stack;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import tiki.syntax.instruction.Operator;
import tiki.syntax.Analyzer;
import tiki.syntax.Evalue;
import tiki.syntax.SymbolManager;
import tiki.syntax.scope.Symbol;
import tiki.syntax.type.IType;
import tiki.syntax.type.Kind;
import tiki.syntax.type.TypeFactory;
import tiki.syntax.type.TypeMethod;

public class exp_INVOKE extends Exp {
	public static String argumentsTypeName(Stack<Exp> arguments) {
		StringBuffer sb = new StringBuffer();

		for (Exp exp : arguments) {
			if (sb.length() != 0) {
				sb.append(", ");
			}
			sb.append(String.format("%s", exp.getEvalue().getType().description()));
		}

		return String.format("(%s)", sb.toString());
	}

	public static boolean check_arguments(Analyzer analyzer, TypeMethod typeMethod, Evalue base, Stack<Exp> arguments,
			Evalue result) {
		Collection<Symbol> parameters = typeMethod.getSymbols();
		boolean variable_length = typeMethod.isVariableLength();

		int explicit_parameters_size = parameters.size() - (variable_length ? 1 : 0);
		if ((arguments.size() < explicit_parameters_size)
				|| (arguments.size() > explicit_parameters_size && !variable_length)) {
			return false;
		}

		analyzer.emit(Operator.ESP,result);

		int vararg_length = arguments.size() - explicit_parameters_size;
		if (vararg_length > 0) {
			if (!check_implicit_parameters(analyzer, arguments, typeMethod.getVarargElementType(), vararg_length))
				return false;
		}

		if (!check_explicit_parameters(analyzer, arguments, parameters, explicit_parameters_size))
			return false;

		Evalue thisObject = base.getExtra();
		analyzer.emit(Operator.INVOKE, thisObject == null ? Evalue.NULL : thisObject, base);

		analyzer.emit(Operator.BSP);

		return true;
	}

	public static boolean check_explicit_parameters(Analyzer analyzer, Stack<Exp> arguments,
			Collection<Symbol> parameters, int explicit_parameters_size) {

		Stack<Evalue> results = new Stack<Evalue>();

		Iterator<Exp> it_arguments = arguments.iterator();
		Iterator<Symbol> it_parameters = parameters.iterator();
		for (int index = 0; index < explicit_parameters_size; index++) {
			Exp argument = it_arguments.next();
			Symbol parameter = it_parameters.next();
			IType argument_type = argument.getEvalue().getType();
			IType parameter_type = parameter.getType();

			if (TypeFactory.isAssignableType(parameter_type, argument_type)) {
				results.push(argument.getEvalue());
			} else {
				return false;
			}
		}
		while (!results.isEmpty()) {
			analyzer.emit(Operator.ARG, results.pop());
		}
		return true;
	}

	public static boolean check_implicit_parameters(Analyzer analyzer, Stack<Exp> arguments, IType varargElementType,
			int vararg_length) {
		LinkedList<Evalue> varargs = new LinkedList<Evalue>();

		int index = arguments.size() - 1;
		while (vararg_length-- > 0) {
			// varargs: var0,var1,var2,var3
			Exp argument = arguments.get(index--);
			IType argument_type = argument.getEvalue().getType();

			if (TypeFactory.isAssignableType(varargElementType, argument_type)) {
				varargs.addFirst(argument.getEvalue());
			} else {
				return false;
			}
		}
		analyzer.emit(Operator.VARARGS, varargs.toArray(new Evalue[0]));
		return true;
	}

	ListNode argument_exps;

	Exp base;

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

	@Override
	public void doCheck(Analyzer analyzer, SymbolManager symbolManager) {
		base.check();
		if (base.getEvalue() == null)
			return;

		Evalue methodEvalue = base.getEvalue();
		IType baseType = methodEvalue.getType();

		if (!TypeFactory.test(baseType, Kind.METHOD)) {
			String msg = String.format("expression can not resolve to a method");
			analyzer.error(msg, this.getPosition());
			return;
		}

		TypeMethod typeMethod = (TypeMethod) baseType;
		IType returnType = typeMethod.getBase();
		Evalue result;
		if (TypeFactory.test(returnType, Kind.VOID)) {
			result = Evalue.NULL;
		} else {
			result = analyzer.newTempEvalue(returnType);
		}

		Stack<Exp> arguments = new Stack<Exp>();
		if (argument_exps != null)
			checkArguments(argument_exps, arguments);

		if (!check_arguments(analyzer, typeMethod, methodEvalue, arguments, result)) {
			String msg = String.format("The method %s is not applicable for the arguments%s", typeMethod.description(),
					argumentsTypeName(arguments));
			analyzer.error(msg, this.getPosition());
		}

		setEvalue(result);
	}

	public void setArgument_exps(ListNode argument_exps) {
		this.argument_exps = argument_exps;
	}

	public void setBase(Exp base) {
		this.base = base;
	}

	@Override
	public void toXML(Element upper) {
		Document doc = upper.getOwnerDocument();
		Element ele = doc.createElement(this.getClass().getSimpleName());

		if (base != null)
			base.toXML((Element) ele.appendChild(doc.createElement("base")));

		if (argument_exps != null)
			argument_exps.toXML(ele);

		upper.appendChild(ele);
	}
}