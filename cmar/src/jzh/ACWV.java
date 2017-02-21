
package jzh;
import weka.classifiers.*;
import weka.core.*;

import java.io.*;
import java.util.*;

import associations.FP;
import associations.ListHead;
import associations.RuleItems;
import associations.TNode;
import util.LogProcess;
import weka.associations.*;
public class ACWV extends Classifier
{
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
   
   
   
//// Column 01
//   double minSup = 0.01;	
//   double minCon = 1.05;
//
////   Column 02
   double minSup = 0.01;	
   double minCon = 1.1;
//
////   Column 03
//   double minSup = 0.01;	
//   double minCon = 1.2;
//
////   Column 04
//   double minSup = 0.01;	
//   double minCon = 1.5;
////
//   Column 05
//   double minSup = 0.02;	
//   double minCon = 1.05;
//
////   Column 06
//   double minSup = 0.02;	
//   double minCon = 1.1;
//
////   Column 07
//   double minSup = 0.02;	
//   double minCon = 1.2;
//
////   Column 08
   //double minSup = 0.02;	
   //double minCon = 1.5;
//
////   Column 09
//   double minSup = 0.05;	
//   double minCon = 1.05;
//
////   Column 10
//   double minSup = 0.05;	
//   double minCon = 1.1;
//
////   Column 11
//   double minSup = 0.05;	
//   double minCon = 1.2;
//
////   Column 12
//   double minSup = 0.05;	
//   double minCon = 1.5;
//
////   Column 13
//   double minSup = 0.1;	
//   double minCon = 1.05;
//
////   Column 14
//   double minSup = 0.1;	
//   double minCon = 1.1;
//
////   Column 15
//   double minSup = 0.1;	
//   double minCon = 1.2;
//
////   Column 16
//   double minSup = 0.1;	
//   double minCon = 1.5;

   
   
   static long timecost = 0;
   LinkedList m_allTheRules=new LinkedList();
  public void buildClassifier (Instances data)throws Exception
  { 
	 
	 double upperBoundMinSupport=1;
     myData = LabeledItemSet.divide(data,false);
     attNum=myData.numAttributes();
     //m_onlyClass contains only the class attribute
     m_onlyClass = LabeledItemSet.divide(data,true);
	 clIndex=data.classIndex();//index of the class
//	 int numClass=m_onlyClass.numDistinctValues(0);//number of classValue
	 int numClass = m_onlyClass.attribute(0).numValues();
	 
	 double[] supB = new double[numClass];
//     classCount=new int[numClass];
//     double[] clValue=m_onlyClass.attributeToDoubleArray(0);
//	 classValue=differentiate(clValue);//find all the different class value
//	 count(clValue);
	 f = new FP();
	 long t1 = System.currentTimeMillis();
	 head = f.buildClassifyNorules(myData, m_onlyClass, minSup, 1, 1, minCon);

	 long t2 = System.currentTimeMillis();
	 timecost += (t2 - t1);
	 //System.out.println("the time cost of building classfier is :" + timecost);
	 classValue = getSupB();
	 count = 0;
	 c++;
//	 m_allTheRules=f.newCMAR(myData,m_onlyClass,0.02,1,1);
	 FastVector []allTheRules = f.findCarLargeItemSetFPTree(myData,m_onlyClass,minSup,upperBoundMinSupport,0.9);
//	 System.out.println(allTheRules[3].elementAt(0).getClass());
//	 System.out.println(allTheRules[0].elementAt(0));
//	 findCarRulesQuickly(m_Ls);
	 //m_Ls.removeAllElements();
//	 printTree(head);
//	 print(allTheRules);
  }
  
