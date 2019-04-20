package tiki.syntax;

import java.util.LinkedHashMap;

import tiki.uitls.PackagenameUtils;

public class PackageScope {
	Clazz currentClass;
	Method currentMethod;
	LinkedHashMap<String, String> importMap;
	private String name;

	PackageScope(String name) {
		this.name = name;
		importMap = new LinkedHashMap<String, String>();
		addImport("tiki.Object");
		addImport("tiki.Array");
		addImport("tiki.String");
		addImport("tiki.util.System");
		addImport("tiki.util.Math");
		addImport("tiki.util.Date");
	}

	public void addImport(String packageName) {
		importMap.put(PackagenameUtils.getBase(packageName), packageName);
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
}
