package tiki.VM.execution;

import tiki.VM.Instruction;
import tiki.VM.Memory;
import tiki.VM.Register;
import tiki.VM.VirtualMachine;
import tiki.runtime.IRuntime;

public class BSP implements IExecution {
	public static void doExecute(VirtualMachine VM, Register reg,Memory mem) {
		String sp_up = mem.read(reg.getAP() + IRuntime.ADD__sp_up);
		String ap_up = mem.read(reg.getAP() + IRuntime.ADD__ap_up);
		String bp_up = mem.read(reg.getAP() + IRuntime.ADD__bp_up);

		reg.setSP(Integer.parseInt(sp_up));
		reg.setAP(Integer.parseInt(ap_up));
		reg.setBP(Integer.parseInt(bp_up));
	}

	@Override
	public void execute(VirtualMachine VM, Register reg, Memory mem, Instruction ins) {
		doExecute(VM, reg,mem);
	}
}
