package mine;

import java.util.List;

import associations.ItemSet;
import associations.ListHead;
import associations.TNode;
import weka.core.FastVector;

public class Rule {
    public ItemSet ruleLeft;
    public double classLabel;
    public double confidence;
    public double support;
    public int ruleLength;
    public double supLeft;
    public double x2;
     
     public Rule(ItemSet ruleLeft,double classLabel,double support,double confidence){
    	 this.ruleLeft = ruleLeft;
    	 this.classLabel = classLabel;
    	 this.confidence = confidence;
    	 this.support = support;
    	 
    	 ruleLength = ruleLeft.size();
     }
     
     

	/**
	 * @param ruleLeft
	 * @param classLabel
	 * @param confidence
	 * @param support
	 * @param ruleLength
	 * @param supLeft
	 * @param x2
	 */
	public Rule(ItemSet ruleLeft, double classLabel, double support,double confidence, double supLeft,
			double x2) {
		this.ruleLeft = ruleLeft;
		this.classLabel = classLabel;
		this.confidence = confidence;
		this.support = support;
		this.ruleLength = ruleLength;
		this.supLeft = supLeft;
		this.x2 = x2;
		 ruleLength = ruleLeft.size();
	}



	@Override
	public String toString() {
		// TODO Auto-generated method stub
		StringBuilder result = new StringBuilder();
		result.append("rules:");
		for(int i = 0 ; i < ruleLeft.items().length ; i ++){
			if((ruleLeft.itemAt(i) != -1)){
			   result.append("("+i+","+ruleLeft.itemAt(i)+")");
			}
		}
		result.append(classLabel);
		return result.toString();
	}
     
     public boolean contains(TNode hn){
    	 boolean result = false;
 
         if(ruleLeft.itemAt(hn.attr) == hn.value){
        	 result = true;
         }
    	 return result;
     }
     
     public boolean containedBy(ListHead lh){
    	 boolean result = false;
    	 if(ruleLeft.itemAt(lh.attr) == lh.value){
    		 result = true;
    	 }
    	 return result;
     }

	@Override
	public boolean equals(Object obj) {
		// TODO Auto-generated method stub
		if(obj instanceof Rule){
			if(this.ruleLeft.equals(((Rule)obj).ruleLeft) && this.support == ((Rule)obj).support && this.confidence == ((Rule)obj).confidence){
				return true;
			}
			return false;
		}
		return false;
	}
     
     
}
