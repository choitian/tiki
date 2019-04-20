package tiki.syntax;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import tiki.syntax.instruction.Instruction;
import tiki.syntax.node.decl_method_definition;
import tiki.syntax.scope.Scope;
import tiki.syntax.scope.Symbol;
import tiki.syntax.type.IType;
import tiki.syntax.type.Kind;
import tiki.syntax.type.TypeFactory;

public class Clazz extends Scope {
	private String annotation;
	private Symbol construct;
	private ArrayList<decl_method_definition> declMethodDefinitions;
	private LinkedHashMap<String, ArrayList<Instruction>> methodEntrys;
	private IType type;

	public Clazz(IType type) {
		declMethodDefinitions = new ArrayList<decl_method_definition>();
		methodEntrys = new LinkedHashMap<String, ArrayList<Instruction>>();

		this.type = type;
	}

	public Symbol addConstruct(String name, IType type, boolean isStatic, String position) {
		if (construct != null) {
			String msg = String.format("construct %s: already defined in %s,redefinition", name,
					construct.getPosition());
			getAnalyzer().error(msg, position);
			return null;
		}
		construct = new Symbol(name, type, isStatic, position);
		return construct;
	}

	public String getAnnotation() {
		return annotation;
	}

	public String getClassName() {
		return type.getName();
	}

	public Symbol getConstruct() {
		return construct;
	}

	public ArrayList<decl_method_definition> getDeclMethodDefinitions() {
		return declMethodDefinitions;
	}

	public Symbol getMember(String name) {
		return name2symbolMap.get(name);
	}

	public ArrayList<Instruction> getMethodEntry(String methodName) {
		return methodEntrys.get(methodName);
	}

	public Collection<Symbol> getNonstatics() {
		ArrayList<Symbol> nonstatics = new ArrayList<Symbol>();
		for (Symbol symbol : this.getSymbols()) {
			if (!symbol.isStatic() && !TypeFactory.test(symbol.getType(), Kind.METHOD)) {
				nonstatics.add(symbol);
			}
		}
		return nonstatics;
	}

	public IType getType() {
		return type;
	}

	public void initializeMember() {
		int statics_off = 0;
		int nonstatics_off = 0;
		if (construct != null) {
			construct.setOffset(statics_off++);
		}
		for (Symbol m : this.getSymbols()) {
			if (TypeFactory.test(m.getType(), Kind.METHOD)) {
				m.setOffset(statics_off++);
			} else if (m.isStatic()) {
				m.setOffset(statics_off++);
			} else {
				m.setOffset(nonstatics_off++);
			}
		}
	}

	public ArrayList<Instruction> newMethodEntry(String methodName) {
		ArrayList<Instruction> entry = new ArrayList<Instruction>();
		methodEntrys.put(methodName, entry);
		return entry;
	}

	public void setAnnotation(String annotation) {
		this.annotation = annotation;
	}

	public void setConstruct(Symbol construct) {
		if (this.construct != null) {
			String msg = String.format("construct %s: already defined in %s,redefinition", this.construct.getName(),
					this.construct.getPosition());
			getAnalyzer().error(msg, construct.getPosition());
			return;
		}
		this.construct = construct;
	}

	public Element toXML(Element upper) {
		Document doc = upper.getOwnerDocument();
		Element class_e = doc.createElement("class");

		Attr attr = doc.createAttribute("name");
		attr.setValue(type.getName());
		class_e.setAttributeNode(attr);

		if (construct != null) {
			Element method_e = doc.createElement("member");

			attr = doc.createAttribute("kind");
			attr.setValue("method");
			method_e.setAttributeNode(attr);

			attr = doc.createAttribute("name");
			attr.setValue(construct.getName());
			method_e.setAttributeNode(attr);

			ArrayList<Instruction> entry = getMethodEntry(construct.getName());
			if (entry != null) {
				for (Instruction ins : entry) {
					Element ins_e = ins.toXML(method_e);
					method_e.appendChild(ins_e);
				}
			}
			class_e.appendChild(method_e);
		}
		for (Symbol member : getSymbols()) {
			if (member.isStatic() && !TypeFactory.test(member.getType(), Kind.METHOD)) {
				Element static_e = doc.createElement("member");

				attr = doc.createAttribute("kind");
				attr.setValue("field");
				static_e.setAttributeNode(attr);

				attr = doc.createAttribute("type");
				attr.setValue(TypeFactory.getTypeCode(member.getType()));
				static_e.setAttributeNode(attr);

				static_e.setTextContent(member.getName());
				class_e.appendChild(static_e);
			} else if (TypeFactory.test(member.getType(), Kind.METHOD)) {
				Element method_e = doc.createElement("member");

				attr = doc.createAttribute("kind");
				attr.setValue("method");
				method_e.setAttributeNode(attr);

				attr = doc.createAttribute("name");
				attr.setValue(member.getName());
				method_e.setAttributeNode(attr);

				if (member.isStatic() && member.getName().equals("main")) {
					attr = doc.createAttribute("entry");
					attr.setValue("true");
					method_e.setAttributeNode(attr);
				}
				ArrayList<Instruction> entry = getMethodEntry(member.getName());
				if (entry != null) {
					for (Instruction ins : entry) {
						Element ins_e = ins.toXML(method_e);
						method_e.appendChild(ins_e);
					}
				}
				class_e.appendChild(method_e);
			}
		}
		return class_e;
	}
}
