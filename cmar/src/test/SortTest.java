package test;

import static org.junit.Assert.*;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import mine.CMAR_App;
import weka.core.FastVector;

public class SortTest {
	private CMAR_App cmarapp = new CMAR_App();
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Test
	public void testSort() {
		FastVector[] rules = new FastVector[3];
		rules[0] = new FastVector();
		rules[1] = new FastVector();
		rules[2] = new FastVector();
		rules[2].addElement(0.0);
		rules[2].addElement(2.0);
		rules[2].addElement(2.0);
		rules[2].addElement(1.0);
		
		rules[1].addElement(0.0);
		rules[1].addElement(2.0);
		rules[1].addElement(2.0);
		rules[1].addElement(1.0);
		
		rules[0].addElement(0.0);
		rules[0].addElement(2.0);
		rules[0].addElement(2.0);
		rules[0].addElement(1.0);
		cmarapp.sortRulesBySupport(rules, 0, 3);
	    StringBuilder strBuilder = new StringBuilder();
	    strBuilder.append(rules[2].elementAt(0)+",");
	    strBuilder.append(rules[2].elementAt(1)+",");
	    strBuilder.append(rules[2].elementAt(2)+",");
	    strBuilder.append(rules[2].elementAt(3));
	    assertEquals(strBuilder.toString(),"2.0,2.0,1.0,0.0");
	}

}
