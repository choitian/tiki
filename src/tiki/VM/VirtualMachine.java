package tiki.VM;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Logger;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import tiki.VM.execution.BSP;
import tiki.VM.execution.ESP;
import tiki.VM.execution.IExecution;
import tiki.VM.execution.INVOKE;
import tiki.runtime.PluginManager;
import tiki.uitls.PackagenameUtils;

public class VirtualMachine {
	private LinkedHashMap<String, Integer> classTable;
	private LinkedHashMap<String, IExecution> executionTab;
	private AtomicBoolean halted;
	private int mainEntry;
	private Memory memory;
	private LinkedHashMap<String, String> packageTable;
	private PluginManager pluginManager;
	private Register register;
	private int systemBase;

	public VirtualMachine(PluginManager pluginManager) {
		this.classTable = new LinkedHashMap<String, Integer>();
		this.packageTable = new LinkedHashMap<String, String>();
		this.pluginManager = pluginManager;
		initializeTxecutionTab();

		this.register = new Register();
		this.memory = new Memory(this);

		this.register.setSP(memory.userAlloc(0x20000));
		
		this.systemBase = memory.systemAlloc(0x4000);
		this.memory.write(systemBase, Operator.HALT.name());
		this.register.IP = systemBase;
		this.mainEntry = systemBase;
	}

	private void execute(Instruction ins) {
		IExecution execution = executionTab.get(ins.getOperator().name());
		if (execution != null) {
			execution.execute(this, register, memory, ins);
		} else {
			Logger.getGlobal().info(String.format("unsupport operator: %s", ins.getOperator().name()));
		}
	}

	private Instruction getInstruction() {
		String inss = memory.read(register.IP++);
		Instruction ins = new Instruction(inss);

		return ins;
	}

	Register getRegister() {
		return register;
	}

	private synchronized void gotoEntry(int entry) {
		ESP.doExecute(this, register,memory,Address.NULL);
		INVOKE.doExecute(this,register,memory, Address.NULL, entry);

		// begin
		halted = new AtomicBoolean(false);
		while (true) {
			Instruction ins = getInstruction();
			execute(ins);
			if (halted.get()) {
				break;
			}
		}
		BSP.doExecute(this, register,memory);
	}

	public void halt() {
		halted.set(true);
	}
	InputStream openPackage(String path) {
		File file = new File(path);
		if (file.exists())
			try {
				return new FileInputStream(file);
			} catch (FileNotFoundException e) {
			}
		return null;
	}
	private InputStream findXmlPackage(String name) {
		InputStream packageIn;
		String localName = PackagenameUtils.toPath(name) + ".xml";

		String libs = System.getProperty("tiki.libs");
		if (libs != null && !libs.isEmpty()) {
			for (String lib : libs.split(";")) {
				lib = lib.trim();
				if (!lib.isEmpty()) {
					lib = Paths.get(lib, localName).toString();
					packageIn = openPackage(lib);
					if (packageIn != null)
						return packageIn;
				}
			}
		}

		String env = System.getProperty("tiki.env");
		String lib = Paths.get(env, "lib", localName).toString();
		packageIn = openPackage(lib);
		if (packageIn != null)
			return packageIn;

		return null;
	}
	private boolean importPackage(String packageName) {
		if (packageTable.containsKey(packageName)) {
			Logger.getGlobal().info(String.format("package '%s' already loaded.", packageName));
			return true;
		}

		Logger.getGlobal().info("import---" + packageName);

		InputStream pkg = findXmlPackage(packageName);
		if (pkg == null)
			return false;

		try {
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			DocumentBuilder db = dbf.newDocumentBuilder();
			Document doc = db.parse(pkg);

			return loadPackage(doc);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}
	private ArrayList<Element> toElements(NodeList nodes) {
		ArrayList<Element> result = new ArrayList<Element>();
		for (int index = 0; index < nodes.getLength(); index++) {
			result.add((Element) nodes.item(index));
		}
		return result;
	}
	private void initializeTxecutionTab() {
		this.executionTab = new LinkedHashMap<String, IExecution>();
		for (Operator op : Operator.values()) {
			String name = op.name();
			try {
				String className = "tiki.VM.execution." + name;
				Class<?> clazz = Class.forName(className);
				Constructor<?> ctor = clazz.getConstructor();

				executionTab.put(name, (IExecution) ctor.newInstance());
			} catch (Exception e) {
				e.printStackTrace();
				Logger.getGlobal().info(String.format("unsupport operator: %s", name));
			}
		}
	}

	public void invoke(String methodName)
	{
		pluginManager.invoke(methodName, memory);
	}

	public boolean loadPackage(File file) {
		Logger.getGlobal().info("loadPackage---" + file.toString());
		try {
			InputStream pkg = new FileInputStream(file);
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			DocumentBuilder db = dbf.newDocumentBuilder();
			Document doc = db.parse(pkg);

			return loadPackage(doc);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;		
	}
	public boolean loadPackage(Document xml) {
		NodeList imports = xml.getElementsByTagName("import");
		for (Element import_ : toElements(imports)) {
			String packageName = import_.getAttribute("package");
			importPackage(packageName);
		}
		NodeList classes = xml.getElementsByTagName("class");
		for (Element class_e : toElements(classes)) {
			String className = class_e.getAttribute("name");
			if (this.classTable.containsKey(className)) {
				Logger.getGlobal().info(String.format("class '%s' already loaded.reload", className));
			}

			NodeList members = class_e.getElementsByTagName("member");
			int classBase = memory.systemAlloc(1 + members.getLength());
			this.classTable.put(className, classBase);

			memory.write(classBase, String.valueOf(classBase + 1));
			classBase++;

			for (int index = 0; index < members.getLength(); index++) {
				Element member = (Element) members.item(index);
				String kind = member.getAttribute("kind");

				if (kind.equals("field")) {
					memory.write(classBase++, memory.getDefault(member.getAttribute("type")));
				} else if (kind.equals("method")) {
					NodeList instructions = member.getElementsByTagName("ins");
					String methodName = member.getAttribute("name");

					if (instructions.getLength() == 0) {
						int insBase = memory.systemAlloc(1);
						memory.write(classBase++, String.valueOf(insBase));

						String scopeName = String.format("%s.%s", className, methodName);
						memory.write(insBase, String.format("%s %s", Operator.SYSINVOKE.name(), Address.newConstant(scopeName).toString()));
					} else {
						int insBase = memory.systemAlloc(instructions.getLength());
						memory.write(classBase++, String.valueOf(insBase));
						boolean entry = Boolean.parseBoolean(member.getAttribute("entry"));
						if (entry) {
							mainEntry = insBase;
						}

						for (int index1 = 0; index1 < instructions.getLength(); index1++) {
							Element instructionElement = (Element) instructions.item(index1);
							memory.write(insBase++, instructionElement.getTextContent());
						}
					}

				}
			}
		}
		return true;
	}

	Integer lookupClass(String className) {
		Integer entry = this.classTable.get(className);
		if (entry == null)
			throw new RuntimeException(String.format("class '%s' is not loaded.", className));
		return entry;
	}

	public void start() {
		gotoEntry(mainEntry);
		
		this.mainEntry = systemBase;
		this.memory.freeUSER();
		this.register.setSP(memory.userAlloc(0x20000));
	}
	public void run(Document xml) {
		loadPackage(xml);	
		start();
	}
}
