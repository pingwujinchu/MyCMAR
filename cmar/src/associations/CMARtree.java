package associations;

public class CMARtree {
	TNode root;
    public CMARtree(int numClass){   	
    	root = new TNode();
    	root.sup = new int[numClass];
        for (int i = 0; i < numClass; i++){
    	    root.sup[i] = 0;	
    	}
    }
    
}