  public void printTree(FastVector head){
	  for(int i = 0 ; i < head.size() ; i++){
		  System.out.print("head:("+((ListHead)head.elementAt(i)).attr+","+((ListHead)head.elementAt(i)).value+","+((ListHead)head.elementAt(i)).count+")");
		  System.out.print("	");
		  ListHead lh = ((ListHead)head.elementAt(i));
		  FastVector next = lh.next;
		  for(int j = 0 ; j < next.size() ; j++){
			  TNode curr = ((TNode)next.elementAt(j));
			  System.out.print("("+curr.hashCode()+","+curr.m_counter+","+curr.father.hashCode()+")");
		  }
		  System.out.println();
	  }
  }
  
  /**
 * @param head
 * ²âÊÔheadtableÊÇ·ñÕý³£
 */
   public void print(FastVector [] allTheRules){
	      System.out.println("rules start");
      for(int i = 0 ; i < allTheRules[0].size() ; i++){
    	  System.out.print("Rule:"+Arrays.toString(((associations.ItemSet)allTheRules[0].elementAt(i)).items())+"		");
    	  System.out.print(Arrays.toString(((associations.ItemSet)allTheRules[1].elementAt(i)).items())+"		");
    	  System.out.print(allTheRules[2].elementAt(i).toString()+"		");
    	  System.out.println();
     }
  }
  
  public double classifyInstance(Instance instance)
   {
    int l=classValue.length;
	double dPro[]=new double[l];
//	dPro = newcalculatePro(l,instance);
   if (c > 1){
	long t1 = System.currentTimeMillis();

	dPro = f.calculatePro(instance, head, classValue);
	long t2 = System.currentTimeMillis();
	timecost += (t2 - t1);
//	System.out.println(timecost);
   }
   count++;
	int iMax=findMax(dPro); 
	return iMax;
   }
  
  private void count(double[] clValue)
  {
	int temp=0;
	for(int i=0;i<clValue.length;i++)
	{
	  temp=(int)clValue[i];
	  classCount[temp]++;
	}
  }
  
  private boolean contains(Instance ins,byte[] rulePre)
  {  
	
	for(int j = 0; j < rulePre.length-1; j++)
	{
	    if (rulePre[j] != -1){
	    	if (rulePre[j] != ins.value(j)){
	    		return false;
	    	}
	    }
	}
	return true;
  }
  
  /**
  *
  * Method that finds all large itemsets for class association rules for the given set of instances.
  * @throws Exception if an attribute is numeric
  */

  private double[] calculatePro(int l,Instance ins)
  {
	double dPro[]=new double[l];
	
	ListIterator<RuleItems> ruleiter = m_allTheRules.listIterator();
	while(ruleiter.hasNext()){
		RuleItems rule = ruleiter.next();
		if (contains(ins,rule.m_items)){
			int ruleLength = length(rule.m_items);
			int len = rule.m_items.length - 1;
			double d =  ruleLength;
			if(d==0)
			{
				d=0.01;
			}
			dPro[rule.m_items[len]] += rule.conv / d;
		}
	}
    return dPro;
  }
  
  private double[] newcalculatePro(int l,Instance ins)
  {
	double dPro[]=new double[l];
	try{
		RandomAccessFile file = new RandomAccessFile("result.dat","rw");
		long cur = 0;
		
	while (cur < file.length()){
			file.seek(cur);
		byte[] item = new byte[attNum+1];
		file.read(item);
		double conv = file.readDouble();
		if (contains(ins,item)){
			double d =  length(item);
			if(d==0)
			{
				d=0.01;
			}
			dPro[item[attNum]] += conv / d;
		}
		cur += (attNum+1);
		cur += 8;
		}
	file.close();
	}catch(IOException e){
		e.printStackTrace();
	}
	
    return dPro;
  } 
  
  private int length(byte[] itemSet)
  {
	int l=0;
	for(int i=0;i < itemSet.length-1 ;i++)
	{
	  if(itemSet[i] == -1)
	  {
		l++;
	  }
	}
	return l;
  }
  
  private double[] differentiate(double[] value)
  {
	double max=0;
	for(int i=0;i<value.length;i++)
	{
	  if(value[i]>max)
	  {
		max=value[i];
	  }
	}
	double distinctValue[]=new double[(int)max+1];
	for(int j=0;j<=max;j++)
	{
	  distinctValue[j]=j;
	}
	return distinctValue;
  }
	
