package tiki.runtime;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;


import tiki.uitls.PackagenameUtils;

public class BaseComponent implements IComponent {
	protected LinkedHashMap<String, IMethod> registeredMethodMap;
	protected String xml;

	public BaseComponent() {
		registeredMethodMap = new LinkedHashMap<String, IMethod>();
	}

	@Override
	public String fieldDeclaration() {
		return "";
	}

	@Override
	public InputStream getPluginDeclaration(String name) {
		// get PluginDeclaration
		String body = "";

		body += this.fieldDeclaration();

		Map<String, IMethod> methodMap = this.methodMap();
		for (Entry<String, IMethod> method : methodMap.entrySet()) {
			body += String.format(method.getValue().signature(), method.getKey()) + ";\n";
		}
		String decl = String.format("class %s {\n%s}\n", PackagenameUtils.getBase(name), body);

		try {
			return new ByteArrayInputStream(decl.getBytes("UTF-8"));
		} catch (IOException e) {
		}
		return null;
	}

	@Override
	public Map<String, IMethod> methodMap() {
		return registeredMethodMap;
	}
	protected void registerMethod(IMethod method, String methodName) {
		registeredMethodMap.put(methodName, method);
	}
}
