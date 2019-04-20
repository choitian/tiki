package tiki.syntax.instruction;

import java.util.ArrayList;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class Instruction {
	private ArrayList<Address> operands;
	Operator operator;
	int ordinal;

	public Instruction(Operator operator, Address... values) {
		this.operator = operator;
		this.operands = new ArrayList<Address>();
		for (Address value : values) {
			this.operands.add(value);
		}
	}

	public Address getOperand(int index) {
		return operands.get(index);
	}


	public Operator getOperator() {
		return operator;
	}

	public int getOrdinal() {
		return ordinal;
	}

	public void setOperand(int index, Address value) {
		operands.set(index, value);
	}

	public void setOrdinal(int i) {
		this.ordinal = i;
	}

	public void toBuffer(StringBuffer buffer) {
		buffer.append(toString());
	}

	@Override
	public String toString() {
		String format = "%10s %s\n";

		StringBuffer operandsSTR = new StringBuffer();
		for (Address operand : operands) {
			operandsSTR.append(String.format("%15s ", operand.toString()));
		}
		return String.format(format, operator.toString(), operandsSTR.toString());
	}

	public Element toXML(Element upper) {
		Document doc = upper.getOwnerDocument();
		Element e = doc.createElement("ins");
		String instruction = operator.name();

		for (Address operand : operands) {
			instruction += " " + operand.toString();
		}
		e.setTextContent(instruction);

		return e;
	}
}
