package tiki.syntax.node;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedList;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import tiki.syntax.instruction.Instruction;
import tiki.syntax.instruction.Operator;
import tiki.syntax.Analyzer;
import tiki.syntax.Clazz;
import tiki.syntax.Method;
import tiki.syntax.SymbolManager;
import tiki.syntax.instruction.Address;
import tiki.syntax.instruction.InstructionBlock;
import tiki.syntax.scope.Symbol;
import tiki.syntax.type.IType;
import tiki.syntax.type.Kind;
import tiki.syntax.type.TypeFactory;
import tiki.syntax.type.TypeMethod;

public class decl_method_definition extends Decl {
	Clazz clazz;

	stmt_compound compound_stmt;

	decl_declarator declarator;

	Symbol method;

	Method scopeMethod;

	decl_specifiers specifiers;

	IType type;

	public void checkBody(Analyzer analyzer, SymbolManager symbolManager) {
		TypeMethod tm = (TypeMethod) type;

		scopeMethod = new Method(symbolManager.getCurrentClass(), method);
		symbolManager.enterMethodScope(scopeMethod);
		scopeMethod.initializeParameter(analyzer, tm.getSymbols());

		scopeMethod.begin.start();
		scopeMethod.body_block.start();
		compound_stmt.checkAsMethodBody(analyzer);
		scopeMethod.end.start();

		prologue(analyzer);
		epilogue(analyzer);

		symbolManager.exitMethodScope();

		completion();
	}

	private void completion() {
		ArrayList<Instruction> jumps = new ArrayList<Instruction>();
		LinkedHashMap<String, Integer> name2blockMap = new LinkedHashMap<String, Integer>();

		Collection<Instruction> instructions = clazz.newMethodEntry(method.getName());
		for (InstructionBlock blocks : scopeMethod.ic.getBlocks()) {
			name2blockMap.put(blocks.getLabel(), instructions.size());
			for (Instruction ins : blocks.getInstructions()) {
				ins.setOrdinal(instructions.size());
				instructions.add(ins);

				if (ins.getOperator().equals(Operator.JUMP) || ins.getOperator().equals(Operator.TRUEJUMP)) {
					jumps.add(ins);
				}
			}
		}

		for (Instruction jump : jumps) {
			int ordinalOfTarget = name2blockMap.get(jump.getOperand(0).getValue());
			int ordinalOfJump = jump.getOrdinal();

			jump.setOperand(0, Address.newConstant(String.valueOf(ordinalOfTarget - ordinalOfJump)));
		}
	}

	@Override
	public void doCheck(Analyzer analyzer, SymbolManager symbolManager) {
		boolean isStatic;

		clazz = symbolManager.getCurrentClass();
		clazz.getDeclMethodDefinitions().add(this);

		if (specifiers == null) {
			declarator.setBaseType(clazz.getType());
			declarator.check();
			type = declarator.getResultType();

			if (declarator.id == null)
				return;

			if (!clazz.getClassName().endsWith(declarator.id)) {
				analyzer.error("Return type for the method is missing", this.getPosition());
				return;
			}

			isStatic = false;
		} else {
			decl_specifiers declaration_specifiers_ = specifiers;
			declaration_specifiers_.check();

			declarator.setBaseType(declaration_specifiers_.type);
			declarator.check();
			type = declarator.getResultType();
			if (declarator.id == null)
				return;

			isStatic = declaration_specifiers_.isStatic;
		}
		String name = declarator.id;
		String position = this.getPosition();

		if (!TypeFactory.test(type, Kind.METHOD)) {
			analyzer.error("illegal,need to be method", position);
			return;
		}
		if (specifiers == null) {
			method = clazz.addConstruct(name, type, isStatic, position);
		} else {
			method = symbolManager.addSymbol(name, type, isStatic, position);
		}
	}

	private void epilogue(Analyzer analyzer) {
		analyzer.emit(Operator.RET);

	}

	private void prologue(Analyzer analyzer) {
		LinkedList<Address> locals = new LinkedList<Address>();
		for (Symbol l : scopeMethod.getLocals()) {
			String typeEncoding = TypeFactory.getTypeCode(l.getType());
			locals.add(Address.newConstant(typeEncoding));
		}
		scopeMethod.begin.emit(Operator.LOCAL, locals.toArray(new Address[0]));
	}

	public void setCompound_stmt(stmt_compound compound_stmt) {
		this.compound_stmt = compound_stmt;
	}

	public void setDeclarator(decl_declarator declarator) {
		this.declarator = declarator;
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
		if (compound_stmt != null)
			compound_stmt.toXML(ele);
		upper.appendChild(ele);
	}
}