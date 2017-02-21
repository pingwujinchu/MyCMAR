package mine;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;

import associations.ItemSet;
import associations.ListHead;
import associations.TNode;
import prun.DBCoverUtil;
import weka.core.FastVector;
import weka.core.Instance;
import weka.core.Instances;

public class CRTree implements Serializable{
		CRTreeNode root;
		int numClass;
		FastVector headertable;
		int totalTransection;
		int numAttr;
		
		public CRTree(int numClass,FastVector headertable,Instances data,Instances only_Class) {
			this.numClass = numClass;
			totalTransection = data.numInstances();
			numAttr = data.numAttributes() ;
			root = new CRTreeNode(null,null,-1,-1, -1, (byte)-1, (byte)-1,-1,-1);
			root.child = new LinkedList();
			this.headertable = headertable;

			DBCoverUtil.data = data;
			DBCoverUtil.onlyClass = only_Class;
			DBCoverUtil.numPerClass = new int[numClass];
			for(int i = 0 ; i < numClass ; i++){
				DBCoverUtil.numPerClass[i] = 1;
			}

//			DBCoverUtil.numPerClass[0] = 10;
//			DBCoverUtil.numPerClass[1] = 5;
			DBCoverUtil.numsCover = new int[data.numInstances()];
		}
		
		/**
		 *  该方法用于将规则插入到CRTree中，已测试(通过)
		 * @param rule
		 *
		 */
		//待测试not test
		public void insertRules(Rule rule){
//		        cpbList.addElement(rule);
				CRTreeNode currentnode = root;
				//for all the header node from last to first
				int  coverNum = 0;
				for(int j=0;j<headertable.size();j++){
					ListHead hn = (ListHead) headertable.elementAt(j);
					CRTreeNode childnode = new CRTreeNode(hn.attr,hn.value);
					if(rule.containedBy(hn)){
						coverNum ++;
						Iterator<TNode> it = currentnode.child.iterator();//it traverse all the child of currentnode
						int flag = 0;
						while(it.hasNext()){//while currentnode has childs
							childnode = (CRTreeNode)it.next();
							//if currentnode has a child hold the same item with hn, increase its class count
							if(childnode.equal(hn)){
//								System.out.println("coverNum = "+coverNum);
//								hn.classcount[(int) rule.classLabel]+= rule.support;
								if(coverNum == rule.ruleLength){
									if(childnode.support != -1){    //如果规则的最后一个节点与另一条规则的最后一个节点重合，则需要对规则进行裁剪
										if(childnode.confidence < rule.confidence || (childnode.confidence == rule.confidence && childnode.support < rule.support)){
											childnode.support = rule.support;
											childnode.confidence = rule.confidence;
											childnode.classLabel = rule.classLabel;
											childnode.leftSup = rule.supLeft;
											childnode.x2 = rule.x2;
										}
									}else{
										childnode.support = rule.support;
										childnode.classLabel = rule.classLabel;
										childnode.confidence = rule.confidence;
										childnode.leftSup = rule.supLeft;
										childnode.x2 = rule.x2;
									}
								}else {
									//如果当前规则没有另一个规则泛化程度高，且排在其后，则将该规则删除。
									if(childnode.support != -1 && (childnode.confidence > rule.confidence || (childnode.confidence == rule.confidence && childnode.support > rule.support))){
										return;
									}
								}
								flag = 1;
								break;
							}
						}
						//if currentnode does not have a child hold the same item with hn, create a new child
						if(flag==0){
							childnode = new CRTreeNode(hn.attr,hn.value);
							if(coverNum == rule.ruleLength){
								childnode.support = rule.support;
								childnode.classLabel = rule.classLabel;
								childnode.confidence = rule.confidence;
								childnode.leftSup = rule.supLeft;
								childnode.x2 = rule.x2;
							}else{
								childnode.confidence = -1;
								childnode.support = -1;
							}
							//hn.classcount[(int) rule.classLabel]+= rule.support;
							hn.addNext(childnode);
							currentnode.addChild(childnode);
							childnode.father = currentnode;
						}
						currentnode = childnode;					
					}
				}
		}
		
		/**
		 * 该方法用于将多条规则插入到CRTree中，已测试（通过）
		 * @param rules
		 */
		public void insertRules(FastVector rules){
			int numCpb = rules.size();
			for(int i=0;i<numCpb;i++){
				insertRules((Rule)rules.elementAt(i));
			}
		}
		
		public void buildCARTree(FastVector headertable){
			this.headertable = headertable;
		}

		//将树结构输出，其中每一行的记录为headerNode，节点值(属性，值，支持度，置信度，父节点属性，父节点值)
	    public void printTree(){
	    	System.out.println("			root");
	    	for(int i = 0 ; i < headertable.size() ; i++){
	    		System.out.print("("+((ListHead)headertable.elementAt(i)).attr+","+((ListHead)headertable.elementAt(i)).value+")");
	    		FastVector fv = ((ListHead)headertable.elementAt(i)).next;
	    		for(int j = 0 ; j < fv.size() ; j++){
	    			CRTreeNode curr = ((CRTreeNode)fv.elementAt(j));
	    		    System.out.print("	("+curr.attr+","+curr.value+","+curr.classLabel+","+curr.confidence+","+curr.support+","+curr.father.attr+","+curr.father.value+")");
	    		}
	    		System.out.println();
	    	}
	    }
	    
	    //待测试
	    /**
	     * 该方法用于生成所有满足某一个实例的所有规则，并将规则返回。
	     * 已测试（通过）
	     * @param ins
	     * @return
	     */
	    public FastVector[] genCarRules(Instance ins){
	    	FastVector[] result = new FastVector[numClass];
	    	for(int i = 0 ; i < numClass ; i++){
	    		result[i] = new FastVector();
	    	}
	    	
	    	CRTreeNode currNode = root;
	        
	    	ItemSet iset2 = new ItemSet(numAttr,totalTransection);
	    	genRules(iset2,result,currNode,ins);
	    	return result;
	    }
	    
	    /**
	     * 该方法用于递归的产生所有满足特定实例的方法
	     * @param itemset
	     * @param result
	     * @param currNode
	     * @param ins
	     * 
	     */
	    public void genRules(ItemSet itemset,FastVector[]result,CRTreeNode currNode,Instance ins){
	    	ItemSet iset = new ItemSet(itemset);
	    	
	    	if(currNode != null){
	    		LinkedList child = currNode.child;
		    	Iterator<CRTreeNode> iterator = child.iterator();
		    	if(child.isEmpty()){
		    		currNode = null;
		    	}
		    	
		    	while(iterator.hasNext()){
		    		CRTreeNode childNode = iterator.next();
		    		if(childNode.containedBy(ins)){
		    			ItemSet is = new ItemSet(iset);
		    			is.addTNode(childNode);
		    			if(childNode.support != -1 && childNode.confidence != -1){
		    				is.setCounter((int)childNode.support);
		    				result[(int) childNode.classLabel].addElement(new Rule(new ItemSet(is),childNode.classLabel,childNode.support,childNode.confidence,childNode.leftSup,childNode.x2));
		    			}
		    			genRules(is,result,childNode,ins);
		    		}
		    	}
	    	}
	    }
	    
	    public FastVector getHeadTable(){
	    	return headertable;
	    }
	    
	    public CRTreeNode getRoot(){
	    	return root;
	    }
}
