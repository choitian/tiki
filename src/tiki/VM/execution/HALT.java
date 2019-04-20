package tiki.VM.execution;

import tiki.VM.Instruction;
import tiki.VM.Memory;
import tiki.VM.Register;
import tiki.VM.VirtualMachine;

public class HALT implements IExecution {
	@Override
	public void execute(VirtualMachine VM, Register reg, Memory mem, Instruction ins) {
		reg.setIP(reg.getIP() - 1);
		VM.halt();
	}
}
