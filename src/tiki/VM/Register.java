package tiki.VM;

public class Register {
	int AP;
	int BP;
	int IP;
	int SP;

	public int getAP() {
		return AP;
	}

	public int getBP() {
		return BP;
	}

	public int getIP() {
		return IP;
	}

	public int getSP() {
		return SP;
	}

	public void setAP(int value) {
		AP = value;
	}

	public void setBP(int value) {
		BP = value;
	}

	public void setIP(int value) {
		IP = value;
	}

	public void setSP(int value) {
		SP = value;
	}
}