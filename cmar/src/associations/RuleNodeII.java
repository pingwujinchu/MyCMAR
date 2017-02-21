package associations;

import java.util.LinkedList;


public class RuleNodeII {
	public class RuleII{
		public byte classlabel;
		public double conv;
		public RuleII(){
			classlabel = -1;
			conv = 0;
		}
		public RuleII(byte label,double c){
			classlabel = label;
			conv = c;
		}
	}
	public byte value;           
	public byte attr;
	public int chnum;
	public int rulenum;
	public RuleNodeII[] child;
    public RuleII[] rule;
    
    public RuleNodeII(){
		 value = -1;
		 attr = -1;	
		 chnum = 0;
		 rulenum = 0;
	 }
    public RuleNodeII(byte a,byte v){
		 value = a;
		 attr = v;	
		 chnum = 0;
		 rulenum = 0;
	}
    public void addChild(RuleNodeII ch){
    	if (child == null || child.length == 0){
    		child = new RuleNodeII[10];
    		child[0] = ch;
    		chnum ++;
    	}
    	else{
    		int len = child.length;
    		if (chnum < len){
    			child[chnum] = ch;
    			chnum++;
    		}
    		else{
    		 RuleNodeII[] newVector = new RuleNodeII[len+10];
    		 System.arraycopy(child, 0, newVector, 0, len);
    		 newVector[len] = ch;
    		 child = newVector;
    		 chnum++;
    		}
    	}
    }
    public void addRule(byte label,double con){
    	RuleII ch = new RuleII(label,con);
    	if (rule == null || rule.length == 0){
    		rule = new RuleII[3];
    		rule[0] = ch;
    		rulenum++;
    	}
    	else{
    		int len = rule.length;
    		if (rulenum < len){
    			rule[rulenum] = ch;
    			rulenum++;
    		}
    		else{
    		RuleII[] newVector = new RuleII[len+3];
    		System.arraycopy(rule, 0, newVector, 0, len);
    		newVector[len] = ch;
    		rule = newVector;
    		rulenum ++;
    		}
    	}
    }
    public void addRule(RuleNodeII node){
    	
    	if (rule == null || rule.length == 0){
    		rule = node.rule;
    	}
    	else{
    		int len = rule.length;
    		int lenadd = node.rule.length;
    		RuleII[] newVector = new RuleII[len+lenadd];
    		System.arraycopy(rule, 0, newVector, 0, len);
    		System.arraycopy(node.rule, 0, newVector, len, lenadd);    		
    		rule = newVector;
    	}
    }
    public boolean equal(RuleNodeII ch){
    	if((value == ch.value) && attr == ch.attr)
    		return true;
    	return false;
    } 
    
}
