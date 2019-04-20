package tiki.tools.complier.lex;

class StringUtils {
	public static boolean isWhiteString(String str) {
		if (str == null || str.length() == 0)
			return true;

		boolean is_white_line = true;
		int cur = 0;
		while (cur < str.length() && is_white_line) {
			is_white_line = Character.isWhitespace(str.charAt(cur++));
		}
		return is_white_line;
	}

	public static int LeftWhiteSpace(char[] ca, int offset) {
		if (ca == null || ca.length == 0)
			return 0;

		boolean is_white_space = true;
		int cur = offset;
		int len = 0;
		while (cur < ca.length && is_white_space) {
			is_white_space = Character.isWhitespace(ca[cur++]);
			if (is_white_space)
				len++;
		}
		return len;
	}

	public static int LeftWhiteSpace(String str, int offset) {
		if (str == null || str.length() == 0)
			return 0;

		boolean is_white_space = true;
		int cur = offset;
		int len = 0;
		while (cur < str.length() && is_white_space) {
			is_white_space = Character.isWhitespace(str.charAt(cur++));
			if (is_white_space)
				len++;
		}
		return len;
	}

	public static Character unescapeCharacter(char ch) {
		switch (ch) {
		case 'n':
			return '\n';
		case 't':
			return '\t';
		case 'v':
			return 0x000B;
		case 'b':
			return '\b';
		case 'r':
			return '\r';
		case 'f':
			return '\f';
		case 'a':
			return 0x0007;
		case '\\':
			return '\\';
		case '\'':
			return '\'';
		case '\"':
			return '\"';
		default:
			return ch;
		}
	}

	public static String unescapeString(String str) {
		StringBuilder sb = new StringBuilder();
		for (int index = 0; index < str.length(); index++) {
			char ch = str.charAt(index);

			if (ch == '\\' && index + 1 < str.length()) {
				index++;
				ch = StringUtils.unescapeCharacter(str.charAt(index));
			}
			sb.append(ch);
		}
		return sb.toString();
	}
}
