package tiki;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Paths;
import java.util.logging.Logger;

import org.w3c.dom.Document;

import tiki.corePlugin.TDate;
import tiki.corePlugin.TMath;
import tiki.corePlugin.TObject;
import tiki.corePlugin.TString;
import tiki.corePlugin.TSystem;
import tiki.runtime.IComponent;
import tiki.syntax.Analyzer;
import tiki.tools.complier.parser.LexicalAnalyzer;
import tiki.tools.complier.parser.Parser;
import tiki.uitls.IOUtils;
import tiki.uitls.PackagenameUtils;

public class Tiki {
	private Parser parser;

	public Tiki() {
		this.parser = initializeParser();
	}

	public void compilePlugin(String name, IComponent com) throws IOException {
		InputStream declaration = com.getPluginDeclaration(name);

		Analyzer analyzer = new Analyzer(parser);
		Document xml = analyzer.analyze(declaration, name);

		if (xml != null) {
			String env = System.getProperty("tiki.env");
			String localName = PackagenameUtils.toPath(name) + ".xml";
			File lib = Paths.get(env, "lib", localName).toFile();
			lib.getParentFile().mkdirs();
			IOUtils.toFile(IOUtils.toString(xml), lib);
		}
	}

	public void buildEnv() throws IOException {
		compilePlugin("tiki.Object", new TObject());
		compilePlugin("tiki.String", new TString());
		compilePlugin("tiki.util.System", new TSystem());
		compilePlugin("tiki.util.Math", new TMath());
		compilePlugin("tiki.util.Date", new TDate());
	}

	public void compile(File sourceDir, File sourceFile) {
		Analyzer analyzer = new Analyzer(parser);
		String packageName = getSrcPackageName(sourceDir, sourceFile);

		try {
			Logger.getGlobal().info("compile---" + sourceFile.getAbsolutePath() + " as " + packageName);
			Document xml = analyzer.analyze(new FileInputStream(sourceFile), packageName);
			if (xml != null) {
				String path = IOUtils.removeExtension(sourceFile.getAbsolutePath()) + ".xml";
				IOUtils.toFile(IOUtils.toString(xml),  new File(path));
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	IComponent createExternalPlugin(URL jarUrl, String classToLoad, String componentName) {
		URLClassLoader cl = null;
		try {
			cl = new URLClassLoader(new URL[] { jarUrl });
			Class<?> loadedClass = cl.loadClass(classToLoad);
			Constructor<?> ctor = loadedClass.getDeclaredConstructor();

			ctor.setAccessible(true);
			IComponent com = (IComponent) ctor.newInstance();
			return com;
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (cl != null)
				try {
					cl.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
		}
		return null;
	}

	IComponent createPlugin(String classToLoad) {
		try {
			Class<?> loadedClass = Class.forName(classToLoad);
			Constructor<?> ctor = loadedClass.getDeclaredConstructor();

			ctor.setAccessible(true);
			IComponent com = (IComponent) ctor.newInstance();
			return com;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	private String getSrcPackageName(File dir, File file) {
		String srcAbsolutePath = dir.getAbsolutePath();

		String localPath = file.getAbsolutePath().substring(srcAbsolutePath.length() + 1);
		String packageName = IOUtils.removeExtension(localPath).replace('/', '.').replace('\\', '.');
		return packageName;
	}

	Parser initializeParser() {
		try {
			InputStream dfa = new FileInputStream("dfa.xml");
			InputStream lalr = new FileInputStream("lalr.xml");

			int debugSourceSnippetLineRange = 3;
			return new Parser(lalr, new LexicalAnalyzer(dfa, debugSourceSnippetLineRange));
		} catch (Exception e) {
			throw new IllegalArgumentException("initializeParser failed." + e.getMessage());
		}
	}
}
