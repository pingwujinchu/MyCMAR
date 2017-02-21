package test;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;

import associations.ItemSet;
import mine.Rule;
import weka.core.FastVector;

public class FastVectorArrayMatcher extends BaseMatcher{
	FastVector []arr;
	
	
	public FastVectorArrayMatcher(FastVector[] arr) {
		this.arr = arr;
	}

	@Override
	public boolean matches(Object item) {
		// TODO Auto-generated method stub
		boolean result = true;
		if(item instanceof FastVector[]){
			Object[]items = (Object[])item;
			for(int i = 0 ; i < arr.length ; i++){
				FastVector curr = (FastVector) items[i];
				FastVector pre = arr[i];
				if(curr.size() == pre.size()){
					for(int j = 0 ; j < curr.size() ; j++){
						Rule r = (Rule) curr.elementAt(j);
						Rule preIt = (Rule) pre.elementAt(j);
						if(!r.equals(preIt)){
							result = false;
							break;
						}
					}
				}else{
					result = false;
					break;
				}
			}
			return result;
		}
		return false;
	}

	@Override
	public void describeTo(Description description) {
		// TODO Auto-generated method stub
		
	}
      
}
