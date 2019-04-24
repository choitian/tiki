/**
 * 
 */
package tiki;

import static org.junit.Assert.*;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Paths;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.w3c.dom.Document;

import tiki.tools.complier.lalr.LALR;
import tiki.tools.complier.lex.DFA;
import tiki.uitls.IOUtils;

/**
 * @author nur
 *
 */
public class TikiTest01 {

	/**
	 * @throws java.lang.Exception
	 */
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	/**
	 * @throws java.lang.Exception
	 */
	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
		System.setProperty("tiki.env", "env");
		System.setProperty("tiki.log", "Test/projects/log");
		System.setProperty("tiki.src", "Test/projects/project1/src");
		System.setProperty("tiki.libs", "Test/projects/project1/lib");
	}

	/**
	 * @throws java.lang.Exception
	 */
	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void test() throws Exception {
		InputStream re = Tiki.class.getResourceAsStream("/re.txt");
		InputStream dnf = Tiki.class.getResourceAsStream("/dnf.txt");

		DFA dfa = new DFA();
		dfa.Construct(re);

		Document dfaXml = dfa.toXML();
		assertTrue(dfaXml!=null);
		IOUtils.toFile(IOUtils.toString(dfaXml), Paths.get(System.getProperty("tiki.env"), "dfa.xml").toFile());

		LALR lalr = new LALR();
		lalr.build(dnf);
		IOUtils.toFile(IOUtils.toString(lalr.toXML()), Paths.get(System.getProperty("tiki.env"), "lalr.xml").toFile());
	}
}
