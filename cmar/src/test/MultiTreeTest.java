package test;

import static org.junit.Assert.*;

import java.io.File;
import java.io.FileReader;
import java.io.Reader;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import associations.ListHead;
import multitree.MultiAcwv_App;
import weka.core.FastVector;
import weka.core.Instances;

public class MultiTreeTest {
	
	FastVector [] head;
	static Instances ins;
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		 ins = new Instances(new FileReader(new File(MultiTreeTest.class.getResource("weather.arff").toURI())));
		 ins.setClassIndex(ins.numAttributes() - 1);
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
		
	}

	@Test
	public void testMultiTreeBuild() {
		MultiAcwv_App m = new MultiAcwv_App();
		try {
			m.buildClassifier(ins);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		FastVector [] fv = m.heads;
		assertEquals(fv.length,2);
		ListHead lh = (ListHead) fv[0].elementAt(0);
		assertEquals(1,lh.sup.length);
		int classLabel = (int) m.classifyInstance(ins.instance(2));
		assertEquals(0,classLabel);
		assertNotEquals(fv[0],fv[1]);
	}

}
