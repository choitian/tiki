package tiki.VM.execution;

import java.util.List;

import tiki.VM.Address;
import tiki.VM.Instruction;
import tiki.VM.Memory;
import tiki.VM.Register;
import tiki.VM.VirtualMachine;

public class LOCAL implements IExecution {

	@Override
	public void execute(VirtualMachine VM, Register reg, Memory mem, Instruction ins) {
		List<Address> operands = ins.getOperands();
		for (Address operand : operands) {
			String type = mem.read(operand);
			String value = mem.getDefault(type);
			mem.push(value);
		}
	}
}
