package prun;

import mine.Rule;
import weka.associations.LabeledItemSet;
import weka.core.FastVector;
import weka.core.Instance;
import weka.core.Instances;

public class DBCoverUtil{
	public static int []numsCover;
	public static int []numPerClass;
	public static int deleteNum;
	public static Instances data;
	public static Instances onlyClass;
	public static boolean[] prunByDBCover(FastVector rules) {
		// TODO Auto-generated method stub
		boolean result[] = new boolean[rules.size()];
		
	    for(int i = 0 ; i < rules.size() ; i++){
	    	result[i] = prunByDBCover((Rule)rules.elementAt(i));
	    }
	    return result;
	}

	public static boolean  prunByDBCover(Rule rule) {
		// TODO Auto-generated method stub

		int coveredNum = 0;

		for(int i = 0 ; i < data.numInstances() ; i++){
			Instance ins = data.instance(i);
			if(rule.ruleLeft.containedBy(ins) && onlyClass.instance(i).value(0) == rule.classLabel){
				if(numsCover[i] < numPerClass[(int) rule.classLabel]){
					numsCover[i] ++;
					coveredNum ++;
				}
			}
		}
		if(coveredNum > 0){
			return true;
		}else{
			return false;
		}
	}

}
