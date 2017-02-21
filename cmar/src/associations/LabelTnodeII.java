package associations;

import java.util.*;
import weka.core.*;
public class LabelTnodeII {
	 public int m_counter;
	 public int [] m_items;
	 public int [] m_sup;       //类属性�?�为索引，对应支持度为�??//
	 public FastVector  child = new FastVector();      
	 public int chnum;
	 public LabelTnodeII  father;       
	 
	 public int size(){
		 int result=0;
		 for(int i=0;i<m_items.length;i++){
			 if(m_items[i]!=-1){
				 result++;
			 }
		 }
		 return result;
	 }
	 public LabelTnodeII(LabeledItemSet s1){
	    	father=null;
	        child=new FastVector();
	        chnum=0;
	        m_counter = s1.m_counter;
	        m_items = s1.m_items;
	        
	    }
	 public LabelTnodeII(){
	    	father=null;
	    	chnum=0;
	        child=new FastVector();
	        m_counter = 0;

	    }
	 public LabelTnodeII(LabelTnodeII node){
	    	father=null;
	        child=new FastVector();
	        chnum=0;
	        m_counter = node.m_counter;
	        m_items = node.m_items;
	        m_sup = node.m_sup;
	    }
	 public void addChild(LabelTnodeII ch){ 
	    	child.addElement(ch);
	    	chnum++;
	    	ch.father=this;
	    } 
	 public void removeChild(LabelTnodeII ch){ 
	    	for(int i = 0; i < chnum; i++){
	    	    LabelTnodeII tnode = (LabelTnodeII)child.elementAt(i);	
	    	    if (tnode.m_items.equals(ch.m_items)){
	    	    	child.removeElementAt(i);
	    	    	chnum--;
	    	    	return;
	    	    }
	    	}
	    	
	    } 
	 public boolean equal(LabelTnodeII ch){
	    	if(m_items.equals(ch.m_items) && m_sup.equals(ch.m_sup))
	    		return true;
	    	return false;
	    }
	 public boolean Condequal(LabelTnodeII ch){
	    	if (m_items.equals(ch.m_items))
	    		return true;
	    	return false;
	    } 
	 public boolean containedBy(ArrayList<LabelTnodeII> list){
		    for (int i = 0; i < list.size(); i++){
		    	LabelTnodeII node=(LabelTnodeII)list.get(i);
		    	if(this.Condequal(node)){
		    		return true;
		    	}
		    }
		      return false;
	 }
	 
/*  public ArrayList<LabelTnodeII> merge(ArrayList<LabelTnodeII> list){
		 ArrayList<LabelTnodeII> result=new ArrayList<LabelTnodeII>();
	     int size = list.size();
		 for(int i=0;i<size;i++){
	    	 boolean fla=true;
	    	 LabelTnodeII node=list.get(i);
	    	 LabelTnodeII n=new LabelTnodeII();
	    	 n.m_items=new int[m_items.length];
	    	 if(m_counter>=node.m_counter){
	    	    n.m_counter=node.m_counter;
	    	 }
	    	 else n.m_counter=m_counter;
	    	 if(value.m_classLabel!=node.value.m_classLabel)
	    		 continue;
	    	 n.value.m_classLabel=value.m_classLabel;
	    	 for(int j=0;j<value.m_items.length;j++){
	    		 if(value.m_items[j]!=-1&&node.value.m_items[j]!=-1){
	    			 fla=false;
	    			 break;
	    		 }
	    		 else if(value.m_items[j]!=-1){
	    				 n.value.m_items[j]=value.m_items[j];
	    		 }
	    		 else {
	    			 n.value.m_items[j]=node.value.m_items[j];
	    		 }
	    	 }
	    	 if(fla)
	    		 result.add(n);
	     }
	     return result;
	 }*/
	 public FastVector mergeVector(FastVector list){
		int size = list.size();
		 FastVector result=new FastVector();
	     for(int i = 0; i < size; i++){
	    	 boolean fla = true;
	    	 LabelItemSetII node=(LabelItemSetII)list.elementAt(i);
	    	 LabelItemSetII nset = new LabelItemSetII(node.m_totalTransactions,node.m_sup);
	    	 nset.m_items = new int[node.m_items.length];
             nset.m_counter = node.m_counter;
	    	 if( m_items.length != node.m_items.length)
             {
            	 fla = false;
            	 continue;
             }
             out:
	    	 for (int j = 0; j < node.m_items.length; j++){
	    		 if(m_items[j] != -1){
	    			 if(node.m_items[j] != -1){
	    				 fla = false;
	    				 break out;
	    			 }
	    			 else{
	    				 nset.m_items[j] = m_items[j];
	    			 }
	    		 }else{
	    			 nset.m_items[j] = node.m_items[j];
	    		 }
	    	 }
	    	 if(fla)
	    	 {
	    		 result.addElement(nset);
	    	 }
	     }
	     return result;
	 }
}

