package tiki.VM.execution;

import tiki.VM.Instruction;
import tiki.VM.Memory;
import tiki.VM.Register;
import tiki.VM.VirtualMachine;
import tiki.runtime.IRuntime;

public class RETV implements IExecution {
	@Override
	public void execute(VirtualMachine VM, Register reg, Memory mem, Instruction ins) {
		if (!ins.getOperands().isEmpty()) {
			int retvAdd = mem.readInteger(reg.getAP() + IRuntime.ADD__retv);
			if (retvAdd != -1)
				mem.write(retvAdd, mem.read(ins.getOperand(0)));
		}
	}
}
