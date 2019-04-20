package tiki.runtime;

import java.io.InputStream;
import java.util.Map;

public interface IComponent {
	String fieldDeclaration();

	InputStream getPluginDeclaration(String name);
	
	Map<String, IMethod> methodMap();
}
