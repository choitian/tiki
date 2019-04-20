package tiki.VM.execution;

import tiki.VM.Address;
import tiki.VM.Instruction;
import tiki.VM.Memory;
import tiki.VM.Register;
import tiki.VM.VirtualMachine;

public class EQUAL_R implements IExecution {

	@Override
	public void execute(VirtualMachine VM, Register reg, Memory mem, Instruction ins) {
		Address ad0 = ins.getOperand(0);
		Address ad1 = ins.getOperand(1);
		Address ad2 = ins.getOperand(2);

		String op1 = mem.read(ad1);
		String op2 = mem.read(ad2);
		String result;
		if (op1 == null || op2 == null) {
			result = String.valueOf(op1 == op2);
		} else {
			result = String.valueOf(op1.equals(op2));
		}
		mem.write(ad0, result);
	}
}
