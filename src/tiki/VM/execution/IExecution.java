package tiki.VM.execution;

import tiki.VM.Instruction;
import tiki.VM.Memory;
import tiki.VM.Register;
import tiki.VM.VirtualMachine;

public interface IExecution {
	void execute(VirtualMachine VM, Register reg, Memory mem, Instruction ins);
}
