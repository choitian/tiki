package tiki.VM.execution;

import java.util.ArrayList;
import java.util.List;

import tiki.VM.Address;
import tiki.VM.Instruction;
import tiki.VM.Memory;
import tiki.VM.Register;
import tiki.VM.VirtualMachine;

public class NEWA implements IExecution {
	private String typeEncoding;

	@Override
	public void execute(VirtualMachine VM, Register reg, Memory mem, Instruction ins) {
		List<Address> ops = ins.getOperands();
		Address result = ops.get(0);
		typeEncoding = mem.read(ins.getOperand(1));
		List<Address> indexs = ops.subList(2, ops.size());

		int base = newArray(VM, mem, ins, indexs);
		mem.write(result, String.valueOf(base));
	}

	private int newArray(VirtualMachine VM, Memory mem, Instruction ins, List<Address> indexs) {
		int number_of_element = mem.readInteger(indexs.get(0));

		if (indexs.size() > 1) {
			ArrayList<Integer> refs = new ArrayList<Integer>();
			for (int i = 0; i < number_of_element; i++) {

				int ref = newArray(VM, mem, ins, indexs.subList(1, indexs.size()));
				refs.add(ref);
			}

			int address = mem.userAlloc(number_of_element + 1);
			mem.write(address, String.valueOf(number_of_element));
			for (int i = 0; i < number_of_element; i++) {
				mem.write(address + i + 1, String.valueOf(refs.get(i)));
			}

			return address+1;
		} else {
			int address = mem.userAlloc(number_of_element + 1);
			mem.write(address, String.valueOf(number_of_element));

			for (int i = 0; i < number_of_element; i++) {
				mem.write(address+1+i, mem.getDefault(typeEncoding));
			}

			return address+1;
		}

	}
}
