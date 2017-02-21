package associations;

public class LinkNode{
    public Object data;
    public LinkNode next;
    public LinkNode(){
    	data = null;
    	next = null;
    }
    public LinkNode(Object a){
    	data = a;
    	next = null;
    }
    public void setNext(LinkNode b){
    	next = b;
    }
    
    public static LinkNode headInsert(LinkNode head, Object b){
    	head.setNext(new LinkNode(b));
    	return head;
    }
    
    public static void headDelete(LinkNode head){
    	head = head.next;
    }
    public boolean isEmpty(){
    	if(data == null)
    		return true;
    	return false;
    }
    public int size(){
    	if(data == null)
    		return 0;
    	LinkNode node = this.next;
    	if (node == null)
    		return 1;
    	int i = 1;
    	while(node != null){
    		i++;
    		node = node.next;
    	}
    	return i;
    }
	
}
