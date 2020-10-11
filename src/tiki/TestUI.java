package tiki;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Control;
import javafx.scene.control.TextArea;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import tiki.Tiki;
import tiki.VM.VirtualMachine;
import tiki.corePlugin.TDate;
import tiki.corePlugin.TMath;
import tiki.corePlugin.TObject;
import tiki.corePlugin.TString;
import tiki.corePlugin.TSystem;
import tiki.runtime.PluginManager;
import tiki.tools.complier.lalr.LALR;
import tiki.tools.complier.lex.DFA;
import tiki.tools.complier.parser.AST;
import tiki.tools.complier.parser.LexicalAnalyzer;
import tiki.tools.complier.parser.ParseTree;
import tiki.tools.complier.parser.Parser;
import tiki.uitls.IOUtils;

public class TestUI extends Application {
	public static void main(String[] args) throws Exception {
		launch(args);

	}

	private static TextArea info;

	private static void log(String msg) {
		info.appendText(msg + "\n");

	}

	static Parser initializeParser() {
		try {
			InputStream dfa = new FileInputStream(Paths.get(System.getProperty("tiki.env"), "dfa.xml").toFile());

			InputStream dnf = Tiki.class.getResourceAsStream("/dnf_ast.txt");
			LALR lalr = new LALR();
			lalr.build(dnf);

			String xml = IOUtils.toString(lalr.toXML());
			InputStream lalr_is = new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8));

