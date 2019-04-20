package tiki.syntax.instruction;

import java.util.Collection;
import java.util.LinkedList;

import org.w3c.dom.Element;

public class InstructionSection {

	private LinkedList<InstructionBlock> insBlockList;

	public InstructionSection() {
		this.insBlockList = new LinkedList<InstructionBlock>();
	}

	public Collection<InstructionBlock> getBlocks() {
		return insBlockList;
	}

	public InstructionBlock getLastBlock() {
		return insBlockList.getLast();
	}

	public void toXML(Element upper) {
		for (InstructionBlock block : insBlockList) {
			Element block_e = block.toXML(upper);
			upper.appendChild(block_e);
		}
	}
}