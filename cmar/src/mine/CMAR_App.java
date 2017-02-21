package mine;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.Calendar;
import java.util.LinkedList;

import associations.FP;
import associations.ItemSet;
import associations.ListHead;
import jzh.ACWV;
import prun.DBCoverUtil;
import prun.X2TestUtil;
import util.LogProcess;
import weka.associations.LabeledItemSet;
import weka.classifiers.Classifier;
import weka.core.FastVector;
import weka.core.Instance;
import weka.core.Instances;

public class CMAR_App extends Classifier{
	   double[] classValue;
	   int[] classCount;
	   Instances myData;
	   FastVector m_hashtables = new FastVector();
	   public Instances m_onlyClass;
	   int clIndex=0;
	   int attNum=0;
	   FP f;
	   int count = 0;
	   static int c = 0;
	   FastVector head;
	   
	   double minSup = 0.01;	
	   double minCon = 1.1;
	   int numClass;
	   
	   static long timecost = 0;
	   LinkedList m_allTheRules=new LinkedList();
	   
	   CRTree crtree;
	@Override
	public void buildClassifier (Instances data)throws Exception
	  { 
		 double upperBoundMinSupport=1;
	     myData = LabeledItemSet.divide(data,false);
	     attNum=myData.numAttributes();
	     //m_onlyClass contains only the class attribute
	     m_onlyClass = LabeledItemSet.divide(data,true);
		 clIndex=data.classIndex();//index of the class
//		 int numClass=m_onlyClass.numDistinctValues(0);//number of classValue
		 numClass = m_onlyClass.attribute(0).numValues();
		 
		 double[] supB = new double[numClass];
//	     classCount=new int[numClass];
//	     double[] clValue=m_onlyClass.attributeToDoubleArray(0);
//		 classValue=differentiate(clValue);//find all the different class value
//		 count(clValue);
		 f = new FP();
		 long t1 = System.currentTimeMillis();
		 head = f.buildClassifyNorules(myData, m_onlyClass, minSup, 1, 1, minCon);  //生成的headtable
		 long t2 = System.currentTimeMillis();
		 timecost += (t2 - t1);
		 //System.out.println("the time cost of building classfier is :" + timecost);
		 classValue = getSupB();
		 count = 0;
		 c++;
//		 m_allTheRules=f.newCMAR(myData,m_onlyClass,0.02,1,1);
		 //检查该函数，看该函数是否为生成所有的关联分类规则，并将规则的支持度、置信度等信息返回。
//		 FastVector []allTheRules = f.findCarLargeItemSetFPTree(myData,m_onlyClass,minSup,upperBoundMinSupport,0.9);
//		 System.out.println(allTheRules[1].size());
//		 System.out.println(allTheRules[0].elementAt(0));

		 
		 FastVector newHead = new FastVector();     //该变量为生成crtree的head
		 for(int i = 0 ; i < head.size() ; i++){    //重构head
			 ListHead old = (ListHead) head.elementAt(i);
			 ListHead lh = new ListHead(old.count,old.attr,old.value);
			 newHead.addElement(lh);
		 }
		 
		 FastVector [] allTheRules = f.newCMAR(myData, m_onlyClass, minSup, 1, minCon);
		 sortRules(allTheRules);
		 crtree = new CRTree(numClass,newHead,myData,m_onlyClass);
		 
		 buildCRTree(allTheRules,crtree);
	  }
	
	public void sortRules(FastVector[]rules){
		sortRulesBySupport(rules,0,rules[2].size()-1);
		sortRulesByConfidence(rules,0,rules[3].size() - 1);
	}
	
