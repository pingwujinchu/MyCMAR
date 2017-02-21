package test;

import static org.junit.Assert.*;

import java.io.File;
import java.io.FileReader;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import associations.ItemSet;
import mine.Rule;
import prun.DBCoverUtil;
import weka.associations.LabeledItemSet;
import weka.core.FastVector;
import weka.core.Instances;
import static org.junit.Assert.*;

public class DBCoverTest {
	private static FastVector rules;
	
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		Instances ins = new Instances(new FileReader(new File(DBCoverTest.class.getResource("dbcover.data").toURI())));
		ins.setClassIndex(ins.numAttributes()-1);
		DBCoverUtil.data = LabeledItemSet.divide(ins,false);
		DBCoverUtil.onlyClass = LabeledItemSet.divide(ins,true);
		int numClass = DBCoverUtil.onlyClass.attribute(0).numValues();
		DBCoverUtil.numPerClass = new int[numClass];
		for(int i = 0 ; i < numClass ; i++){
			DBCoverUtil.numPerClass[i] = 1;
		}
		DBCoverUtil.numsCover = new int[ins.numInstances()];
		
		int []arr = new int[]{-1,0,-1,-1};
		int []arr1 = new int[]{-1,1,-1,-1};
		int []arr2 = new int[]{-1,2,1,-1};
		int []arr3 = new int[]{0,0,-1,-1};
		
		ItemSet is1 = new ItemSet(arr);
		ItemSet is2 = new ItemSet(arr1);
		ItemSet is3 = new ItemSet(arr2);
		ItemSet is4 = new ItemSet(arr3);
		
		Rule r1 = new Rule(is1,0,0.88,0.88);
		Rule r2 = new Rule(is2,1,0.88,0.88);
		Rule r3 = new Rule(is3,2,0.88,0.88);
		Rule r4 = new Rule(is4,0,0.88,0.88);
		
		rules = new FastVector();
		rules.addElement(r1);
		rules.addElement(r2);
		rules.addElement(r3);
		rules.addElement(r4);
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
		
	}

	@Test
	public void testDBCover() {
		boolean result [] = DBCoverUtil.prunByDBCover(rules);
		assertArrayEquals(result,new boolean[]{true,true,true,false});
	}

}
