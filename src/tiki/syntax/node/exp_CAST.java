package tiki.syntax.node;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import tiki.syntax.instruction.Operator;
import tiki.syntax.Analyzer;
import tiki.syntax.Evalue;
import tiki.syntax.SymbolManager;
import tiki.syntax.type.IType;
import tiki.syntax.type.TypeFactory;

public class exp_CAST extends Exp {
	String kind;

	exp_SCOPE_NAME scope_name;

	decl_specifier target;

	String value;

	@Override
	public void doCheck(Analyzer analyzer, SymbolManager symbolManager) {
		IType targetType;
		switch (kind) {
		case "class":
			targetType = analyzer.getTypeClass(scope_name.getName());
			break;
		case "primitive":
			targetType = TypeFactory.getPrimitiveType(target.value);
			break;
		default:
			targetType = null;
		}
		if (targetType == null)
			return;

		getOp0().check();
		if (getOp0().getEvalue() == null)
			return;

		Evalue op0_evalue = getOp0().getEvalue();
		IType expType = getOp0().getEvalue().getType();

		// check constraint
		if (TypeFactory.isCastableType(targetType, expType)) {
			Evalue result;

			String opType = TypeFactory.getTypeCode(expType) + "2" + TypeFactory.getTypeCode(targetType);
			switch (opType) {
			case "F2I":
				result = analyzer.emit(Operator.F2I, targetType, op0_evalue);
				break;
			default:
				result = analyzer.emit(Operator.MOV, targetType, op0_evalue);
			}
			setEvalue(result);
		} else {
			String msg = String.format("Type mismatch: cannot convert from %s to %s", expType.description(),
					targetType.description());
			analyzer.error(msg, this.getPosition());
			return;
		}
	}

	public void setKind(String kind) {
		this.kind = kind;
	}

	public void setScope_name(exp_SCOPE_NAME scope_name) {
		this.scope_name = scope_name;
	}

	public void setTarget(decl_specifier target) {
		this.target = target;
	}

	public void setValue(String value) {
		this.value = value;
	}

	@Override
	public void toXML(Element upper) {
		Document doc = upper.getOwnerDocument();
		Element ele = doc.createElement(this.getClass().getSimpleName());
		Attr attr;

		if (kind != null) {
			attr = doc.createAttribute("kind");
			attr.setValue(kind);
			ele.setAttributeNode(attr);
		}
		if (value != null) {
			attr = doc.createAttribute("value");
			attr.setValue(value);
			ele.setAttributeNode(attr);
		}
		if (target != null) {
			target.toXML(ele);
		}
		upper.appendChild(ele);
	}
}