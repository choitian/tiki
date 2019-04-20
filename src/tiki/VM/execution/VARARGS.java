package tiki.VM.execution;

import java.util.List;

import tiki.VM.Address;
import tiki.VM.Instruction;
import tiki.VM.Memory;
import tiki.VM.Register;
import tiki.VM.VirtualMachine;
import tiki.runtime.IRuntime;

public class VARARGS implements IExecution {
	@Override
	public void execute(VirtualMachine VM, Register reg, Memory mem, Instruction ins) {
		List<Address> operands = ins.getOperands();

		mem.write(reg.getAP() + IRuntime.ADD__is_VARARGS, mem.read(Address.TRUE));

		mem.push(String.valueOf(operands.size()));
		for (Address arg : operands) {
			mem.push(mem.read(arg));
		}
		mem.push(String.valueOf(reg.getSP() - operands.size()));
	}
}
