package test;

import static org.junit.Assert.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.URISyntaxException;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import associations.FP;
import associations.TNode;
import weka.core.Instances;

public class X2Test {

	static FP fp;
	static Instances ins;
	static TNode curr;
	static double[]supB;
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		fp = new FP();
		ins = new Instances(new FileReader(new File(X2Test.class.getResource("x2test.data").toURI())));
		for(int i = 0 ; i < 499 ; i++){
			ins.add(ins.instance(0));
		}
		fp.m_onlyClass = ins;
		curr = new TNode((byte)0,(byte)0);
		curr.sup = new int[2];
		curr.sup[0] = 199;
		curr.sup[1] = 1;
		curr.m_counter = 200;
		supB = new double[2];
		supB[0] = (double)450/500;
		supB[1] = (double)50 / 500;
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
		
	}

	@Test
	public void testX2() throws FileNotFoundException, IOException, URISyntaxException {
		double result = fp.calculateX2(curr, supB);
		System.out.println(result);
		assertEquals(result,88.4);
	}

}
