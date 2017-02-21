package test;

import static org.junit.Assert.*;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.net.URISyntaxException;

import javax.xml.transform.SourceLocator;

import org.junit.BeforeClass;
import org.junit.Test;

import associations.ItemSet;
import associations.ListHead;
import mine.CMAR_App;
import mine.CRTree;
import mine.CRTreeNode;
import mine.Rule;
import weka.core.FastVector;
import weka.core.Instance;
import weka.core.Instances;

public class CRTreeTest {

	private static FastVector head;
	private static FastVector rules;
	private static CRTree crtree;
	private static Instances ins;
	private static Instance instance ;
	
	@BeforeClass
	public static void beforClass(){
		head = new FastVector();
		
		ListHead lh  = new ListHead(59,(byte)0,(byte)0);
		ListHead lh1 = new ListHead(57,(byte)1,(byte)1);
		ListHead lh2 = new ListHead(57,(byte)1,(byte)0);
		ListHead lh3 = new ListHead(55,(byte)0,(byte)2);
		ListHead lh4 = new ListHead(55,(byte)2,(byte)2);
		ListHead lh5 = new ListHead(54,(byte)3,(byte)1);
		ListHead lh6 = new ListHead(50,(byte)3,(byte)0);
		
		head.addElement(lh);
		head.addElement(lh1);
		head.addElement(lh2);
		head.addElement(lh3);
		head.addElement(lh4);
		head.addElement(lh5);
		head.addElement(lh6);
		
		try {
			ins = new Instances(new FileReader(new File(CRTreeTest.class.getResource("test.data").toURI())));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		crtree = new CRTree(3, head, ins,ins);
		
		int [] arr = new int[]{-1,-1,-1,0};
		int [] arr1 = new int[]{0,1,-1,-1};
		int [] arr2 = new int[]{0,-1,-1,-1};
		int [] arr3 = new int[]{-1,0,-1,-1};
		int [] arr4 = new int[]{1,-1,-1,-1};
		int [] arr5 = new int[]{-1,-1,2,-1};
		int [] arr6 = new int[]{0,1,-1,0};
		
		ItemSet iset = new ItemSet(arr);
		ItemSet iset1 = new ItemSet(arr1);
		ItemSet iset2 = new ItemSet(arr2);
		ItemSet iset3 = new ItemSet(arr3);
		ItemSet iset4 = new ItemSet(arr4);
		ItemSet iset5 = new ItemSet(arr5);
		ItemSet iset6 = new ItemSet(arr6);
		
		Rule rule1 = new Rule(iset,0,0.99,0.999);
		Rule rule2 = new Rule(iset1,0,0.947,0.947);
		Rule rule3 = new Rule(iset2,0,0.83,0.83);
		Rule rule4 = new Rule(iset3,1,0.59,0.59);
		Rule rule5 = new Rule(iset4,1,0.638,0.638);
		Rule rule6 = new Rule(iset5,2,0.89,0.89);
		Rule rule7 = new Rule(iset6,0,0.99,0.99);
		
		rules = new FastVector();
		rules.addElement(rule2);
		rules.addElement(rule5);
		rules.addElement(rule4);
		rules.addElement(rule6);
		rules.addElement(rule1);
		rules.addElement(rule3);
		rules.addElement(rule7);
		
		CRTreeNode crtnode1 = new CRTreeNode((byte)0, (byte)0);
		CRTreeNode crtnode2 = new CRTreeNode((byte)1, (byte)1);
		CRTreeNode crtnode3 = new CRTreeNode((byte)1, (byte)0);
		CRTreeNode crtnode4 = new CRTreeNode((byte)2, (byte)2);
		CRTreeNode crtnode5 = new CRTreeNode((byte)3, (byte)0);
		CRTreeNode crtnode6 = new CRTreeNode((byte)3, (byte)0);
		
		crtnode1.classLabel = 0;
		crtnode1.child.add(crtnode2);
		crtnode1.father = crtree.getRoot();
		crtnode1.confidence = 0.83;
		crtnode1.support = 0.83;
		
		crtnode2.classLabel = 0;
		crtnode2.confidence = 0.947;
		crtnode2.support = 0.947;
		crtnode2.father = crtnode1;
		
		crtnode3.father = crtree.getRoot();
		crtnode3.classLabel = 1;
		crtnode3.confidence = 0.59;
		crtnode3.support = 0.59;
		
		crtnode4.classLabel = 2;
		crtnode4.confidence = 0.89;
		crtnode4.support = 0.89;
		crtnode4.father = crtree.getRoot();
		
		crtnode5.classLabel = 0;
		crtnode5.confidence = 0.99;
		crtnode5.support = 0.99;
		crtnode5.father = crtree.getRoot();
		
		crtnode6.classLabel = 0;
		crtnode6.confidence = 0.99;
		crtnode6.support = 0.99;
		crtnode6.father = crtnode2;
		crtnode2.addChild(crtnode6);
		
		crtree.getRoot().child.add(crtnode1);
		crtree.getRoot().child.add(crtnode3);
		crtree.getRoot().child.add(crtnode4);
		crtree.getRoot().child.add(crtnode5);
		
		lh.addNext(crtnode1);
		lh1.addNext(crtnode2);
		lh2.addNext(crtnode3);
		lh4.addNext(crtnode4);
		lh6.addNext(crtnode5);
		lh6.addNext(crtnode6);
		instance = new Instance(5);
	}
	
	/**
	 * 测试insert方法是否运行正常
	 */
	@Test
	public void testInsertTree() {
		FastVector newHead = new FastVector();
		for(int i = 0 ; i < head.size() ; i++){
			ListHead lh = (ListHead) head.elementAt(i);
		    newHead.addElement(new ListHead(lh.count,lh.attr,lh.value));	
		}
		CRTree tree = new CRTree(3,newHead,ins,ins);
		tree.insertRules(rules);
		assertThat(tree,new CRTreeMatcher(crtree));
	}
	
	/**
	 * 对满足某条实例的规则进行测试
	 */
	@Test
	public void testGenAllRules(){
		Instance testInstance = ins.instance(0);
		FastVector [] result = crtree.genCarRules(testInstance);
		FastVector [] expect = new FastVector[3];
		for(int i = 0 ; i < 3 ; i++){
		    expect[i] = new FastVector();	
		}
		ItemSet it = new ItemSet(new int []{0,-1,-1,-1});
		ItemSet it1 = new ItemSet(new int []{0,1,-1,-1});
		ItemSet it2 = new ItemSet(new int []{0,1,-1,0});
		ItemSet it3 = new ItemSet(new int []{-1,-1,-1,0});
		Rule rule1 = new Rule(it,0,0.83,0.83);
		Rule rule2 = new Rule(it1,0,0.947,0.947);
		Rule rule3 = new Rule(it2,0,0.99,0.99);
		Rule rule4 = new Rule(it3,0,0.99,0.99);
		expect[0].addElement(rule1);
		expect[0].addElement(rule2);
		expect[0].addElement(rule3);
		expect[0].addElement(rule4);
		assertThat(result,new FastVectorArrayMatcher(expect));
	}
	
	@Test
	public void testDBCover(){
		
	}
	
	@Test
	public void testFindMax(){
		double [] vote= new double[]{0.0,0.8,0.2,0.6};
		CMAR_App ca = new CMAR_App();
		assertEquals(1,ca.findMax(vote));
	}

}
