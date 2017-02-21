import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.HashMap;

/* -------------------------------------------------------------------------- */
/*                                                                            */
/*                          R U L E   N O D E                                 */
/*                                                                            */
/*                            Frans Coenen                                    */
/*                                                                            */
/*                        Tuesday 14 Match 2006                               */
/*                         (Revision 11/10/2006)                              */
/*                                                                            */
/*                    Department of Computer Science                          */
/*                     The University of Liverpool                            */
/*                                                                            */
/* -------------------------------------------------------------------------- */

/** Class for storing binary tree of ARs or CARs as appropriate. (Used to
be defined as an inner class in AssocRuleMining class.)
@author Frans Coenen
@version 11 October 2006 */

/* To Compile: javac RuleNode.java */

public class RuleNode {

    /* ------ FIELDS ------ */

    /** Antecedent of AR. */
    public short[] antecedent;

    /** Consequent of AR. */
    public short[] consequent;

    /** The confidence value associate with the rule represented by this
    node. <P>Same field is used if rules are ordered in some other way,
    e.g. Laplace accuracy or Chi^2 values. */
    public float confidenceForRule= (float) 0.0;
    
    /** The support for a rule. <P>Again same field may be used for some
    other ordering value. */
    public float supportForRule= (float) 0.0;

    /** Rule number, added when bin-tree containing rules is complete. */
    public short ruleNumber=0;

    /** Links to next node */
    public RuleNode leftBranch  = null;
    public RuleNode rightBranch = null;

    /* ------ CONSTRUCTOR ------ */

    /** Three argument constructor
    @param ante the antecedent (LHS) of the AR/CAR.
    @param cons the consequent (RHS) of the AR/CAR.
    @param confValue the associated "confidence" value.
    @param suppValue the associate "support" value.     */

    public RuleNode(short[] ante, short[]cons, double confValue, 
                                                          double suppValue) {
	antecedent        = ante;
	consequent        = cons;
	confidenceForRule = (float) confValue;
        supportForRule    = (float) suppValue;
	}

    /* ------ METHODS ------ */

public String[] getAntecedantName() throws IOException{
    	
    	if (this.antecedent == null) return null;
    	else{
    	
    	String[] antecedantNames = new String[this.antecedent.length];
    	File file2 = new File("relation.txt");
        FileInputStream f2 = new FileInputStream(file2);
        ObjectInputStream s2 = new ObjectInputStream(f2);
        try {
			HashMap<String, Object> fileObj2 = (HashMap<String, Object>) s2.readObject();
			for(int i=0;i<this.antecedent.length;i++){
				antecedantNames[i] = (String) fileObj2.get(Short.toString(this.antecedent[i]));
			}
			
			
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        s2.close();
    	return antecedantNames;
    	}
    }
    
    public String[] getConsequentName() throws IOException{
    	
    	String[] consequentNames = new String[this.consequent.length];
    	
    	File file2 = new File("produitRelations.txt");
        FileInputStream f2 = new FileInputStream(file2);
        ObjectInputStream s2 = new ObjectInputStream(f2);
        try {
			HashMap<String, Object> fileObj2 = (HashMap<String, Object>) s2.readObject();
			for(int i=0;i<this.consequent.length;i++){
				consequentNames[i] = (String) fileObj2.get(Short.toString(this.consequent[i]));
			}
			
			
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        s2.close();
    	return consequentNames;
    }

    }
