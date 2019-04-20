package tiki.syntax.node;

import tiki.syntax.type.IType;

public abstract class decl_declarator extends Decl {
	protected IType baseType;

	protected String id;

	protected IType resultType;

	public IType getResultType() {
		return resultType;
	}

	public void setBaseType(IType baseType) {
		this.baseType = baseType;
	}
	public abstract String getId() ;
	public void setId(String id) {
		this.id = id;
	}
}