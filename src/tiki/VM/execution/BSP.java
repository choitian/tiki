package tiki.VM.execution;

import tiki.VM.Instruction;
import tiki.VM.Memory;
import tiki.VM.Register;
import tiki.VM.VirtualMachine;
import tiki.runtime.IRuntime;

public class BSP implements IExecution {
	public static void doExecute(VirtualMachine VM, Register reg,Memory mem) {
		reg.setSP(reg.getAP());
		mem.pop();//next_ins
		mem.pop();//retvAdd
		mem.pop();// is VARARGS
		reg.setBP(Integer.parseInt(mem.pop()));//bp_up
		reg.setAP(Integer.parseInt(mem.pop()));//ap_up
		
		//reg.setSP(Integer.parseInt(sp_up));
		//reg.setAP(Integer.parseInt(ap_up));
		//reg.setBP(Integer.parseInt(bp_up));
	}

	@Override
	public void execute(VirtualMachine VM, Register reg, Memory mem, Instruction ins) {
		doExecute(VM, reg,mem);
	}
}
