package associations;
import java.util.Enumeration;

import weka.core.FastVector;
import weka.core.Instance;
//import java.util.ArrayList;

public class LabelListHead {
	   public FastVector next;
	   public int nextnum;
	   public LabeledItemSet is;
	   public LabelListHead(){
		   next=new FastVector();
		   nextnum=0;
//		   is=new LabeledItemSet();
	   }
	   public LabelListHead(LabeledItemSet set){
		   is=set.copy();
		   next=new FastVector();
		   nextnum=0;
	   }
	   public void addNext(LabelTnode t){
		   next.addElement(t);
		   nextnum++;
	   }
	   public boolean containedBy(FastVector list){
		   Enumeration enu = list.elements();
		      while (enu.hasMoreElements()) {
		    	  LabeledItemSet set=((LabelTnode)enu.nextElement()).value;
		    	 if(set.equals(is)){
		    		 return true;
		    	 }
		      }
		      return false;
//			 for (int i = 0; i < list.size(); i++){
//			   TNode node=(TNode)list.elementAt(i);
//			     if(items.equals(node.value)){
//			    	return true;
//			    	}
//			    }
//			      return false;
		}
	   public boolean containedBy(Instance instance) {
		   if(is.containedBy(instance))
		      return true;
		   else
			   return false;
		  }
		public boolean equal(LabelTnode ch){
		    	if (is.equals(ch.value))
		    		return true;
		    	return false;
		    }
}