  private int findMax(double[] d)
  {
	int l=d.length;
	int iMax=0;
	double temp=d[0];
	for(int i=1;i<l;i++)
	{
	  if(d[i]>temp)
	  {
		iMax=i;
		temp=d[i];
	  }
	}
	return iMax;
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

  public static void main(String[] argv){
	  Calendar cal = Calendar.getInstance();
	  String fileName = "log/"+cal.getTimeInMillis()+".log";
	  try {
		PrintStream ps = new PrintStream(new File(fileName));
		System.setOut(ps);
	} catch (FileNotFoundException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	} 
//	  String[] arg1 ={"-t","vehicleout.arff"};
//	  String[] arg2 ={"-t","balloons.arff"};
//	  String[] arg3 ={"-t","car.arff"};
//	  String[] arg4 ={"-t","lenses.arff"};
//	  String[] arg5 ={"-t","tic-tac-toe.arff"};
//	  String[] arg6 ={"-t","ionoout2.arff"};
//	  String[] arg7 ={"-t","pimaout.arff"};
//	  String[] arg8 ={"-t","taeout.arff"};
//	  String[] arg9 ={"-t","habermanout.arff"};
//	  String[] arg10={"-t","glassout.arff"};
//	  String[] arg11={"-t","breastout.arff"};
//	  String[] arg12={"-t","cmcout.arff"};
//	  String[] arg13={"-t","ecoliout.arff"};
//	  String[] arg14={"-t","liverout.arff"};
//	  String[] arg15={"-t","postout.arff"};
//	  String[] arg16={"-t","hypoout2.arff"};
//	  String[] arg17={"-t","yeastout.arff"};
//	  String[] arg18={"-t","autoout.arff"};
//	  String[] arg19={"-t","cleveout.arff"};
//	  String[] arg20={"-t","diabetesout.arff"};
//	  String[] arg21={"-t","heartout.arff"};
//	  String[] arg22={"-t","irisout.arff"};
//	  String[] arg23={"-t","laborout.arff"};
//	  String[] arg24={"-t","led7.arff"};
//	  String[] arg25={"-t","wineout.arff"};
//	  String[] arg26={"-t","zoo.arff"};
//	  String[] arg27={"-t","crxout.arff"};
//	  String[] arg28={"-t","vehicleout.arff"};
//	  String[] arg29={"-t","lymph.arff"};
//	  String[] arg30={"-t","austraout.arff"};
//	  String[] arg31={"-t","hepatiout.arff"};
//	  String[] arg32={"-t","germanout2.arff"};
//	  String[] arg33={"-t","sickout.arff"};
//	  String[] arg34={"-t","horseout2.arff"};
//	  String[] arg35={"-t","annealout.arff"};
//	  String[] arg36={"-t","sonarout2.arff"};


	  //balance-scale	balloons	car	lenses	tic-tac-toe	   ionoout2   pima	tae		haberman
	  //glass	breastout	cmcout		ecoli	//liver-disorder//bupaout		post	hypoout2	yeast	
	  //autoout   cleveout	diabetes	heart	 iris	labor	led7	wine	 zoo	crx		vehicleout 	 lymph 
	  //austraout	hepatiout	germanout2	 sickout2	horseout2	annealout		sonarout2	   9atrr.arff
//	  runClassifier(new ACWV(), arg1);
//	  runClassifier(new ACWV(), arg2);
//	  runClassifier(new ACWV(), arg3);
//	  runClassifier(new ACWV(), arg4);
//	  runClassifier(new ACWV(), arg5);
//	  runClassifier(new ACWV(), arg6);
//	  runClassifier(new ACWV(), arg7);
//	  runClassifier(new ACWV(), arg8);
//	  runClassifier(new ACWV(), arg9);
//	  runClassifier(new ACWV(), arg10);
//	  runClassifier(new ACWV(), arg11);
//	  runClassifier(new ACWV(), arg12);
//	  runClassifier(new ACWV(), arg13);
//	  runClassifier(new ACWV(), arg14);
//	  runClassifier(new ACWV(), arg15);
//	  runClassifier(new ACWV(), arg16);
//	  runClassifier(new ACWV(), arg17);
//	  runClassifier(new ACWV(), arg18);
//	  runClassifier(new ACWV(), arg19);
//	  runClassifier(new ACWV(), arg20);
//	  runClassifier(new ACWV(), arg21);
//	  runClassifier(new ACWV(), arg22);
//	  runClassifier(new ACWV(), arg23);
//	  runClassifier(new ACWV(), arg24);
//	  runClassifier(new ACWV(), arg25);
//	  runClassifier(new ACWV(), arg26);
//	  runClassifier(new ACWV(), arg27);
//	  runClassifier(new ACWV(), arg28);
//	  runClassifier(new ACWV(), arg29);
//	  runClassifier(new ACWV(), arg30);
//	  runClassifier(new ACWV(), arg31);
//	  runClassifier(new ACWV(), arg32);
//	  runClassifier(new ACWV(), arg33);
//	  runClassifier(new ACWV(), arg34);
//	  runClassifier(new ACWV(), arg35);
//	  runClassifier(new ACWV(), arg36);

//	  String[] arg1 ={"-t","lenses.arff"};
//	  String[] arg2 ={"-t","ionoout2.arff"};
//	  String[] arg3 ={"-t","pimaout.arff"};
//	  String[] arg4 ={"-t","habermanout.arff"};
//	  String[] arg5 ={"-t","glassout.arff"};
//	  String[] arg6 ={"-t","cleveout.arff"};
//	  String[] arg7 ={"-t","diabetesout.arff"};
//	  String[] arg8 ={"-t","heartout.arff"};
//	  String[] arg9 ={"-t","led7.arff"};
//	  String[] arg10={"-t","wineout.arff"};
//	  String[] arg11={"-t","zoo.arff"};
//	  String[] arg12={"-t","crxout.arff"};
//	  String[] arg13={"-t","vehicleout.arff"};
//	  String[] arg14={"-t","lymph.arff"};
//	  String[] arg15={"-t","austraout.arff"};
//	  String[] arg16={"-t","hepatiout.arff"};
	  String[] arg17={"-t","data/irisD.arff","-i"};
//	  String[] arg18={"-t","sickout2.arff"};
//	  String[] arg19={"-t","annealout.arff"};
//	  String[] arg20={"-t","sonarout2.arff"};


//	  runClassifier(new ACWV(), arg1);
//	  runClassifier(new ACWV(), arg2);
//	  runClassifier(new ACWV(), arg3);
//	  runClassifier(new ACWV(), arg4);
//	  runClassifier(new ACWV(), arg5);
//	  runClassifier(new ACWV(), arg6);
//	  runClassifier(new ACWV(), arg7);
//	  runClassifier(new ACWV(), arg8);
//	  runClassifier(new ACWV(), arg9);
//	  runClassifier(new ACWV(), arg10);
//	  runClassifier(new ACWV(), arg11);
//	  runClassifier(new ACWV(), arg12);
//	  runClassifier(new ACWV(), arg13);
//	  runClassifier(new ACWV(), arg14);
//	  runClassifier(new ACWV(), arg15);
//	  runClassifier(new ACWV(), arg16);
	  System.out.println("dataset:iris");
	  runClassifier(new ACWV(), arg17);
//	  runClassifier(new ACWV(), arg18);
//	  runClassifier(new ACWV(), arg19);
//	  runClassifier(new ACWV(), arg20);

	  
	  LogProcess.logProcess(fileName);
//	runClassifier(new ACWV(), arg1);
	  
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
		  runClassifier(new ACWV(),arg);
	  }
	  LogProcess.logProcess(fileName);
  }
}
