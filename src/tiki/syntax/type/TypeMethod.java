package tiki.syntax.type;

import java.util.Collection;
import java.util.Iterator;

import tiki.syntax.scope.Scope;
import tiki.syntax.scope.Symbol;

public class TypeMethod extends Scope implements IType {
	private IType returnType;
	private IType varargsType;

	TypeMethod(IType returnType) {
		this.returnType = returnType;
	}

	@Override
	public String description() {
		StringBuffer sb = new StringBuffer();

		Collection<Symbol> paramenters = this.getSymbols();
		for (Symbol paramenter : paramenters) {
			if (sb.length() != 0) {
				sb.append(", ");
			}
			sb.append(String.format("%s", paramenter.getType().description()));
		}
		return String.format("%s method(%s)", returnType.description(), sb.toString());
	}

	@Override
	public IType getBase() {
		return returnType;
	}

	@Override
	public Kind getKind() {
		return Kind.METHOD;
	}

	@Override
	public String getName() {
		return null;
	}

	public IType getVarargElementType() {
		return varargsType;
	}

	@Override
	public boolean isEqualTo(IType type) {
		if (!this.getClass().isInstance(type))
			return false;

		TypeMethod tf = (TypeMethod) type;

		if (!returnType.isEqualTo(tf.returnType))
			return false;

		Collection<Symbol> parameters0 = this.getSymbols();
		Collection<Symbol> parameters1 = tf.getSymbols();

		if (parameters0.size() != parameters1.size())
			return false;

		Iterator<Symbol> it0 = parameters0.iterator();
		Iterator<Symbol> it1 = parameters1.iterator();

		for (int index = 0; index < parameters0.size(); index++) {
			Symbol p0 = it0.next();
			Symbol p1 = it1.next();

			if (!p0.getType().isEqualTo(p1.getType()))
				return false;
		}

		return true;
	}

	public boolean isVariableLength() {
		return varargsType != null;
	}

	public void setVararg(IType varargsType) {
		this.varargsType = varargsType;
	}
}
