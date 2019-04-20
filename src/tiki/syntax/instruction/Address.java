package tiki.syntax.instruction;

public class Address {
	public final static Address FALSE = Address.newConstant("false");
	public final static Address NULL = new Address("null");
	public final static Address ONE = Address.newConstant("1");
	public final static Address NEGONE = Address.newConstant("-1");
	public final static Address TRUE = Address.newConstant("true");
	public final static Address ZERO = Address.newConstant("0");

	private static String b2h(byte[] bytes) {
		return b2h(bytes, bytes.length);
	}

	private static String b2h(byte[] bytes, int len) {
		char[] hex = new char[len * 2];
		for (int idx = 0; idx < len; ++idx) {
			int hi = (bytes[idx] & 0xF0) >>> 4;
			int lo = (bytes[idx] & 0x0F);
			hex[idx * 2] = (char) (hi < 10 ? '0' + hi : 'A' - 10 + hi);
			hex[idx * 2 + 1] = (char) (lo < 10 ? '0' + lo : 'A' - 10 + lo);
		}
		return new String(hex);
	}

	private static byte[] h2b(String hex) {
		if ((hex.length() & 0x01) == 0x01)
			throw new IllegalArgumentException("illegal value in hex string");
		byte[] bytes = new byte[hex.length() / 2];
		for (int idx = 0; idx < bytes.length; ++idx) {
			int hi = Character.digit((int) hex.charAt(idx * 2), 16);
			int lo = Character.digit((int) hex.charAt(idx * 2 + 1), 16);
			if ((hi < 0) || (lo < 0))
				throw new IllegalArgumentException("illegal value in hex string");
			bytes[idx] = (byte) ((hi << 4) | lo);
		}
		return bytes;
	}

	public static Address newClass(String className) {
		return new Address("S@" + className);
	}

	public static Address newConstant(String value) {
		return new Address("C@" + b2h(value.getBytes()));
	}

	public static Address newReference(Address variable) {
		return new Address("R@" + variable.value.substring(2));
	}
	
	public static Address newVariable(int offset) {
		return new Address("V@" + offset);
	}
	public boolean isConstant() {
		return value.startsWith("C@") || value.equals("null");
	}
	public String getValue() {
		if (value.equals("null")) {
			return null;
		}
		if (isConstant())
			return new String(h2b(value.substring(2)));

		return value.substring(2);
	}
	private String value;

	private Address(String value) {
		this.value = value;
	}

	@Override
	public String toString() {
		return value;
	}
}
