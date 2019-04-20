package tiki.VM.execution;

import tiki.VM.Address;
import tiki.VM.Instruction;
import tiki.VM.Memory;
import tiki.VM.Register;
import tiki.VM.VirtualMachine;

public class ARG implements IExecution {
	@Override
	public void execute(VirtualMachine VM, Register reg, Memory mem, Instruction ins) {
		Address argument = ins.getOperand(0);
		mem.push(mem.read(argument));
	}
}
