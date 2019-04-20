package tiki.syntax.instruction;

import java.util.LinkedList;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import tiki.syntax.instruction.Address;

public class InstructionBlock {

	private InstructionSection ic;
	private LinkedList<Instruction> instructions;
	private String label;

	public InstructionBlock(InstructionSection ic, String label) {
		this.label = label;
		this.ic = ic;
		instructions = new LinkedList<Instruction>();
	}

	public void emit(Operator operator, Address... values) {
		Instruction i = new Instruction(operator, values);
		instructions.add(i);
	}

	public InstructionSection getIc() {
		return ic;
	}

	public LinkedList<Instruction> getInstructions() {
		return instructions;
	}

	public String getLabel() {
		return label;
	}

	public void setIc(InstructionSection ic) {
		this.ic = ic;
	}

	public void setInsList(LinkedList<Instruction> insList) {
		this.instructions = insList;
	}

	public void start() {
		ic.getBlocks().add(this);
	}

	public void toBuffer(StringBuffer buffer) {
		buffer.append(String.format("%s:\n", label));
		for (Instruction ins : instructions) {
			ins.toBuffer(buffer);
		}
	}

	public Element toXML(Element upper) {
		Document doc = upper.getOwnerDocument();
		Element ib = doc.createElement("block");

		Attr attr = doc.createAttribute("label");
		attr.setValue(label);
		ib.setAttributeNode(attr);

		for (Instruction ins : instructions) {
			Element ins_e = ins.toXML(ib);
			ib.appendChild(ins_e);
		}

		return ib;
	}
}