package associations;
import java.util.Enumeration;

import weka.core.FastVector;
import weka.core.Instance;
import weka.core.Instances;
import java.util.ArrayList;

public class ListHeadII {
	   public int count;
	   public byte attr;
	   public byte value;
	   public int[] sup;
	   public FastVector next;
	   public int nextnum;
	   public ListHeadII(){
		   count=0;
		   next=new FastVector();
		   attr = -1;
		   value = -1;
		   nextnum=0;
	   }
	   public ListHeadII(ItemSet is){
		   count = is.m_counter;
		   next=new FastVector();
		   attr = (byte)is.hashCode();
		   value = (byte)is.m_items[attr];
		   nextnum=0;
	   }
	   public ListHeadII(int c,byte item,byte v){
		   count=c;
		   attr=item;
		   value = v;
		   next=new FastVector();
		   nextnum=0;
	   }
	   public void addNext(TNode t){
		  
		   next.addElement(t);
		   nextnum++;
	   }
	   public void addNextII(LabelTnodeII t){
		   for (int i = 0; i < sup.length; i++){
			   sup[i] += t.m_sup[i];
		   }
		   next.addElement(t);
		   nextnum++;
	   }
	   public void addNextII(TNode t){
		   for (int i = 0; i < sup.length; i++){
			   sup[i] += t.sup[i];
		   }
		   next.addElement(t);
		   nextnum++;
	   }
	   public void addNextII(LabelTnodeII t,int[] rulesup){
		   for (int i = 0; i < sup.length; i++){
			   sup[i] += rulesup[i];
		   }
		   next.addElement(t);
		   nextnum++;
	   }
	   public void addNextII(TNode t,int[] rulesup){
		   for (int i = 0; i < sup.length; i++){
			   sup[i] += rulesup[i];
		   }
		   next.addElement(t);
		   nextnum++;
	   }

	   public boolean containedBy(Instance instance) {
		   if (instance.isMissing(attr))
		        return false;
		   if ((byte)instance.value(attr) != value)
			   return false;
		    return true;
		  }
	   public boolean containedBy(LabelItemSetII instance) {
		    for (int i = 0; i < instance.m_items.length; i++) 
		    {
		    	if (value != (int)instance.m_items[attr])
			        return false;
		    }
		    return true;
		  }
		public boolean equal(TNode ch){
		    	if((value == ch.value) && (attr == ch.attr))
		    		return true;
		    	return false;
		    }
		 public boolean containedBy(FastVector list){
			   Enumeration enu = list.elements();
			      while (enu.hasMoreElements()) {
			    	  TNode node = (TNode)enu.nextElement();
			    	 if(equal(node)){
			    		 return true;
			    	 }
			      }
			      return false;
			}
    public static FastVector singleton(Instances instances) throws Exception {
    	FastVector setOfItemSets = new FastVector();
    	ListHeadII current;
	    for (byte i = 0; i < instances.numAttributes(); i++) {
	    	if (instances.attribute(i).isNumeric())
	    		throw new Exception("Can't handle numeric attributes!");
	    	for (byte j = 0; j < instances.attribute(i).numValues(); j++) {
				current = new ListHeadII();
				current.attr = i;
				current.value = j;    
				setOfItemSets.addElement(current);
			   }
		}
	    return setOfItemSets;
	}
    public void upDateCounter(Instance instance) {
        if (containedBy(instance))
          count++;
      }
    public final void upDateCounter(Instance instanceNoClass, Instance instanceClass) {
    	if(sup == null)
    		sup = new int[instanceClass.attribute(0).numValues()];
        if (containedBy(instanceNoClass)){
        	count++;
            int classindex = (int)instanceClass.value(0);
            sup[classindex] ++;
        }
    }
    
    public static void upDateCounters(FastVector itemSets, Instances instances) {

        for (int i = 0; i < instances.numInstances(); i++) {
        	Enumeration enu = itemSets.elements();
        	 while (enu.hasMoreElements()) {
		    	  ListHeadII node = (ListHeadII)enu.nextElement();
		    	 if(node != null){
		    		 node.upDateCounter(instances.instance(i));
		    	 }
		      }
          }
    }
    public static void upDateCounters(FastVector itemSets, Instances instancesNoClass, Instances instancesClass){

        for (int i = 0; i < instancesNoClass.numInstances(); i++) {
        	Enumeration enu = itemSets.elements();
        	 while (enu.hasMoreElements()) {
		    	  ListHeadII node = (ListHeadII)enu.nextElement();
		    	 if(node != null){
		    		 node.upDateCounter(instancesNoClass.instance(i), instancesClass.instance(i));
		    	 }
		      }
          }
    }
    public static FastVector deleteItemSets(FastVector itemSets, 
			  int minSupport, int maxSupport) {
    	FastVector newVector = new FastVector();
    	Enumeration enu = itemSets.elements();
    	while (enu.hasMoreElements()) {
    		 ListHeadII current = (ListHeadII)enu.nextElement();
    		 if ((current.count > minSupport) 
     				&& (current.count <= maxSupport))
     			newVector.addElement(current);
    	}
    	return newVector;
    }
}
