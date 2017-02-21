package test;

import java.util.LinkedList;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;

import associations.TNode;
import mine.CRTree;
import mine.CRTreeNode;

public class CRTreeMatcher extends BaseMatcher{
	private CRTree crtree;
	
	public CRTreeMatcher(CRTree crtree) {
		this.crtree = crtree;

	}

	@Override
	public boolean matches(Object item) {
		// TODO Auto-generated method stub
		boolean result = false;
		if(item instanceof CRTree){
			CRTree tree = (CRTree)item;
			if(tree.getHeadTable().size() == crtree.getHeadTable().size()){
				CRTreeNode curr1 = tree.getRoot();
				CRTreeNode curr2 = crtree.getRoot();
				LinkedList<TNode> l1 = new LinkedList<TNode>();
				LinkedList<TNode> l2 = new LinkedList<TNode>();
;
				while(curr1 != null && curr2 != null){
					if(curr1.equals(curr2)){
						l1.addAll(curr1.child);
						l2.addAll(curr2.child);
						if(!l1.isEmpty() && !l2.isEmpty()){
						  curr1 = (CRTreeNode) l1.remove();
						  curr2 = (CRTreeNode) l2.remove();
						}else{
					         if(l1.isEmpty()){
					        	 curr1 = null;
					         }
					         if(l2.isEmpty()){
					        	 curr2 = null;
					         }  
						}
						if(curr1 == null && curr2 == null){
							result = true;
						}
					}else{
						break;
					}
				}
			}
		}
		return result;
	}

	@Override
	public void describeTo(Description description) {
		// TODO Auto-generated method stub
		
	}

}
