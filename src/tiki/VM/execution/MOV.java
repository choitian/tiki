package tiki.VM.execution;

import tiki.VM.Address;
import tiki.VM.Instruction;
import tiki.VM.Memory;
import tiki.VM.Register;
import tiki.VM.VirtualMachine;

public class MOV implements IExecution {
	@Override
	public void execute(VirtualMachine VM, Register reg, Memory mem, Instruction ins) {
		Address result = ins.getOperand(0);
		Address op1 = ins.getOperand(1);

		mem.write(result, mem.read(op1));
	}
}
