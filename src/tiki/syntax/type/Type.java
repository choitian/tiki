package tiki.syntax.type;

class Type implements IType {
	private IType base;
	public Kind kind;
	private String name;

	public Type(Kind kind) {
		this.kind = kind;
	}

	public Type(Kind kind, IType base) {
		this.kind = kind;
		this.base = base;
	}

	public Type(Kind kind, String name) {
		this.kind = kind;
		this.name = name;
	}

	@Override
	public String description() {
		if (kind == Kind.ARRAY) {
			return String.format("%s[]", base.description());
		} else if (kind == Kind.CLASS) {
			return String.format("%s", name);
		} else {
			return kind.name().toLowerCase();
		}

	}

	@Override
	public IType getBase() {
		return base;
	}

	@Override
	public Kind getKind() {
		return kind;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public boolean isEqualTo(IType type) {
		if (!this.getClass().isInstance(type))
			return false;

		Type tp = (Type) type;
		if (!kind.equals(tp.kind))
			return false;

		if (base != null) {
			if (!base.isEqualTo(tp.base))
				return false;
		}
		if (name != null) {
			if (!name.equals(tp.name))
				return false;
		}
		return true;
	}
}