	public void sortRulesByConfidence(FastVector[]rules,int start,int end){

//	    int i = start;
//		int j = end;
//		
//		double pivote = (double)rules[2].elementAt(i);
//		while(i < j){
//			while((double)rules[2].elementAt(j) < pivote){
//				j--;
//			}
//			while(i < j &&(double)rules[2].elementAt(i) >= pivote){
//				i ++;
//			}
//			if(j < i){
//				j ++;
//			}
//			swap(rules,i,j);
//		}
//		swap(rules,start,j);
//		if(i > start){
//			sortRulesByConfidence(rules,start,i - 1);
//		}
//		if(j < end){
//			sortRulesByConfidence(rules,j + 1,end);
//		}
		
		for(int i = start ; i <= end ; i++){
			for(int j = i + 1 ; j <= end ; j++){
				if((double)rules[3].elementAt(i) < (double)rules[3].elementAt(j)){
					swap(rules,i,j);
				}
			}
		}
	}
	
	public void sortRulesBySupport(FastVector[]rules,int start,int end){

//		int i = start;
//		int j = end;
//		double pivote = (double)rules[2].elementAt(i);
//		while(i < j){
//			while((double)rules[2].elementAt(j) < pivote){
//				j--;
//			}
//			while(i < j &&(double)rules[2].elementAt(i) >= pivote){
//				i ++;
//			}
//			if(j < i){
//				j ++;
//			}
//			swap(rules,i,j);
//		}
//		swap(rules,start,j);
//		if(i > start){
//			sortRulesBySupport(rules,start,i - 1);
//		}
//		if(j < end){
//			sortRulesBySupport(rules,j + 1,end);
//		}
		
		for(int i = start ; i <= end ; i++){
			for(int j = i + 1 ; j <= end ; j++){
				if((double)rules[2].elementAt(i) < (double)rules[2].elementAt(j)){
					swap(rules,i,j);
				}
			}
		}
	}
	
	public void swap(FastVector[] fv,int i,int j){
		for(int k = 0 ; k < fv.length ; k++){
			Object item = fv[k].elementAt(i);
			fv[k].setElementAt(fv[k].elementAt(j), i);
			fv[k].setElementAt(item, j);
		}
	}
	
	//not test
	/**
	 * 该方法使用生成的规则集合构建crtree，即将每条规则插入到crtree中。
	 * @param rules
	 * @param crtree
	 */
	public void buildCRTree(FastVector [] rules,CRTree crtree){
		for(int i = 0 ; i < rules[0].size() ; i++){
			ItemSet curr = (ItemSet) rules[0].elementAt(i);
			ItemSet cl = (ItemSet) rules[1].elementAt(i);
			double support = (Double) rules[2].elementAt(i);
			double conf = (Double) rules[3].elementAt(i);
			double x2 = (Double) rules[5].elementAt(i);
			double leftSup = (Integer) rules[6].elementAt(i);
						
			Rule rule = new Rule(curr,cl.itemAt(0),support,conf,leftSup,x2);
			if(DBCoverUtil.prunByDBCover(rule)&&X2TestUtil.prunByX2Test(rule)){
				crtree.insertRules(rule);
			}
		}
	}
	  
//	@Override
//	  public double classifyInstance(Instance instance)
//	   {
//	    int l=classValue.length;
//		double dPro[]=new double[l];
////		dPro = newcalculatePro(l,instance);
//	   if (c > 1){
//		long t1 = System.currentTimeMillis();
//
//		dPro = f.calculatePro(instance, head, classValue);
//		long t2 = System.currentTimeMillis();
//		timecost += (t2 - t1);
////		System.out.println(timecost);
//	   }
//	   count++;
//		int iMax=0; 
//		return iMax;
//	   }
	
