package prun;

import mine.Rule;

public class X2TestUtil {
	public static double minValue = 40;
	
	//not test
	public static boolean prunByX2Test(Rule rule){
		boolean prun = false;
		if(rule.x2 > minValue){
			prun = true;
		}
		return prun;
	}
}
