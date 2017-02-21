package associations;
import weka.core.FastVector;
import weka.core.Instances;
import weka.core.Instance;
import java.util.Hashtable;
import java.util.Enumeration;
import java.io.Serializable;
public class LabelItemSetII extends ItemSet implements Serializable{
	 protected int[] m_sup;
	 protected double[] m_conv;
	 public LabelItemSetII(int totalTrans, int classLabel,int sup){
			super(totalTrans);
			m_sup[classLabel] = sup;
		    }
	 
	 public LabelItemSetII(int totalTrans, int[] sup){
			super(totalTrans);
			m_sup = sup;

		    }
	 public LabelItemSetII(int totalTrans, TNode node,int len){
			super(node,totalTrans,len);
			m_sup = node.sup;
		    }
	 public LabelItemSetII(int totalTrans,int len){
			super(totalTrans);
			m_sup = new int[len];
		    }
	 public double getConv(int i)
	 {
	   return m_conv[i];
	 }
	    public final boolean equalCondset(Object itemSet) {

	        if ((itemSet == null) || !(itemSet.getClass().equals(this.getClass()))) {
	          return false;
	        }
	        if (m_items.length != ((ItemSet)itemSet).items().length)
	          return false;
	        for (int i = 0; i < m_items.length; i++)
	          if (m_items[i] != ((ItemSet)itemSet).itemAt(i))
	    	return false;
	        return true;
	      }
	    
//	    public final int size() {
//	        int s=0;
//	        for (int i=0; i<m_items.length; i++){
//	       	 if(m_items[i] != -1){
//	       		 s++;
//	       	 }
//	        }
//	   	 return s;
//	   	  }
	    public boolean smaller(LabelItemSetII set){
	    	
	    	int len = set.m_sup.length;
	    	if (len != m_sup.length)
	    		return false;
	    	for(int i = 0; i < len; i++){
	    		if(m_sup[i] > set.m_sup[i])
	    			return false;
	    	}
	    	return true;
	    }
	    public static FastVector mergeAllItemSets(FastVector itemSets, int size, 
			    int totalTrans) {
	    	
	        FastVector newVector = new FastVector();
	        LabelItemSetII result;
	        int numFound, k;
	        
	        for (int i = 0; i < itemSets.size(); i++) {
	  	  LabelItemSetII first = (LabelItemSetII)itemSets.elementAt(i);
	        out:
	  	  for (int j = i+1; j < itemSets.size(); j++) {
	  	      LabelItemSetII second = (LabelItemSetII)itemSets.elementAt(j);
	  	      while((!first.smaller(second)) && (!second.smaller(first))){
	  	    	  j++;
	  	    	 if(j == itemSets.size())
	  		      break out;
	  	    	second = (LabelItemSetII)itemSets.elementAt(j);
	  	        }
	  	     
	  	      result = new LabelItemSetII(totalTrans,first.m_sup.length);
	  	      if(first.smaller(second)){
	  	    	  for(int ii = 0; ii < result.m_sup.length; ii++){
	  	    		  result.m_sup[ii] = first.m_sup[ii];
	  	    	  }
	  	    	  result.m_counter = first.m_counter;
	  	      }else{
		  	    	  for(int ii = 0; ii < result.m_sup.length; ii++){
		  	    		  result.m_sup[ii] = second.m_sup[ii];
		  	    	  }
		  	    	  result.m_counter = second.m_counter;
	  	      }
	  	      result.m_items = new int[first.m_items.length];
	  	      
	  	      // Find and copy common prefix of size 'size'
	  	      numFound = 0;
	  	      k = 0;
	  	      while (numFound < size) {
	  		  if (first.m_items[k] == second.m_items[k]) {
	  		      if (first.m_items[k] != -1) 
	  			  numFound++;
	  		      result.m_items[k] = first.m_items[k];
	  		  } else 
	  		      continue out;
	  		  k++;
	  	      }
	  	      
	  	      // Check difference
	  	      while (k < first.m_items.length) {
	  		  if ((first.m_items[k] != -1) && (second.m_items[k] != -1))
	  		      break;
	  		  else {
	  		      if (first.m_items[k] != -1)
	  			  result.m_items[k] = first.m_items[k];
	  		      else
	  			  result.m_items[k] = second.m_items[k];
	  		  }
	  		  k++;
	  	      }
	  	      if (k == first.m_items.length) {
	  		  	  newVector.addElement(result);
	  	      }
	  	  }
	        }
	        
	      return newVector;

	    }
}