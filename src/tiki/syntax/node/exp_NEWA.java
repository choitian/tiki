package tiki.syntax.node;

import java.util.LinkedList;
import java.util.List;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import tiki.syntax.instruction.Operator;
import tiki.syntax.Analyzer;
import tiki.syntax.Evalue;
import tiki.syntax.SymbolManager;
import tiki.syntax.type.IType;
import tiki.syntax.type.Kind;
import tiki.syntax.type.TypeFactory;

public class exp_NEWA extends Exp {
	ListNode indexs;

	decl_specifiers specifiers;
	private IType checkIndexList(Analyzer analyzer, ListNode index_list, IType type, List<Evalue> indexs) {
		Exp value = (Exp) index_list.node;
		ListNode LIST = index_list.list;

		value.check();
		if (value.getEvalue() == null) {
			return null;
		}
		if (!TypeFactory.test(value.getEvalue().getType(), Kind.INT)) {
			String msg = String.format("Type mismatch: cannot convert from %s to int",
					value.getEvalue().getType().description());
			analyzer.error(msg, this.getPosition());
		}
		indexs.add(value.getEvalue());
		IType ta = TypeFactory.getArray(type);

		if (LIST != null)
			return checkIndexList(analyzer, LIST, ta, indexs);

		return ta;
	}
	@Override
	public void doCheck(Analyzer analyzer, SymbolManager symbolManager) {
		specifiers.check();
		IType type = specifiers.type;

		if (TypeFactory.test(type, Kind.VOID)) {
			String msg = "illegal,array of void";
			analyzer.error(msg, this.getPosition());
		}

		LinkedList<Evalue> indexEvalues = new LinkedList<Evalue>();
		IType ta = checkIndexList(analyzer, indexs, type, indexEvalues);
		if (ta == null) {
			return;
		}
		
 		setEvalue(analyzer.newTempEvalue(ta));
		String typeEncoding = TypeFactory.getTypeCode(type);
		indexEvalues.addFirst(Evalue.newConstantString(typeEncoding));
		indexEvalues.addFirst(getEvalue());
		analyzer.emit(Operator.NEWA, indexEvalues.toArray(new Evalue[0]));
	}

	public void setIndexs(ListNode indexs) {
		this.indexs = indexs;
	}

	public void setSpecifiers(decl_specifiers specifiers) {
		this.specifiers = specifiers;
	}

	@Override
	public void toXML(Element upper) {
		Document doc = upper.getOwnerDocument();
		Element ele = doc.createElement(this.getClass().getSimpleName());
		specifiers.toXML(ele);
		indexs.toXML(ele);

		upper.appendChild(ele);
	}
}