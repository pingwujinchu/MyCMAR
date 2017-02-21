package associations;

public class FPtree {
	TNode root;
	public FPtree(){
		root = new TNode();
	}
    public FPtree(int numClass){
    	root = new TNode();
    	root.sup = new int[numClass];
        for (int i = 0; i < numClass; i++){
    	    root.sup[i] = 0;	
    	}
    }
    
   
}
