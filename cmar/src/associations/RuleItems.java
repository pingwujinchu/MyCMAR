package associations;

import java.io.Serializable;

import weka.core.Instances;

public class RuleItems implements Serializable{
	private static final long serialVersionUID = 2724000045282835792L;
	public byte[] m_items;
	public double conv;
	public RuleItems(byte[] item,double v){
		m_items  = item;
		conv = v;
	}
	public String toString(Instances instances,Instances onlyClass) {

	    StringBuffer text = new StringBuffer();
        int i = 0;
	    for (i = 0; i < instances.numAttributes(); i++){
	      if (m_items[i] != -1) {
	    	  text.append(instances.attribute(i).name()+'=');
	    	  text.append(instances.attribute(i).value(m_items[i])+' ');
		  }
	    }
	    text.append(" => ");
		text.append(onlyClass.attribute(0).name()+'=');
		text.append(onlyClass.attribute(0).value(m_items[i])+' ');
		text.append("  conv:  ");
	    text.append(conv);
	    return text.toString();
	  }
}
