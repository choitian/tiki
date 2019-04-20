package tiki.syntax;

import tiki.syntax.instruction.Address;
import tiki.syntax.type.IType;
import tiki.syntax.type.TypeFactory;

public class Evalue {
	public static final Evalue FALSE = new Evalue(Address.FALSE, TypeFactory.BOOLEAN);
	public static final Evalue NULL = new Evalue(Address.NULL, TypeFactory.NULL);
	public static final Evalue ONE = new Evalue(Address.ONE, TypeFactory.INT);
	public static final Evalue NEGONE = new Evalue(Address.NEGONE, TypeFactory.INT);
	public static final Evalue TRUE = new Evalue(Address.TRUE, TypeFactory.BOOLEAN);
	public static final Evalue ZERO = new Evalue(Address.ZERO, TypeFactory.INT);
	
	public static Evalue newClass(String className, IType type) {
		return new Evalue(Address.newClass(className), type);
	}
	public static Evalue defaultValue(IType type)
	{
		String code = TypeFactory.getTypeCode(type);
		switch (code) {
		case "I":
			return ZERO;
		case "F":
			return new Evalue(Address.ZERO, TypeFactory.FLOAT);
		case "B":
			return FALSE;
		case "R":
			return new Evalue(Address.NULL, type);
		default:
			throw new RuntimeException(String.format("unkown memory value type '%s'", type));
		}		
	}
	public static Evalue newConstant(String value, IType type) {
		return new Evalue(Address.newConstant(value), type);
	}

	public static Evalue newConstantString(String value) {
		return new Evalue(Address.newConstant(value), TypeFactory.STRING);
	}

	public static Evalue newReference(Evalue variable, IType type) {
		return new Evalue(Address.newReference(variable.address), type);
	}
	public static Evalue newVariable(int offset, IType type) {
		return new Evalue(Address.newVariable(offset), type);
	}

	private Address address;
	private Evalue extra;
	private boolean lvalue;
	private boolean staticScope;
	private IType type;

	private Evalue(Address address, IType type) {
		this.address = address;
		this.type = type;
	}

	public Address getAddress() {
		return address;
	}

	public Evalue getExtra() {
		return extra;
	}

	public IType getType() {
		return type;
	}

	public boolean isLvalue() {
		return lvalue;
	}

	public boolean isStaticScope() {
		return staticScope;
	}

	public void setExtra(Evalue extra) {
		this.extra = extra;
	}

	public void setLvalue(boolean lvalue) {
		this.lvalue = lvalue;
	}

	public void setStaticScope(boolean staticScope) {
		this.staticScope = staticScope;
	}
}
