package associations;
import java.util.*;
import weka.core.*;
public class LabelTnode {
	 public LabeledItemSet value;
	 public ArrayList<LabelTnode> child=new ArrayList<LabelTnode>();      //子结点
	 public int chnum;
	 public LabelTnode father;       //父结点
	 public int size(){
		 int result=0;
		 for(int i=0;i<value.m_items.length;i++){
			 if(value.m_items[i]!=-1){
				 result++;
			 }
		 }
		 return result;
	 }
	 public LabelTnode(LabeledItemSet s1){
	    	father=null;
	        child=new ArrayList<LabelTnode>();
	        value=s1.copy();
	        chnum=0;
	    }
	 public LabelTnode(){
	    	father=null;
	    	chnum=0;
//	        value=new LabelTnode();
	        child=new ArrayList<LabelTnode>();

	    }
	 public LabelTnode(LabelTnode node){
	    	father=null;
	        child=new ArrayList<LabelTnode>();;
	        value=node.value.copy();
	        chnum=0;
	    }
	 public void addChild(LabelTnode ch){ 
	    	child.add(ch);
	    	chnum++;
	    	ch.father=this;
	    } 
	 public boolean equal(LabelTnode ch){
	    	if(value.equals(ch.value))
	    		return true;
	    	return false;
	    }
	 public boolean Condequal(LabelTnode ch){
	    	if (value.equalCondset(ch.value))
	    		return true;
	    	return false;
	    } 
	 public boolean containedBy(ArrayList<LabelTnode> list){
		    for (int i = 0; i < list.size(); i++){
		    	LabelTnode node=(LabelTnode)list.get(i);
		    	if(this.equal(node)){
		    		return true;
		    	}
		    }
		      return false;
	 }
	 public ArrayList<LabelTnode> merge(ArrayList<LabelTnode> list){
		 ArrayList<LabelTnode> result=new ArrayList<LabelTnode>();
	     for(int i=0;i<list.size();i++){
	    	 boolean fla=true;
	    	 LabelTnode node=list.get(i);
	    	 LabelTnode n=new LabelTnode();
	    	 n.value.m_items=new int[value.m_items.length];
	    	 if(value.m_counter>=node.value.m_counter){
	    	    n.value.m_counter=node.value.m_counter;
	    	 }
	    	 else n.value.m_counter=value.m_counter;
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
	 }
	 public FastVector mergeVector(FastVector list){
		 FastVector result=new FastVector();
	     for(int i=0;i<list.size();i++){
	    	 boolean fla=true;
	    	 LabeledItemSet node=(LabeledItemSet)list.elementAt(i);
	    	 int totalt=node.m_totalTransactions;
	    	 int clas=node.m_classLabel;
	    	 LabeledItemSet nset=new LabeledItemSet(totalt,clas);
	    	 nset.m_items=new int[node.m_items.length];
	    	 if(value.m_classLabel!=node.m_classLabel)
	    		 continue;
	    	 if(value.m_ruleSupCounter > node.m_ruleSupCounter){
	    	    nset.m_ruleSupCounter = node.m_ruleSupCounter;
	    	 }
	    	 else nset.m_ruleSupCounter=value.m_ruleSupCounter;
	    	 
	    	 for(int j=0;j<value.m_items.length;j++){
	    		 if(value.m_items[j]!=-1&&node.m_items[j]!=-1){
	    			 fla=false;
	    			 break;
	    		 }
	    		 else if(value.m_items[j]!=-1){
	    				 nset.m_items[j]=value.m_items[j];
	    		 }
	    		 else {
	    			 nset.m_items[j]=node.m_items[j];
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
