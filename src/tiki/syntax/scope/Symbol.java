package tiki.syntax.scope;

import tiki.syntax.type.IType;

public class Symbol {
	protected boolean isStatic;
	protected String name;

	protected int offset;
	protected String position;
	protected IScope scope;
	protected IType type;

	public Symbol(String name, IType type, boolean isStatic, String position) {
		this.name = name;
		this.type = type;
		this.isStatic = isStatic;
		this.position = position;
	}

	public String getName() {
		return name;
	}

	public int getOffset() {
		return offset;
	}

	public String getPosition() {
		return position;
	}

	public IScope getScope() {
		return scope;
	}

	public IType getType() {
		return type;
	}

	public boolean isStatic() {
		return isStatic;
	}

	public void setOffset(int offset) {
		this.offset = offset;
	}

	public void setScope(IScope scope) {
		this.scope = scope;
	}
}
