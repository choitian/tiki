package tiki.syntax.node;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import tiki.syntax.Analyzer;
import tiki.syntax.Clazz;
import tiki.syntax.SymbolManager;
import tiki.syntax.type.IType;

public class decl_class_definition extends Decl {
	decl_annotation annotation;

	ListNode class_declarations;

	private String name;

	@Override
	public void doCheck(Analyzer analyzer, SymbolManager symbolManager) {
		IType typeClass = analyzer.getTypeClass(name);
		if (typeClass == null) {
			String msg = String.format("%s cannot be resolved to a type", name);
			analyzer.error(msg, this.getPosition());
			return;
		}
		Clazz clazz = analyzer.addDefinedClass(typeClass, this.getPosition());
		if (annotation != null) {
			annotation.check();
			clazz.setAnnotation(annotation.info);
		}

		if (class_declarations != null) {

			symbolManager.enterClassScope(clazz);

			class_declarations.check();

			clazz.initializeMember();
			symbolManager.exitClassScope();
		}
	}

	public String getName() {
		return name;
	}

	public void setAnnotation(decl_annotation annotation) {
		this.annotation = annotation;
	}

	public void setClass_declarations(ListNode class_declarations) {
		this.class_declarations = class_declarations;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Override
	public void toXML(Element upper) {
		Document doc = upper.getOwnerDocument();
		Element ele = doc.createElement(this.getClass().getSimpleName());

		Attr attr = doc.createAttribute("name");
		attr.setValue(name);
		ele.setAttributeNode(attr);

		if (annotation != null)
			annotation.toXML(ele);

		if (class_declarations != null)
			class_declarations.toXML(ele);

		upper.appendChild(ele);
	}
}