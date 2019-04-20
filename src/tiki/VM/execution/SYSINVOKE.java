package tiki.VM.execution;

import tiki.VM.Instruction;
import tiki.VM.Memory;
import tiki.VM.Register;
import tiki.VM.VirtualMachine;
import tiki.runtime.IRuntime;

public class SYSINVOKE implements IExecution {
	@Override
	public void execute(VirtualMachine VM, Register reg, Memory mem, Instruction ins) {
		String method = mem.read(ins.getOperand(0));
		VM.invoke(method);

		if (reg.getAP() != 0) {
			reg.setIP(Integer.parseInt(mem.read(reg.getAP() + IRuntime.ADD__next_ins)));
		}
	}
}
