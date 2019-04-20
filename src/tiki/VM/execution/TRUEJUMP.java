package tiki.VM.execution;

import tiki.VM.Instruction;
import tiki.VM.Memory;
import tiki.VM.Register;
import tiki.VM.VirtualMachine;

public class TRUEJUMP implements IExecution {
	@Override
	public void execute(VirtualMachine VM, Register reg, Memory mem, Instruction ins) {
		int offset = mem.readInteger(ins.getOperand(0));
		boolean test = mem.readBoolean(ins.getOperand(1));

		if (test) {
			reg.setIP(reg.getIP() + offset - 1);
		}
	}
}
