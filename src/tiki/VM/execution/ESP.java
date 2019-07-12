package tiki.VM.execution;

import tiki.VM.Address;
import tiki.VM.Instruction;
import tiki.VM.Memory;
import tiki.VM.Register;
import tiki.VM.VirtualMachine;

public class ESP implements IExecution {
	public static void doExecute(VirtualMachine VM, Register reg, Memory mem,Address result) {
		// bp_up
		// ap_up
		// is VARARGS
		// retvAdd
		// next_ins
		// -------------------ap
		// p2
		// p1
		// p0
		// current object
		// -------------------bp
		// ap = ap_up
		// bp = bp_up
		// set retv
		int retvAdd = mem.getMemAddress(result);
		mem.push(String.valueOf(reg.getBP())); // bp_up
		mem.push(String.valueOf(reg.getAP())); // ap_up		
		mem.push(mem.read(Address.FALSE)); // is VARARGS
		mem.push(String.valueOf(retvAdd)); // retvAdd
		mem.push(mem.read(Address.NULL)); // next_ins
		reg.setAP(reg.getSP());
	}

	@Override
	public void execute(VirtualMachine VM, Register reg, Memory mem, Instruction ins) {
		Address result = ins.getOperand(0);
		doExecute(VM,reg, mem,result);
	}
}