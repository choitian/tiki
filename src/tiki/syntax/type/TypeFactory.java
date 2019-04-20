package tiki.syntax.type;

public class TypeFactory {
	public static final IType BOOLEAN = new Type(Kind.BOOLEAN);
	public static final IType FLOAT = new Type(Kind.FLOAT);
	public static final IType INT = new Type(Kind.INT);
	public static final IType NULL = new Type(Kind.NULL);
	public static final IType OBJECT = TypeFactory.getClass("tiki.Object");
	public static final IType STRING = TypeFactory.getClass("tiki.String");
	public static final IType VOID = new Type(Kind.VOID);

	public static IType conversionCommonType(IType t0, IType t1) {
		if (isEqual(t0, t1)) {
			return t0;
		} else if (TypeFactory.test(t0, Kind.ARITHMETIC) && TypeFactory.test(t1, Kind.ARITHMETIC)) {
			return (TypeFactory.test(t0, Kind.FLOAT) || TypeFactory.test(t1, Kind.FLOAT)) ? TypeFactory.FLOAT
					: TypeFactory.INT;
		} else if (TypeFactory.test(t0, Kind.NULL) && TypeFactory.test(t1, Kind.REFERENCE)) {
			return t1;
		} else if (TypeFactory.test(t1, Kind.NULL) && TypeFactory.test(t0, Kind.REFERENCE)) {
			return t0;
		} else if (TypeFactory.test(t0, Kind.OBJECT) && TypeFactory.test(t1, Kind.REFERENCE)) {
			return t0;
		} else if (TypeFactory.test(t1, Kind.OBJECT) && TypeFactory.test(t0, Kind.REFERENCE)) {
			return t1;
		} else {
			return TypeFactory.VOID;
		}
	}

	public static Type getArray(IType elementType) {
		return new Type(Kind.ARRAY, elementType);
	}

	public static IType getArrayElementType(IType type) {
		if (!TypeFactory.test(type, Kind.ARRAY)) {
			throw new IllegalArgumentException("build syntax tree failed.");
		}
		return type.getBase();
	}

	public static Type getClass(String name) {
		return new Type(Kind.CLASS, name);
	}

	public static TypeMethod getMethod(IType returnType) {
		return new TypeMethod(returnType);
	}

	public static IType getPrimitiveType(String typeName) {
		switch (Kind.valueOf(typeName.toUpperCase())) {
		case VOID:
			return TypeFactory.VOID;
		case INT:
			return TypeFactory.INT;
		case FLOAT:
			return TypeFactory.FLOAT;
		case BOOLEAN:
			return TypeFactory.BOOLEAN;
		default:
			return TypeFactory.VOID;
		}
	}

	public static String getTypeCode(IType type) {
		return getTypeKind(type).substring(0, 1);
	}

	private static String getTypeKind(IType type) {
		if (TypeFactory.test(type, Kind.REFERENCE))
			return Kind.REFERENCE.name();

		return type.getKind().name();
	}

	public static boolean isArithmeticAssignable(IType target, IType from) {
		if (!TypeFactory.test(from, Kind.ARITHMETIC))
			return false;
		if (!TypeFactory.test(target, Kind.ARITHMETIC))
			return false;

		if (TypeFactory.test(target, Kind.INT) && TypeFactory.test(from, Kind.FLOAT))
			return false;

		return true;
	}

	public static boolean isArithmeticCastable(IType target, IType from) {
		if (!TypeFactory.test(from, Kind.ARITHMETIC))
			return false;
		if (!TypeFactory.test(target, Kind.ARITHMETIC))
			return false;
		return true;
	}

	public static boolean isAssignableType(IType target, IType from) {
		if (isEqual(target, from)) {
			return true;
		} else if (isArithmeticAssignable(target, from)) {
			return true;
		} else if (TypeFactory.test(target, Kind.REFERENCE) && TypeFactory.test(from, Kind.NULL)) {
			return true;
		} else if (TypeFactory.test(target, Kind.ARRAY) && TypeFactory.test(from, Kind.ARRAY)) {
			return true;
		} else if (TypeFactory.test(target, Kind.OBJECT)) {
			return true;
		} else {
			return false;
		}
	}

	public static boolean isCastableType(IType target, IType from) {
		if (isEqual(target, from)) {
			return true;
		} else if (isArithmeticCastable(target, from)) {
			return true;
		} else if (TypeFactory.test(target, Kind.REFERENCE) && TypeFactory.test(from, Kind.NULL)) {
			return true;
		} else if (TypeFactory.test(target, Kind.ARRAY) && TypeFactory.test(from, Kind.ARRAY)) {
			return true;
		} else if (TypeFactory.test(target, Kind.OBJECT)) {
			return true;
		} else {
			return false;
		}
	}

	public static boolean isComparableType(IType t0, IType t1) {
		if (isEqual(t0, t1)) {
			return true;
		} else if (TypeFactory.test(t0, Kind.ARITHMETIC) && TypeFactory.test(t1, Kind.ARITHMETIC)) {
			return true;
		} else if (TypeFactory.test(t0, Kind.NULL) && TypeFactory.test(t0, Kind.REFERENCE)) {
			return true;
		} else if (TypeFactory.test(t1, Kind.NULL) && TypeFactory.test(t1, Kind.REFERENCE)) {
			return true;
		} else if (TypeFactory.test(t0, Kind.OBJECT) && TypeFactory.test(t1, Kind.REFERENCE)) {
			return true;
		} else if (TypeFactory.test(t1, Kind.OBJECT) && TypeFactory.test(t0, Kind.REFERENCE)) {
			return true;
		} else {
			return false;
		}
	}

	public static boolean isEqual(IType type0, IType type1) {
		return type0.isEqualTo(type1);
	}

	public static boolean test(IType type, Kind kind) {
		boolean ret = false;
		switch (kind) {
		case ARITHMETIC:
			ret = type.equals(TypeFactory.INT) || type.equals(TypeFactory.FLOAT);
			break;
		case ARRAY:
			ret = type.getKind() == Kind.ARRAY;
			break;
		case BOOLEAN:
			ret = type.equals(TypeFactory.BOOLEAN);
			break;
		case CLASS:
			ret = type.getKind() == Kind.CLASS;
			break;
		case FLOAT:
			ret = type.equals(TypeFactory.FLOAT);
			break;
		case INT:
			ret = type.equals(TypeFactory.INT);
			break;
		case METHOD:
			ret = type instanceof TypeMethod;
			break;
		case NULL:
			ret = type.equals(TypeFactory.NULL);
			break;
		case REFERENCE:
			ret = type.getKind() == Kind.CLASS || type.getKind() == Kind.ARRAY || type instanceof TypeMethod
					|| type.equals(TypeFactory.NULL);
			break;
		case VOID:
			ret = type.equals(TypeFactory.VOID);
			break;
		case OBJECT:
			ret = test(type, Kind.CLASS) && type.getName().equals("tiki.Object");
			break;
		case STRING:
			ret = test(type, Kind.CLASS) && type.getName().equals("tiki.String");
			break;
		default:
			break;

		}
		return ret;
	}

}
