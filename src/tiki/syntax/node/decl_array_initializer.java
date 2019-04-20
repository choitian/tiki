package tiki.syntax.node;

import java.util.LinkedList;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import tiki.syntax.instruction.Operator;
import tiki.syntax.Analyzer;
import tiki.syntax.Evalue;
import tiki.syntax.SymbolManager;
import tiki.syntax.type.IType;
import tiki.syntax.type.Kind;
import tiki.syntax.type.TypeFactory;

public class decl_array_initializer extends Stmt {
	ListNode initializers;
	private Evalue evalue;
	private IType definedType;

	public final void setEvalue(Evalue e) {
		this.evalue = e;
	}

	public final Evalue getEvalue() {
		return evalue;
	}

	public void doCheck(Analyzer analyzer, SymbolManager symbolManager) {
		if (!TypeFactory.test(definedType, Kind.ARRAY)) {
			String msg = String.format("left expression can not resolve to a array");
			analyzer.error(msg, this.getPosition());
			return;
		}

		IType elementType = TypeFactory.getArrayElementType(definedType);

		LinkedList<Evalue> inis = new LinkedList<Evalue>();
		int size = 1;
		if (initializers != null) {
			LinkedList<SyntaxNode> nodes = initializers.nodes();
			size += nodes.size();
			for (SyntaxNode node : nodes) {
				decl_initializer initializer = (decl_initializer) node;

				initializer.setDefinedType(elementType);
				initializer.check();
				Evalue ev = initializer.getEvalue();
				if (ev == null)
					return;
				IType toType = elementType;
				IType fromType = ev.getType();
				if (!TypeFactory.isAssignableType(toType, fromType)) {

					String msg = String.format("Type mismatch: cannot convert from %s to %s", fromType.description(),
							toType.description());
					analyzer.error(msg, this.getPosition());
					return;
				}

				inis.add(ev);
			}
		}
		IType ta = TypeFactory.getArray(elementType);

		setEvalue(analyzer.newTempEvalue(ta));

		Evalue addr = analyzer.newTempEvalue(TypeFactory.INT);
		analyzer.emit(Operator.NEW, addr, Evalue.newConstant(String.valueOf(size), TypeFactory.INT));
		analyzer.emit(Operator.MOV, Evalue.newReference(addr, TypeFactory.INT),
				Evalue.newConstant(String.valueOf(inis.size()), TypeFactory.INT));
		analyzer.emit(Operator.ADD_I, addr, addr, Evalue.ONE);

		analyzer.emit(Operator.MOV, getEvalue(), addr);
		for (Evalue ini : inis) {
			analyzer.emit(Operator.MOV, Evalue.newReference(addr, ini.getType()), ini);
			analyzer.emit(Operator.ADD_I, addr, addr, Evalue.ONE);
		}
	}

	public void setInitializers(ListNode initializers) {
		this.initializers = initializers;
	}

	@Override
	public void toXML(Element upper) {
		Document doc = upper.getOwnerDocument();
		Element ele = doc.createElement(this.getClass().getSimpleName());

		if (initializers != null)
			initializers.toXML(ele);

		upper.appendChild(ele);
	}

	public void setDefinedType(IType definedType) {
		this.definedType = definedType;
	}
}