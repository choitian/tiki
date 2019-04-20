package tiki.syntax;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.logging.Logger;
import java.util.zip.Deflater;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.InflaterInputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import tiki.syntax.instruction.Address;
import tiki.syntax.instruction.Operator;
import tiki.syntax.node.ListNode;
import tiki.syntax.node.SyntaxNode;
import tiki.syntax.node.decl_class_definition;
import tiki.syntax.node.decl_import;
import tiki.syntax.node.decl_method_definition;
import tiki.syntax.node.decl_unit;
import tiki.syntax.scope.Symbol;
import tiki.syntax.type.IType;
import tiki.syntax.type.TypeFactory;
import tiki.tools.complier.parser.ParseTree;
import tiki.tools.complier.parser.Parser;
import tiki.uitls.Formator;
import tiki.uitls.IOUtils;
import tiki.uitls.PackagenameUtils;

public class Analyzer {
	private LinkedHashMap<String, Clazz> definedClasses;
	private int errorTotal;
	private ArrayList<String> importPackages;
	private Parser parser;
	private SymbolManager symbolManager;

	private String decompressString(String str) {
		InflaterInputStream inf = null;
		String decompressed = null;
		try {
			byte[] decoded = Formator.h2b(str);
			inf = new InflaterInputStream(new ByteArrayInputStream(decoded));
			decompressed = IOUtils.toString(inf);
			inf.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return decompressed;
	}
	private InputStream getSource(InputStream xml) {
		try {
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			DocumentBuilder db = dbf.newDocumentBuilder();
			Document doc = db.parse(xml);

			String source = doc.getElementsByTagName("source").item(0).getTextContent();
			source = decompressString(source);
			
			return new ByteArrayInputStream(source.getBytes("UTF-8"));
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
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
	public InputStream findSourcePackage(String name) {
		InputStream packageIn;

		String localName = PackagenameUtils.toPath(name) + ".t";
		String src = System.getProperty("tiki.src");
		if (src != null && !src.isEmpty()) {
			String path = Paths.get(src, localName).toString();
			packageIn = openPackage(path);
			if (packageIn != null)
				return packageIn;
		}

		localName = PackagenameUtils.toPath(name) + ".xml";
		String libs = System.getProperty("tiki.libs");
		if (libs != null && !libs.isEmpty()) {
			for (String lib : libs.split(";")) {
				lib = lib.trim();
				if (!lib.isEmpty()) {
					lib = Paths.get(lib, localName).toString();
					packageIn = openPackage(lib);
					if (packageIn != null) {
						return getSource(packageIn);
					}
				}
			}
		}
		String env = System.getProperty("tiki.env");
		String lib = Paths.get(env, "lib", localName).toString();
		packageIn = openPackage(lib);
		if (packageIn != null) {
			return getSource(packageIn);
		}
		return null;
	}
	public Analyzer(Parser parser) {
		this.parser = parser;
		this.symbolManager = new SymbolManager(this);

		this.definedClasses = new LinkedHashMap<String, Clazz>();
		this.importPackages = new ArrayList<String>();
	}

	public Clazz addDefinedClass(IType type, String position) {
		String name = type.getName();
		Clazz clazz = new Clazz(type);
		definedClasses.put(name, clazz);
		return clazz;
	}

	public Document analyze(InputStream source, String packageName) {
		PackageScope curPKG = symbolManager.entryPackageScope(packageName);

		String content = null;
		try {
			content = IOUtils.toString(source, "utf-8");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		decl_unit unit = translateUnit(content);
		
		try {
			IOUtils.toFile(IOUtils.toString(unit.getTree().toXML()), Paths.get(System.getProperty("tiki.log"), packageName + ".syntaxTree.xml").toFile());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		checkUnitClass(curPKG, unit, packageName);
		// definition
		Clazz clazz = definedClasses.values().iterator().next();
		if (clazz != null) {
			symbolManager.enterClassScope(clazz);
			for (decl_method_definition md : clazz.getDeclMethodDefinitions()) {
				md.checkBody(this, this.symbolManager);
			}
			symbolManager.exitClassScope();
		}
		// to xml Result.
		if (errorTotal > 0)
			return null;
		Document xml = toXMLResult(content, importPackages, clazz);
		return xml;
	}

	private void checkUnitClass(PackageScope pkg, decl_unit unit, String packageName) {
		LinkedList<SyntaxNode> class_definitions = unit.getClass_definitions().nodes();
		decl_class_definition class_definition;
		if (class_definitions.size() > 1) {
			String msg = String.format("only one type/class can defined in every file");
			error(msg, unit.getPosition());
		}
		class_definition = (decl_class_definition) class_definitions.get(0);

		if (packageName.isEmpty())
			pkg.setName(class_definition.getName());

		if (!pkg.getName().endsWith(class_definition.getName())) {
			String msg = String.format("type/class must defined in its own file with same name");
			error(msg, unit.getPosition());
		}

		// set package map
		ListNode imports = unit.getImports();
		if (imports != null) {
			for (SyntaxNode node : imports.nodes()) {
				decl_import im = (decl_import) node;
				pkg.addImport(im.getPackage_name());
			}
		}
		// declare class member
		class_definition.check();
	}

	private String compressString(String str) {
		DeflaterOutputStream def = null;
		String compressed = null;
		try {
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			def = new DeflaterOutputStream(out, new Deflater(Deflater.BEST_COMPRESSION));
			def.write(str.getBytes());
			def.close();
			compressed = Formator.b2h(out.toByteArray());
		} catch (Exception e) {
			e.printStackTrace();
		}
		return compressed;
	}

	public void emit(Operator operator, Evalue... evalues) {
		Address[] adds = new Address[evalues.length];
		for (int i = 0; i < evalues.length; i++) {
			adds[i] = evalues[i].getAddress();
		}
		symbolManager.getCurrentMethod().ic.getLastBlock().emit(operator, adds);
	}

	public Evalue emit(Operator operator, IType resultType, Evalue... evalues) {
		Address[] adds = new Address[1 + evalues.length];
		Evalue result = newTempEvalue(resultType);

		adds[0] = result.getAddress();
		for (int i = 0; i < evalues.length; i++) {
			adds[1 + i] = evalues[i].getAddress();
		}
		symbolManager.getCurrentMethod().ic.getLastBlock().emit(operator, adds);

		return result;
	}

	public void error(String msg, String position) {
		errorTotal++;
		String log = String.format("%s [%s %s]", msg, symbolManager.currentPackageScope().getName(), position);
		
		System.out.println(log);
		Logger.getGlobal().severe(log);
	}

	public Clazz getDefinedClass(String name) {
		if (!definedClasses.containsKey(name)) {
			importPackage(name);
		}
		return definedClasses.get(name);
	}

	public int getErrorTotal() {
		return errorTotal;
	}

	public SymbolManager getSymbolManager() {
		return symbolManager;
	}

	public IType getTypeClass(String name) {
		String packageName = name;

		if (!name.contains(".")) {
			if (symbolManager.currentPackageScope().importMap.containsKey(name)) {
				packageName = symbolManager.currentPackageScope().importMap.get(name);
			} else {
				packageName = PackagenameUtils
						.concat(PackagenameUtils.getParent(symbolManager.currentPackageScope().getName()), name);
			}
		}
		if (symbolManager.currentPackageScope().getName().equals(packageName)) {
			return TypeFactory.getClass(packageName);
		}
		InputStream pkg = findSourcePackage(packageName);
		if (pkg != null) {
			return TypeFactory.getClass(packageName);
		}
		return null;
	}

	private void importPackage(String packageName) {
		InputStream pkg = findSourcePackage(packageName);
		if (pkg == null)
			return;
		importPackages.add(packageName);
		PackageScope curPKG = symbolManager.entryPackageScope(packageName);

		String content = null;
		try {
			content = IOUtils.toString(pkg, "utf-8");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		decl_unit unit = translateUnit(content);
		checkUnitClass(curPKG, unit, packageName);

		symbolManager.exitPackageScope();
	}

	public Evalue newTempEvalue(IType type) {
		Symbol temp = symbolManager.newTemp(type);
		return Evalue.newVariable(temp.getOffset(), type);
	}
	public ParseTree toParseTree(String content) {
		ParseTree ptree = null;
		try {
			ptree = parser.Parse(content);
		} catch (Exception e) {
			e.printStackTrace();
		}
		if (ptree == null) {
			String msg = "parse failed.";
			error(msg, "analyzer");
			return null;
		}
		return ptree;
	}

	private SyntaxTree toSyntaxTree(String content) {
		ParseTree ptree = toParseTree(content);
		SyntaxTree stree = new SyntaxTree(ptree);
		return stree;
	}

	private Document toXMLResult(String content, ArrayList<String> importPackages, Clazz clazz) {
		try {
			DocumentBuilderFactory doc_builder_factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder doc_builder = doc_builder_factory.newDocumentBuilder();
			Document doc = doc_builder.newDocument();

			Element root = doc.createElement("package");
			doc.appendChild(root);

			Element source_e = doc.createElement("source");
			source_e.setTextContent(compressString(content));
			root.appendChild(source_e);

			for (String pkg : importPackages) {

				Element import_e = doc.createElement("import");
				Attr attr = doc.createAttribute("package");
				attr.setValue(pkg);
				import_e.setAttributeNode(attr);

				root.appendChild(import_e);
			}
			Element classElement = clazz.toXML(root);
			root.appendChild(classElement);

			return doc;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	private decl_unit translateUnit(String content) {
		SyntaxTree stree = toSyntaxTree(content);
		stree.setAnalyzer(this);
		return stree.getUnit();
	}

	public void warning(String msg, String position) {
		String log = String.format("%s [%s %s]", msg, symbolManager.currentPackageScope().getName(), position);
		Logger.getGlobal().warning(log);
	}
}
