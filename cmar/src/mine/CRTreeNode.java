package mine;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import associations.ListHead;
import associations.TNode;

public class CRTreeNode extends TNode{
	public CRTreeNode father;
	public double classLabel;
	public double support;
	public double confidence;
	public double x2;
	public double leftSup;

	
	public CRTreeNode(CRTreeNode parent, LinkedList child, double classLabel, double support, double confidence, byte attr,
			byte value ,double leftSup,double x2) {
		super();
		this.father = parent;
		this.child = child;
		this.classLabel = classLabel;
		this.support = support;
		this.confidence = confidence;
		this.attr = attr;
		this.value = value;
		this.leftSup = leftSup;
		this.x2 = x2;
	}

	public CRTreeNode(byte attr, byte value) {
		super();
		this.attr = attr;
		this.value = value;
		this.child = new LinkedList();
	}
	
	public void addChild(CRTreeNode child){
		this.child.add(child);
	}
	
	public boolean equal(ListHead lh){
		if(this.attr == lh.attr &&this.value == lh.value){
			return true;
		}else{
			return false;
		}
	}

	@Override
	public boolean equals(TNode ch) {
		// TODO Auto-generated method stub
		if(ch instanceof CRTreeNode){
			CRTreeNode curr = (CRTreeNode)ch;
			if(this.attr == curr.attr && this.value == curr.value && this.classLabel == curr.classLabel){
				return true;
			}else{
				return false;
			}
		}
		return false;
	}
	
	
	
}
