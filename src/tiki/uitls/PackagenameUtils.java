package tiki.uitls;

import java.util.List;

public class PackagenameUtils {
	public static String concat(String parent, String nameToAdd) {
		if (parent.isEmpty()) {
			return nameToAdd;
		} else {
			return parent + "." + nameToAdd;
		}
	}

	public static String getBase(String name) {
		int index = name.lastIndexOf('.');
		if (index == -1) {
			return name;
		} else {
			return name.substring(index + 1);
		}
	}

	public static String getParent(String name) {
		int index = name.lastIndexOf('.');
		if (index == -1) {
			return "";
		} else {
			return name.substring(0, index);
		}
	}

	public static String getRoot(String name) {
		int index = name.indexOf('.');
		if (index == -1) {
			return name;
		} else {
			return name.substring(0, index);
		}
	}

	public static String link(List<String> list, String delimit) {
		String result = null;
		for (String value : list) {
			if (result == null)
				result = value;
			else
				result = result + delimit + value;
		}
		return result;
	}

	public static String toPath(String name) {
		return name.replace('.', '/');
	}
}