			int debugSourceSnippetLineRange = 3;
			return new Parser(lalr_is, new LexicalAnalyzer(dfa, debugSourceSnippetLineRange));
		} catch (Exception e) {
			throw new IllegalArgumentException("111 initializeParser failed." + e.getMessage());
		}
	}

	static private String getSrcPackageName(File dir, File file) {
		String srcAbsolutePath = dir.getAbsolutePath();

		String localPath = file.getAbsolutePath().substring(srcAbsolutePath.length() + 1);
		String packageName = IOUtils.removeExtension(localPath).replace('/', '.').replace('\\', '.');
		return packageName;
	}

	static void buildAST(File sourceDir, File sourceFile) {
		String packageName = getSrcPackageName(sourceDir, sourceFile);

		String content = null;
		try {
			content = IOUtils.toString(new FileInputStream(sourceFile), "utf-8");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Parser parser = initializeParser();
		ParseTree ptree = null;
		try {
			ptree = parser.Parse(content);
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		;
		if (ptree != null) {
			AST ast = new AST(ptree);
			try {
				IOUtils.toFile(IOUtils.toString(ast.toXML()),
						Paths.get(System.getProperty("tiki.log"), packageName + ".AST.xml").toFile());
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	static void testAST() throws Exception {
		String src = System.getProperty("tiki.src");
		File sourceDir = new File(src);
		Files.walk(sourceDir.toPath()).filter(path -> !Files.isDirectory(path))
				.filter(path -> path.toString().endsWith(".t")).forEach(path -> buildAST(sourceDir, path.toFile()));

	}
	
	static void clearParser() throws Exception {
		File dfaXML = Paths.get(System.getProperty("tiki.env"), "dfa.xml").toFile();
		dfaXML.delete();
		
		File lalrXML = Paths.get(System.getProperty("tiki.env"), "lalr.xml").toFile();
		lalrXML.delete();
		
		Logger.getGlobal().info("clearParser done.");
	}
	static void rebuildParser() throws Exception {
		InputStream re = Tiki.class.getResourceAsStream("/re.txt");
		InputStream dnf = Tiki.class.getResourceAsStream("/dnf.txt");
	
		DFA dfa = new DFA();
		dfa.Construct(re);
		Files.copy(new ByteArrayInputStream(IOUtils.toString(dfa.toXML()).getBytes()), Paths.get(System.getProperty("tiki.env"), "dfa.xml"), StandardCopyOption.REPLACE_EXISTING);

		LALR lalr = new LALR();
		lalr.build(dnf);
		Files.copy(new ByteArrayInputStream(IOUtils.toString(lalr.toXML()).getBytes()), Paths.get(System.getProperty("tiki.env"), "lalr.xml"), StandardCopyOption.REPLACE_EXISTING);
		
		Logger.getGlobal().info("rebuildParser done.");
	}

	static void compile(File sourceDir, File sourceFile) {
		Tiki tiki = new Tiki();
		tiki.compile(sourceDir, sourceFile);
	}
	static void clear() throws Exception {
		String Libs = System.getProperty("tiki.libs");
		String src = System.getProperty("tiki.src");
		if (src == null || src.isEmpty() || !new File(src).exists()) {
			throw new RuntimeException("missing 'tiki.src' or not exists");
		}
		Logger.getGlobal().info(String.format("tiki.src: %s", src));
		Logger.getGlobal().info(String.format("tiki.libs: %s", Libs == null ? "empty" : Libs));

		File sourceDir = new File(src);
		Files.walk(sourceDir.toPath()).filter(path -> !Files.isDirectory(path))
				.filter(path -> path.toString().endsWith(".xml")).forEach(path -> {path.toFile().delete();});
		
		Logger.getGlobal().info("clear done.");
	}

	static void build() throws Exception {
		String Libs = System.getProperty("tiki.libs");
		String src = System.getProperty("tiki.src");
		if (src == null || src.isEmpty() || !new File(src).exists()) {
			throw new RuntimeException("missing 'tiki.src' or not exists");
		}
		Logger.getGlobal().info(String.format("tiki.src: %s", src));
		Logger.getGlobal().info(String.format("tiki.libs: %s", Libs == null ? "empty" : Libs));

		File sourceDir = new File(src);
		Files.walk(sourceDir.toPath()).filter(path -> !Files.isDirectory(path))
				.filter(path -> path.toString().endsWith(".t")).forEach(path -> compile(sourceDir, path.toFile()));
	}

	static void run() throws Exception {
		long start = System.currentTimeMillis();

		PluginManager PM = new PluginManager();
		PM.installPlugin("tiki.Object", new TObject());
		PM.installPlugin("tiki.String", new TString());
		PM.installPlugin("tiki.util.System", new TSystem());
		PM.installPlugin("tiki.util.Math", new TMath());
		PM.installPlugin("tiki.util.Date", new TDate());

		VirtualMachine VM = new VirtualMachine(PM);

		String src = System.getProperty("tiki.src");
		File sourceDir = new File(src);
		Files.walk(sourceDir.toPath()).filter(path -> !Files.isDirectory(path))
				.filter(path -> path.toString().endsWith(".xml")).forEach(path -> VM.loadPackage(path.toFile()));

		VM.start();
		Logger.getGlobal().info("invoke               \t\t\t" + (System.currentTimeMillis() - start));
	}

	static void buildEnv() throws Exception {
		Tiki tiki = new Tiki();
		tiki.buildEnv();
		
		Logger.getGlobal().info("buildEnv done.");
	}

	@Override
	public void start(Stage primaryStage) throws FileNotFoundException {
		System.setProperty("tiki.env", "env");
		System.setProperty("tiki.log", "Test/projects/log");
		System.setProperty("tiki.src", "Test/projects/project1/src");
		System.setProperty("tiki.libs", "Test/projects/project1/lib");

		primaryStage.setTitle("Tiki");
		BorderPane root = new BorderPane();
		info = new TextArea();
		root.setCenter(info);

		ArrayList<Control> buttons = new ArrayList<>();

		Button btn;
		btn = new Button();
		btn.setText("clear");
		btn.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				try {
					int[][] dimensional_2 = new int[2][3];
					for (int i = 0; i < dimensional_2.length; i++) {
						{
							for (int j = 0; j < dimensional_2[i].length; j++) {
								System.out.print(
										String.format("dimensional_2[%d][%d] = %d\n", i, j, dimensional_2[i][j]));
							}
						}
					}
					clear();
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		});
		buttons.add(btn);

		btn = new Button();
		btn.setText("build");
		btn.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				try {
					build();
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		});
		buttons.add(btn);

		btn = new Button();
		btn.setText("run");
		btn.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				try {
					run();
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		});
		buttons.add(btn);

		btn = new Button();
		btn.setText("buildEnv");
		btn.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				try {
					buildEnv();
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		});
		buttons.add(btn);

		btn = new Button();
		btn.setText("clearParser");
		btn.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				try {
					clearParser();
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		});
		buttons.add(btn);
		
		btn = new Button();
		btn.setText("rebuildParser");
		btn.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				try {
					rebuildParser();
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		});
		buttons.add(btn);

		btn = new Button();
		btn.setText("Test01");
		btn.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				try {
					//testAST();

					InputStream re_grammar = Tiki.class.getResourceAsStream("/re_grammar.txt");
					String content = IOUtils.toString(re_grammar);
					// System.out.println(content);

					Pattern p = Pattern.compile("((?<prod>(\\s*\\w+\\s+)+)(?<script>\\{.*\\})?(\\s*\\|)?)+");
					Matcher m = p.matcher(content);
					while (m.find()) {
						for(int i=0;i<m.groupCount();i++)
						{
							System.out.println(String.format("%d: %s", i,m.group(i)));
						}
					}

				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		});
		buttons.add(btn);

		buttons.forEach(b -> b.setPrefSize(250, 60));

		VBox box = new VBox();
		box.setPadding(new Insets(15, 12, 15, 12));
		box.setSpacing(20);
		box.getChildren().addAll(buttons);
		root.setRight(box);

		primaryStage.setScene(new Scene(root, 900, 700));
		primaryStage.show();
	}
}