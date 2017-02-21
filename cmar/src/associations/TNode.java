package associations;
import java.io.Serializable;
import java.util.*;
import weka.core.*;
public class TNode implements Serializable{
	static final long serialVersionUID = 7684467755712672058L;
	 public byte value;           
	 public byte attr;
	 public int m_counter;                   
	 public LinkedList<TNode> child;     
	 public TNode father;  
	 public int[] sup;
	 public TNode(byte n,byte v){
	    	father=null;	     
	        child=new LinkedList<TNode>();
	        value=v;
	        attr=n;
	        m_counter = 0;
	    }
	 public TNode(){
	    	father=null;
	        child=new LinkedList<TNode>();
	        value=-1;
	        attr=-1;
	        m_counter = 0;
	    }
	 public TNode(ItemSet is){
	    	father=null;       
	        child=new LinkedList<TNode>();
	        attr = (byte)is.hashCode();
	        value = (byte)is.m_items[attr];
	        m_counter = is.m_counter;
	    }
	 public TNode(TNode node){
	    	father = null;
	        child = new LinkedList<TNode>();;
	        value = node.value;
	        attr = node.attr;
	        m_counter = node.m_counter;
	    }
	 public void addChild(TNode ch){ 
	    	child.add(ch);
	    	ch.father=this;
	    } 
	 public boolean equal(TNode ch){
	    	if((value == ch.value) && attr == ch.attr)
	    		return true;
	    	return false;
	    } 
	 
	 public boolean equals(TNode ch){
	    	if((value == ch.value) && attr == ch.attr)
	    		return true;
	    	return false;
	 } 
	 
	 int hashcode(){
		 return attr;
	 }
	 
	 public boolean containedBy(ArrayList<TNode> list){
		    int size = list.size();
		    TNode[] nodelist = (TNode[])list.toArray();
		    for (int i = 0; i < size; i++){
		    	TNode node= nodelist[i];
		    	if(equal(node)){
		    		return true;
		    	}
		    }
		      return false;
	 }
	 public boolean containedBy(FastVector list){
	    for (int i = 0; i < list.size(); i++){
	    	TNode node=(TNode)list.elementAt(i);
	    	if(equal(node)){
	    		return true;
	    	}
	    }
	      return false;
      }
	 public boolean containedBy(Instance instance) {
		   if (instance.isMissing(attr))
		        return false;
		   if ((byte)instance.value(attr) != value)
			   return false;
		    return true;
	 }
	 public void mergeVector(FastVector list){
	     for(int i=0;i<list.size();i++){
	    	 ItemSet node=(ItemSet)list.elementAt(i);    	 
	    	 if(m_counter < node.m_counter){
	    		 node.m_counter = m_counter;
	    	 }
	         if(node.m_items[attr] > -1)
	        	 continue;
	         node.m_items[attr] = value;
	     }
	     
	 }
	 public void mergeLVector(FastVector list){
	     for(int i=0;i<list.size();i++){
	    	 LabelItemSetII node=(LabelItemSetII)list.elementAt(i);    	 
	    	 if(m_counter < node.m_counter){
	    		 node.m_counter = m_counter;
	    	 }
	         if(node.m_items[attr] > -1)
	        	 continue;
	         node.m_items[attr] = value;
	     }
	     
	 }
}
