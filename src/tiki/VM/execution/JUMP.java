package tiki.VM.execution;

import tiki.VM.Instruction;
import tiki.VM.Memory;
import tiki.VM.Register;
import tiki.VM.VirtualMachine;

public class JUMP implements IExecution {
	@Override
	public void execute(VirtualMachine VM, Register reg, Memory mem, Instruction ins) {
		int offset = mem.readInteger(ins.getOperand(0));

		reg.setIP(reg.getIP() + offset - 1);
	}
}
