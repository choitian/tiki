package tiki.syntax;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Stack;

import tiki.syntax.instruction.InstructionBlock;
import tiki.syntax.instruction.InstructionSection;
import tiki.syntax.scope.Scope;
import tiki.syntax.scope.Symbol;
import tiki.syntax.type.IType;

public class Method extends Scope {
	public final static String THIS = "this";
	public InstructionBlock begin;
	public InstructionBlock body_block;
	public Stack<InstructionBlock> breakableStack = new Stack<InstructionBlock>();

	private Clazz clazz;
	public Stack<InstructionBlock> continuableStack = new Stack<InstructionBlock>();
	public InstructionBlock end;

	public InstructionSection ic;
	private ArrayList<Symbol> locals;
	private Symbol method;

	private int NO = 0;

	// ScopeFunctionBody function_body_scope;
	public Method(Clazz clazz, Symbol method) {
		this.clazz = clazz;
		this.method = method;
		this.ic = new InstructionSection();
		this.locals = new ArrayList<Symbol>();

		begin = newInstructionBlock(String.format("%s", getScopeName()));
		body_block = newInstructionBlock(String.format("%s_BODY", getScopeName()));
		end = newInstructionBlock(String.format("%s_END", getScopeName()));
	}

	public void addLocal(Symbol symbol) {
		symbol.setOffset(locals.size());
		locals.add(symbol);
	}

	public Collection<Symbol> getLocals() {
		return locals;
	}

	private String getScopeName() {
		return String.format("%s.%s", clazz.getClassName(), method.getName());
	}

	public IType getType() {
		return method.getType();
	}

	public void initializeParameter(Analyzer analyzer, Collection<Symbol> parameters) {
		Symbol thiz = new Symbol(Method.THIS, clazz.getType(), false, method.getPosition());
		thiz.setOffset(-1);
		super.insertSymbol(thiz);

		int order = 0;
		for (Symbol p : parameters) {
			p.setOffset(-2 - order++);
			super.insertSymbol(p);
		}
	}

	@Override
	public void insertSymbol(Symbol symbol) {
		addLocal(symbol);
		super.insertSymbol(symbol);
	}

	public boolean isStatic() {
		return method.isStatic();
	}

	public InstructionBlock newInstructionBlock() {
		return new InstructionBlock(ic, String.format("%s__%d", getScopeName(), NO++));
	}

	public InstructionBlock newInstructionBlock(String label) {
		return new InstructionBlock(ic, label);
	}
}
