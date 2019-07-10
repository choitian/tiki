package tiki.VM;

import java.util.HashMap;

import tiki.runtime.IRuntime;

interface IRAM {
	int alloc(int size);

	String read(int address);

	void write(int address, String value);
}

public class Memory implements IRuntime {
	private IRAM ram;
	private MemorySection SYSTEM;
	private MemorySection USER;
	private VirtualMachine VM;

	Memory(VirtualMachine VM) {
		this.VM = VM;
		ram = new RAM__Map();

		USER = new MemorySection(ram.alloc(0x40000), 0x40000);
		SYSTEM = new MemorySection(ram.alloc(0x40000), 0x40000);
	}

	public void freeUSER() {
		USER.free();
	}

	public String getDefault(String type) {
		switch (type) {
		case "I":
			return String.valueOf(0);
		case "F":
			return String.valueOf(0);
		case "B":
			return String.valueOf(false);
		case "R":
			return read(Address.NULL);
		default:
			throw new RuntimeException(String.format("unkown memory value type '%s'", type));
		}
	}

	@Override
	public int getParameterAddress(int index) {
		return this.VM.getRegister().BP - 1 - index;
	}

	@Override
	public int getAp() {
		return this.VM.getRegister().AP;
	}

	public String pop() {
		return read(VM.getRegister().SP--);
	}

	public void push(String value) {
		write(++VM.getRegister().SP, value);
	}

	public int getMemAddress(Address address) {
		if (address.isVariable()) {
			return VM.getRegister().getBP() + Integer.parseInt(address.getValue());
		} else if (address.isReference()) {
			int add = this.readInteger(VM.getRegister().getBP() + Integer.parseInt(address.getValue()));
			return add;
		} else if (address.isClass()) {
			int add = VM.lookupClass(address.getValue());
			return add;
		}
		return -1;
	}
	public String read(Address address) {
		if (address.isConstant())
			return address.getValue();
		else {
			int add = getMemAddress(address);
			return this.read(add);
		}
	}
	@Override
	public String read(int memAddress) {
		return ram.read(memAddress);
	}

	public boolean readBoolean(Address address) {
		return Boolean.parseBoolean(read(address));
	}

	@Override
	public boolean readBoolean(int memAddress) {
		return Boolean.parseBoolean(read(memAddress));
	}

	public float readFloat(Address address) {
		return Float.parseFloat(read(address));
	}

	@Override
	public float readFloat(int memAddress) {
		return Float.parseFloat(read(memAddress));
	}

	public int readInteger(Address address) {
		return Integer.parseInt(read(address));
	}

	@Override
	public int readInteger(int memAddress) {
		return Integer.parseInt(read(memAddress));
	}

	public int systemAlloc(int size) {
		return SYSTEM.alloc(size);
	}

	public int userAlloc(int size) {
		return USER.alloc(size);
	}

	public void write(Address address, String value) {
		this.write(this.getMemAddress(address), value);
	}

	@Override
	public void write(int memAddress, String value) {
		ram.write(memAddress, value);
	}
}

class MemorySection {
	private int base;
	private int offset;
	private int size;

	MemorySection(int base, int size) {
		this.base = base;
		this.size = size;
		this.offset = base;
	}

	public int alloc(int size) {
		int add = offset;
		offset += size;
		if (offset > (base + this.size)) {
			throw new RuntimeException("out of memory");
		}
		return add;
	}

	public void free() {
		offset = base;
	}
}

class RAM__Map implements IRAM {
	private int offset;
	private HashMap<Integer, String> space;

	RAM__Map() {
		space = new HashMap<Integer, String>();
	}

	@Override
	public int alloc(int size) {
		int add = offset;
		offset += size;
		return add;
	}

	@Override
	public String read(int address) {
		return space.get(address);
	}

	@Override
	public void write(int address, String value) {
		space.put(address, value);
	}
}