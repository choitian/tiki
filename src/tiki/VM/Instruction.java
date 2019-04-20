package tiki.VM;

import java.util.ArrayList;

public class Instruction {
	private ArrayList<Address> operands;
	Operator operator;
	public Instruction(String code) {
		String[] parts = code.split("\\s+");
		String op = parts[0];
		try {
			this.operator = Operator.valueOf(op);
		} catch (Exception e) {
			e.printStackTrace();
		}
		this.operands = new ArrayList<Address>();
		for (int i = 1; i < parts.length; i++) {

			Address operand = new Address(parts[i]);
			this.operands.add(operand);
		}
	}

	public Address getOperand(int index) {
		return operands.get(index);
	}

	public ArrayList<Address> getOperands() {
		return operands;
	}

	public Operator getOperator() {
		return operator;
	}

	@Override
	public String toString() {
		String format = "%10s %s\n";

		StringBuffer operandsSTR = new StringBuffer();
		for (Address operand : operands) {
			operandsSTR.append(String.format("%15s ", operand.toString()));
		}
		return String.format(format, operator.toString(), operandsSTR.toString());
	}
}
