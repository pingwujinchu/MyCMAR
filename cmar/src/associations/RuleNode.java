package associations;

import java.util.LinkedList;
import java.util.ListIterator;
public class RuleNode {
	public class Rules{
		public byte classlabel;
		public double conv;
		public Rules(){
			classlabel = -1;
			conv = 0;
		}
		public Rules(byte label,double c){
			classlabel = label;
			conv = c;
		}
	}
	 public byte value;           
	 public byte attr;                  
	 public LinkedList<RuleNode> child;     
//	 public RuleNode father;
	 public LinkedList<Rules> rule;
	 
	 public RuleNode(){
		 value = -1;
		 attr = -1;
//		 father = null;
		 child = new LinkedList<RuleNode>();
		 rule = new LinkedList<Rules>();
	 }
	 
	 public RuleNode(byte a,byte v){
		 value = v;
		 attr = a;
//		 father = null;
		 child = new LinkedList<RuleNode>();
		 rule = new LinkedList<Rules>();
	 }
	 
	 public void addChild(RuleNode ch){
		 if (child == null)
			 child = new LinkedList<RuleNode>();
		 child.add(ch);
//		 ch.father = this;
		 
	 }
	 
	 public boolean equal(RuleNode ch){
	    	if((value == ch.value) && attr == ch.attr)
	    		return true;
	    	return false;
	    } 
	 public void addRule(byte label,double con){
		 Rules r = new Rules(label,con);
		 if ( rule == null)
			 rule = new LinkedList<Rules>();
		 rule.add(r);		 
	 }
	 public void addRule(RuleNode node){ 
		 if ( rule == null)
			 rule = new LinkedList<Rules>();
		 rule.addAll(node.rule);		 
	 }
	 public static void addTail(RuleNode ruletail,LinkedList<TNode> tail){
		 ListIterator<TNode> nodeiter = tail.listIterator();
		 RuleNode tailnode = ruletail;
		 while (nodeiter.hasNext()){
			 TNode node = nodeiter.next();
			 RuleNode next = new RuleNode(node.attr,node.value);
			 tailnode.addChild(next);
			 tailnode = next;
		 }
		 tailnode.rule = ruletail.rule;
		 ruletail.rule = null;
	 }
	 public static void main(String[] args){
		 LinkedList<TNode> list = new LinkedList<TNode>();
		 for (byte i = 0;i < 5; i++){
			 TNode node = new TNode(i,i);
			 list.addFirst(node);
		 }
		 RuleNode root = new RuleNode();
		 byte j = 1;
		 double k = 2;
		 root.addRule(j,k);
		 addTail(root, list);
		 System.out.print("ok");
	 }
}
