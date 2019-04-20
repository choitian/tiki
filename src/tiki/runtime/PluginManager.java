package tiki.runtime;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.LinkedHashMap;
import java.util.Map;

import tiki.uitls.PackagenameUtils;

public class PluginManager {
	private LinkedHashMap<String, IComponent> pluginMap;

	public PluginManager() {	
		this.pluginMap = new LinkedHashMap<String, IComponent>();
	}
	public IComponent installPlugin(String name,IComponent com) {
		return pluginMap.put(name, com);
	}

	public void invoke(String methodName, IRuntime rt) {
		String className = PackagenameUtils.getParent(methodName);
		String name = PackagenameUtils.getBase(methodName);

		IComponent com = pluginMap.get(className);

		if (com == null) {
			throw new RuntimeException(
					String.format("plugin '%s' unloaded,or method '%s' undefined.", className, name));
		}
		Map<String, IMethod> methods = com.methodMap();
		IMethod method = methods.get(name);
		if (method != null) {
			method.invoke(new Context(rt));
		}
	}

	InputStream openPackage(String path) {
		File file = new File(path);
		if (file.exists())
			try {
				return new FileInputStream(file);
			} catch (FileNotFoundException e) {
			}
		return null;
	}
}
