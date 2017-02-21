package associations;

public class CarFPtree {
	LabelTnode root;
    public CarFPtree(int num,int total){
    	int n[]=new int[num];
    	for(int i=0;i<num;i++){
    		n[i]=-1;
    	}
    	LabeledItemSet set=new LabeledItemSet(total,-1);
    	set.m_items=n;
    	root = new LabelTnode(set);
    }
    
   
}