	@Override 
	public double classifyInstance(Instance instance){
		double []vote = new double[numClass];
		for(int i = 0 ; i < vote.length ; i++){
			vote[i] = 0;
		}
		FastVector [] rules = crtree.genCarRules(instance);
		System.out.println("rules:"+rules[0].size());
		for(int i = 0 ; i < rules.length ; i++){
			FastVector fv = rules[i];
			double weight = 0;
			for(int j = 0 ; j < fv.size() ; j++){
				weight += ((Rule)fv.elementAt(j)).confidence;     //使用置信度进行计算投票结果。
				//*******
				//使用maxX2进行计算投票结果
				//*******
				int T = m_onlyClass.numInstances();
				double e = 1/(((Rule)fv.elementAt(j)).supLeft * (classValue[i]*T)) + 1/(((Rule)fv.elementAt(j)).supLeft * (T - classValue[i]*T)) + 1/((T - ((Rule)fv.elementAt(j)).supLeft) * (classValue[i]*T))  + 1/((T - ((Rule)fv.elementAt(j)).supLeft) * (T - classValue[i]*T));
				double maxX2 = Math.pow((Math.min(((Rule)fv.elementAt(j)).supLeft, (classValue[i]*T))-(((Rule)fv.elementAt(j)).supLeft * (classValue[i]*T)) / T),2)*T*e;
				weight += Math.pow(((Rule)fv.elementAt(j)).x2,2)/maxX2;
			}
//			if(fv.size() > 0){
//				weight /= fv.size();
//			}
			vote[i] = weight;
//			System.out.println(vote[i]);
		}
		
		int result  = findMax(vote);
		return result;
	}
	
	/**
	 * 
	 * 找出投票完之后的数组中最大的，然后将下标返回。
	 * @param vote
	 * @return
	 * 
	 */
	public int findMax(double [] vote){
	   	double max = vote[0];
	   	int index = 0;
	   	for(int i = 0 ; i < vote.length ; i ++){
	   		if(vote[i] > max){
	   			max = vote[i];
	   			index = i;
	   		}
	   	}
	   	return index;
	}
	
	public double[] getSupB(){
		  int len = (m_onlyClass.attribute(0)).numValues();
		  double[] supB = new double[len];
		  int[] s = new int[len];
		  
		  for (int i = 0; i < len; i++){
			  s[i] = 0;
		  }
		  
		  for (int i = 0; i < m_onlyClass.numInstances(); i++){
			  Instance instance = m_onlyClass.instance(i);
			  int classlabel=(int)(m_onlyClass.instance(i).value(0));
			  s[classlabel] ++;
		  }
		  for (int i = 0; i < len; i++){
			  supB[i] = (double)s[i] / (double)(m_onlyClass.numInstances());
		  }
		  return supB;
	  }
	  
	  public static void main(String []args){
		  String balance = "data";
		  String imblance = "keel";
//		  runAllDataSet(balance);
		  String[] arg1={"-t","keel/yeast5.arff","-i"};
		  String[] arg2={"-t","keel/glass-0-1-2-3_vs_4-5-6.arff","-i"};
		  String[] arg3={"-t","keel/glass1.arff","-i"};
		  String[] arg4={"-t","keel/glass6.arff","-i"};
		  String[] arg5={"-t","keel/new-thyroid1.arff","-i"};
		  String[] arg6={"-t","keel/newthyroid2.arff","-i"};
		  String[] arg7={"-t","keel/pima.arff","-i"};
		  String[] arg8={"-t","keel/pimaImb.arff","-i"};
//		  System.out.println("dataset:iris");
//		  runClassifier(new ACWV(), arg1);
		  runClassifier(new CMAR_App(), arg7);
		  
//**************************************************************
//		  runAllDataSet(imblance);
//		  ACWV.runAllDataSet(imblance);
	  }
	  
	  public static void runAllDataSet(String folderName){
		  Calendar cal = Calendar.getInstance();
		  String fileName = "log/"+cal.getTimeInMillis()+".log";
		  try {
			PrintStream ps = new PrintStream(new File(fileName));
			System.setOut(ps);
		   } catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		   } 
		  File folder = new File(folderName);
		  File[] allFile = folder.listFiles();
		  for(int i = 1 ; i < allFile.length ; i++){
			  System.out.println("dataset:"+allFile[i].getName());
			  String [] arg = new String[]{"-t",allFile[i].getPath(),"-i"};
			  runClassifier(new CMAR_App(),arg);
		  }
		  LogProcess.logProcess(fileName);
	  }
}
