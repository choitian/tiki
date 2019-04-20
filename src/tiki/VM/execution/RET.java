package tiki.VM.execution;

import tiki.VM.Instruction;
import tiki.VM.Memory;
import tiki.VM.Register;
import tiki.VM.VirtualMachine;
import tiki.runtime.IRuntime;

public class RET implements IExecution {
	public static void doExecute(VirtualMachine VM, Register reg, Memory mem) {
		if (reg.getAP() != 0) {
			reg.setIP(Integer.parseInt(mem.read(reg.getAP() + IRuntime.ADD__next_ins)));
		}
	}

	@Override
	public void execute(VirtualMachine VM, Register reg, Memory mem, Instruction ins) {
		doExecute(VM, reg,mem);
	}
}
