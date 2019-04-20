package tiki.VM.execution;

import tiki.VM.Address;
import tiki.VM.Instruction;
import tiki.VM.Memory;
import tiki.VM.Register;
import tiki.VM.VirtualMachine;

public class SUB_I implements IExecution {

	@Override
	public void execute(VirtualMachine VM, Register reg, Memory mem, Instruction ins) {
		Address ad0 = ins.getOperand(0);
		Address ad1 = ins.getOperand(1);
		Address ad2 = ins.getOperand(2);

		String result = String.valueOf(mem.readInteger(ad1) - mem.readInteger(ad2));
		mem.write(ad0, result);
	}
}
