package tiki.VM.execution;

import java.util.List;

import tiki.VM.Address;
import tiki.VM.Instruction;
import tiki.VM.Memory;
import tiki.VM.Register;
import tiki.VM.VirtualMachine;

public class NEW implements IExecution {
	@Override
	public void execute(VirtualMachine VM, Register reg, Memory mem, Instruction ins) {
		List<Address> ops = ins.getOperands();
		Address result = ops.get(0);
		int size = mem.readInteger(ops.get(1));

		int address = mem.userAlloc(size);
		mem.write(result, String.valueOf(address));
		for (int i = 0; i < size; i++) {
			mem.write(address+i, String.valueOf(0));
		}
	}
}
