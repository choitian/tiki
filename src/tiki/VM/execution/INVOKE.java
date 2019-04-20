package tiki.VM.execution;

import tiki.VM.Address;
import tiki.VM.Instruction;
import tiki.VM.Memory;
import tiki.VM.Register;
import tiki.VM.VirtualMachine;
import tiki.runtime.IRuntime;

public class INVOKE implements IExecution {
	public static void doExecute(VirtualMachine VM, Register reg, Memory mem, Address base, int entry) {
		mem.push(mem.read(base));
		reg.setBP(reg.getSP());

		mem.write(reg.getAP() + IRuntime.ADD__next_ins, String.valueOf(reg.getIP()));
		reg.setIP(entry);
	}

	@Override
	public void execute(VirtualMachine VM, Register reg, Memory mem, Instruction ins) {
		Address base = ins.getOperand(0);
		String entry = mem.read(ins.getOperand(1));

		doExecute(VM, reg,mem, base, Integer.parseInt(entry));
	}
}
