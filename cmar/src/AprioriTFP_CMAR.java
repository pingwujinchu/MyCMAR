/* -------------------------------------------------------------------------- */
/*                                                                            */
/*                             APRIORI-TFP CMAR                               */
/*              (CLASSIFICATION BASED ON MULTIPLE ASSOCIATION RULES)          */
/*                                                                            */
/*                               Frans Coenen                                 */
/*                                                                            */
/*                             Tuesday 2 March 2004                           */
/*             (Bug fixes and maintenance: 12/10/2006, 5/3/2012)              */
/*                                                                            */
/*                       Department of Computer Science                       */
/*                        The University of Liverpool                         */
/*                                                                            */
/* -------------------------------------------------------------------------- */

/* Class structure

AssocRuleMining
      |
      +-- TotalSupportTree
                |
		+-- PartialSupportTree
			|
			+--AprioriTFPclass
				|
				+--AprioriTFP_CARgen
					|
					+-- AprioriTFP_CMAR		*/

// Java packages

import java.util.*;			      
import java.io.*;

// Java GUI packages
import javax.swing.*;

/** Methods to produce classification rules using Wenmin Li, Jiawei Han and 
Jian Pei's CMAR (Classification based on Multiple associate Rules) algorithm 
but founded on Apriori-TFP. Assumes that input dataset is orgnised such that 
classifiers are at the end of each record. Note: number of classifiers value is 
stored in the <TT>numClasses</TT> field.
@author Frans Coenen
@version 12 October 2006 */

public class AprioriTFP_CMAR extends AprioriTFP_CARgen {

    /* ------ FIELDS ------ */

    // --- Constants ---
    /** Minimum times a record mist be covered */
    protected static int MIN_COVER=3; 	// At least 3 rules
    
    // --- Chi-Squared Testing Constants ---    
    /** Critical threshold for 10% "significance" level (assuming "degree of
    freedom" equivalent to 1). */
    protected static final double THRESHOLD_10    = 2.7055;
    /** Critical threshold for 5% "significance" level (assuming "degree of
    freedom" equivalent to 1). */
    protected static final double THRESHOLD_5     = 3.8415;
    /** Critical threshold for 2.5% "significance" level (assuming "degree of
    freedom" equivalent to 1). */
    protected static final double THRESHOLD_2HALF = 5.0239;
    /** Critical threshold for 1% "significance" level (assuming "degree of
    freedom" equivalent to 1). */
    protected static final double THRESHOLD_1     = 6.6349;
    /** Critical threshold for 0.5% "significance" level (assuming "degree of
    freedom" equivalent to 1). */
    protected static final double THRESHOLD_HALF  = 7.8794;
    
    // --- Data structures ---
    /** Nested Class. Rule node in linked list of rules for CMAR classification 
    rules. */

    protected class RuleNodeCMAR {
    	/** Antecedent of AR. */
		protected short[] antecedent;
		/** Consequent of AR. */
		protected short[] consequent;
		/** The confidence value associate with the rule represented by this
		node. */
		double confidenceForRule=0.0;
		/** The support value associate with the rule represented by this
		node. */
		double supportForRule=0.0;
		/** The support value associate with the antecedent of the rule 
		represented by this node. */
		double suppAntecedent=0.0;
		/** The support value associate with the consequent of the rule 
		represented by this node. */
		double suppConsequent=0.0;
		/** Link to next node */
		RuleNodeCMAR next = null;
	
		/** Six argument constructor
		@param antecedent the antecedent (LHS) of the AR.
    	@param consequent the consequent (RHS) of the AR.
    	@param suppValue the associated support value. 
		@param suppAnte the associated support for the antecedent.
		@param suppCons the associated support for the consequent.
		@param consvalue the associated confidence value.   */
	
		private RuleNodeCMAR(short[] ante, short[]cons, double suppValue,
	      	      double suppAnte, double suppCons, double confValue) {
	    	antecedent        = ante;
	    	consequent        = cons;
	    	supportForRule    = suppValue;
	    	suppAntecedent    = suppAnte;
	    	suppConsequent    = suppCons;
	    	confidenceForRule = confValue;
	    	}
		}
	
    /** The reference to start of the CMAR rule list. */
    protected RuleNodeCMAR startCMARrulelist = null;
    
    // --- Chi-Squared Testing Varibales ---
    /** 1-D array for observed values for Chi-Squared Testing. */
    private double[] obsValues = new double[4];
    /** 1-D array for expected values for Chi-Squared Testing. */
    private double[] expValues = new double[4];
    /** Support value for the antecedent of the rule. */
    private double supAntecedent;
    /** Support value for NOT the antecedent of the rule. */
    private double supNotAntecedent;
    /** Support value for the concequent of the rule. */
    private double supConsequent;
    /** Support value for NOT the concequent of the rule. */
    private double supNotConsequent;
    /** Support for the rule. */
    private double supRule;	
    /** Number of records in the input (training) sets. */
    private double numRecords;
    /** Current critical threshold value. */
    protected double threshold = THRESHOLD_5;		// Default

    /* ------ CONSTRUCTORS ------ */

    /** Constructor processes command line arguments.
    @param args the command line arguments (array of String instances). */

    public AprioriTFP_CMAR(String[] args) {
	super(args);
	}

    /** Constructor with argument from existing instance of class
    AssocRuleMining.
    @param armInstance the given instance of the <TT>AssocRuleMining</TT>
    class. */

    public AprioriTFP_CMAR(AssocRuleMining armInstance) {
	super(armInstance);
        }
	
    /* ------ METHODS ------ */

    /*----------------------------------------------------------------------- */
    /*                                                                        */
    /*                       START CMAR CLASSIFICATION                        */
    /*                                                                        */
    /*----------------------------------------------------------------------- */
    
    /* START CMAR CLASSIFICATION */
    
    /** Starts CMAR classifier generation proces. <P> Proceeds as follows:<OL>	
    <LI>Generate all CARs using Apriori-TFP and place selected CARs into linked
    list of rules.
    <LI>Prune list according the cover stratgey.
    <LI>Test classification using Chi-Squared Weighting approach.</OL>.	*/

    public void startClassification() {
        String s = "START APRIORI-TFP CMAR\n------------------------------\n" +
	                                "Min. rules to cover  = " + MIN_COVER +
	                              "\nCrit. threshold val. = " + threshold + 
	                           "\nMax number of CARS   = " + MAX_NUM_CARS +
	           "\nMax size antecedent  = " + MAX_SIZE_OF_ANTECEDENT + "\n";
		if (textArea==null) System.out.println(s);
        else textArea.append(s);

		// Proceed
		startClassification2();
		}

    /** Starts CMAR classifier generation proces, GUI version.		*/

    public void startClassification(JTextArea tArea) {
        // Set text area
        textArea = tArea;

        // proceed
        startClassification();
		}

    /** Continues process of starting the CMAR classifier generation proces.	*/

    protected void startClassification2() {
        // Set global rule list reference to null
        startCMARrulelist = null;

        // Generate all CARs using Apriori-TFP and place selected CARs into
        // linked list of rules.
        startCARgeneration2();

        // Prune linked list of rules using "cover" principal
        pruneUsingCover(copyItemSet(dataArray));

        // Output rule list
        if (ruleListAttNumOutputFlag || ruleListSchemaOutputFlag) 
        													outputCMARrules();

        // Test classification using the test set.
        testClassification();
        }
        		
    /*----------------------------------------------------------------------- */
    /*                                                                        */
    /*             APRIORI-TFP CMAR WITH TEN CROSS VALIDATION (TCV)           */
    /*                                                                        */
    /*----------------------------------------------------------------------- */

    /* COMMEMCE TEN CROSS VALIDATION WITH OUTPUT*/

    /** Start CMAR Ten Cross Validation (TCV) process with output of individual
    accuracies. */

    public void commenceTCVwithOutput() {
        double[][] parameters = new double[10][5];
		String s = "START TCV APRIORI-TFP CMAR CLASSIFICATION\n" +
	            "------------------------------\nMin. rules to cover  = " +
		       MIN_COVER + "\nMax number of CARS   = " + MAX_NUM_CARS +
	           "\nMax size antecedent  = " + MAX_SIZE_OF_ANTECEDENT + "\n";
		if (textArea==null) System.out.println(s);
        else textArea.append(s);

		// Loop through tenths data sets
		for (int index=0;index<10;index++) {
	    	s = "[--- " + (index+1) + " ---] ";
	    	if (textArea==null) System.out.println(s);
            else textArea.append(s);
	    	// Create training and test sets
	    	createTrainingAndTestDataSets(index);
	    	// Mine data, produce T-tree and generate CRs
	    	startClassification();
	    	parameters[index][0] = accuracy;
	    	parameters[index][1] = aucValue;
	    	parameters[index][2] = numFrequentSets;
	    	parameters[index][3] = numUpdates;
	    	parameters[index][4] = getNumCMAR_CRs();
	    	}

		// Output
		tcvOutput(parameters);
		}

    /** Start CMAR Ten Cross Validation (TCV) process with output of individual
    accuracies, GUI version.
    @param tArea the given instance of the <TT>JTextArea</TT> class. */

    public void commenceTCVwithOutput(JTextArea tArea) {
        textArea = tArea;

        // Proceed
        commenceTCVwithOutput();
        }
	
    /*----------------------------------------------------------------------- */
    /*                                                                        */
    /*            CLASSIFICATION ASSOCIATION RULE (CAR) GENERATION            */
    /*                                                                        */
    /*----------------------------------------------------------------------- */

    /* GENERATE CLASSIFICATION ASSOCIATION RULES (RIGHT LEVEL). */
    
    /** Generating classification association rules from a given array of 
    T-tree nodes. <P> For each rule generated add to rule list if: (i) 
    Chi-Squared value is above a specified critical threshold (5% by default), 
    and (ii) the CR tree does not contain a more general rule with a higher 
    ordering. Rule added to rule list according to CMAR ranking (ordering). 
    @param itemSetSofar the label for a T-treenode as generated sofar.
    @param size the length/size of the current array lavel in the T-tree.
    @param consequent the current consequent (classifier) for the CAR.
    @param linkRef the reference to the current array lavel in the T-tree. */
	
    protected void generateCARsRightLevel(short[] itemSetSofar, int size, 
    				    short[] consequent, TtreeNode[] linkRef) {    
        // Loop through T-tree array
		for (int index=1; index < size; index++) {
	    	// Check if node exists
	    	if (linkRef[index] != null) {
	        	// Generate Antecedent
	        	short[] tempItemSet = realloc2(itemSetSofar,(short) index);
	        	// Determine confidence
	        	double suppForAntecedent = (double)
		   		      getSupportForItemSetInTtree(tempItemSet);
	        	double confidenceForCAR = getConfidence(suppForAntecedent,
		    				       linkRef[index].support);
	        	// Add CAR to linked list structure if confidence greater
	        	// than minimum confidence threshold.
	        	if (confidenceForCAR >= confidence) { 
		    		numCarsSoFar++;
		    		double suppForConcequent = (double)
		   		      					getSupportForItemSetInTtree(consequent);
		    		insertRinRlistCMARranking(tempItemSet,consequent,
			                  				suppForAntecedent,suppForConcequent,
			        			     linkRef[index].support,confidenceForCAR);
	            	}
				} 
	    	}
		}

    /* ---------------------------------------------------------------- */
    /*                                                                  */
    /*        RULE LINKED LIST ORDERED ACCORDING TO CMAR RANKING        */
    /*                                                                  */
    /* ---------------------------------------------------------------- */
    
    /* Methods for inserting rules into a linked list of rules ordered 
    according to CMAR ranking. Each rule described in terms of 4 fields: 1) 
    Antecedent (an item set), 2) a consequent (an item set), 3) a total support 
    value and 4) a confidence value (double). */
    
    /* INSERT (ASSOCIATION/CLASSIFICATION) RULE INTO RULE LINKED LIST (ORDERED
    ACCORDING CONFIDENCE). */
    
    /** Inserts an (association/classification) rule into the linkedlist of
    rules pointed at by <TT>startRulelist</TT>. <P> List is ordered according 
    to "CMAR" ranking. 
    @param antecedent the antecedent (LHS) of the rule.
    @param consequent the consequent (RHS) of the rule.
    @param supportForAntecedent the associated support for the antecedent.
    @param supportForConsequent the associated support for the consequent.
    @param supportForRule the associated support value. 
    @param confidenceForRule the associated confidence value. */
    
    protected void insertRinRlistCMARranking(short[] antecedent, 
    					short[] consequent, double supportForAntecedent,
					 double supportForConsequent, double supportForRule, 
			                                 double confidenceForRule) {
	
        // Test rule using Chi-Squared testing
        if (!testRuleUsingChiSquaredTesting(supportForAntecedent,
		        supportForConsequent,supportForRule,numRows)) return;
	
		// Create new node
		RuleNodeCMAR newNode = new RuleNodeCMAR(antecedent,consequent,
				       supportForRule,supportForAntecedent,
				       supportForConsequent,confidenceForRule);
	  
		// Empty rule list situation
		if (startCMARrulelist == null) {
	    	startCMARrulelist = newNode;
	    	return;
	    	}
	
		// Check if more general rule with higher ranking exists. 
		if (moreGeneralRuleExists(newNode)) return;
		
		// Add new node to start	
		if (ruleIsCMARgreater(newNode,startCMARrulelist)) {
	    	newNode.next = startCMARrulelist;
	    	startCMARrulelist  = newNode;
	    	return;
	    	}
	
		// Add new node to middle
		RuleNodeCMAR markerNode = startCMARrulelist;
		RuleNodeCMAR linkRuleNode = startCMARrulelist.next;
		while (linkRuleNode != null) {
	    	if (ruleIsCMARgreater(newNode,linkRuleNode)) {
	        	markerNode.next = newNode;
				newNode.next    = linkRuleNode;
				return;
				}
	    	markerNode = linkRuleNode;
	    	linkRuleNode = linkRuleNode.next;	    
			}  
	
		// Add new node to end
		markerNode.next = newNode;
		}
	
    /* MORE GENERAL EXISTS */
    
    /** Tests whether a more general rule, with higher ranking, already exists 
    in the rule list.
    @param rule the rule under consideration.
    @return true if more general rule with higher ranking exists, and false
    otherwise. */
    
    private boolean moreGeneralRuleExists(RuleNodeCMAR rule) {
        RuleNodeCMAR linkRef = startCMARrulelist;
	
	// Loop through list
	while (linkRef!=null) {
	    if (ruleIsMoreGeneral(rule,linkRef) && 
	    			ruleIsCMARgreater2(rule,linkRef)) return(true);
	    linkRef=linkRef.next;
	    }
	
	// Default return
	return(false);
	}
    	
    /* RULE IS MORE GENERAL */
    
    /** Compares two rules and returns true if the first is a more general rule
    than the second (has fewer antecedent attributes).
    @param rule1 the given rule to be compared to the second.
    @param rule2 the rule which the given rule1 is to be compared to.
    @return true id rule1 is greater then rule2, and false otherwise. */
    
    private boolean ruleIsMoreGeneral(RuleNodeCMAR rule1, RuleNodeCMAR rule2) {
        if (rule1.antecedent.length < rule2.antecedent.length) return(true);
	
	// Otherwise return false   
	return(false);
	} 
	
    /* RULE IS CMAR GREATER */
    
    /** Compares two rules and returns true if the first is "CMAR greater" (has 
    a higher ranking) than the second. <P> CMAR ordering (same as CBA) is as 
    follows:
    <OL>
    <LI>Confidence, a rule <TT>r1</TT> has priority over a rule <TT>r2</TT> if 
    <TT>confidence(r1) &gt; confidence(r2)</TT>. 
    <LI>Support, a rule <TT>r1</TT> has priority over a rule <TT>r2</TT> if 
    <TT>confidence(r1)==confidence(r2) &amp;&amp; support(r1)&gt;support(r2)
    </TT>. 
    <LI>Size of antecedent, a rule <TT>r1</TT> has priority over a rule 
    <TT>r2</TT> if <TT>confidence(r1)==confidence(r2) &amp;&amp;
    support(r1)==spoort(r2) &amp;&amp;|A<SUB>r1</SUB>|&lt;|A<SUB>r2</SUB>|
    </TT>. 
    </OL>
    @param rule1 the given rule to be compared to the second.
    @param rule2 the rule which the given rule1 is to be compared to.
    @return true id rule1 is greater then rule2, and false otherwise. */
    
    private boolean ruleIsCMARgreater(RuleNodeCMAR rule1, RuleNodeCMAR rule2) {
        // Compare confidences
	if (rule1.confidenceForRule > rule2.confidenceForRule) return(true);
	
	// If confidences are the same compare support values
        if (similar2dec(rule1.confidenceForRule,rule2.confidenceForRule)) {
	   if (rule1.supportForRule > rule2.supportForRule) return(true);
	   // If confidences and supports are the same compare antecedents
	   if (similar2dec(rule1.supportForRule,rule2.supportForRule)) {
	       if (rule1.antecedent.length < rule2.antecedent.length) 
	       							return(true);
	       }
	   }
	
	// Otherwise return false   
	return(false);
	} 
	
    /* RULE IS CMAR GREATER 2 */
    	
    /** Compares two rules, such that the first id more general than the second,
    and returns true if the first is "CMAR greater" (has a higher ranking) than 
    the second. <P> Method similar to ruleIsCMARgreater method but with the
    "more general rule" prerequisite. CMAR ordering (founded on confidence and 
    support only) is as follows:
    <OL>
    <LI>Confidence, a rule <TT>r1</TT> has priority over a rule <TT>r2</TT> if 
    <TT>confidence(r1) &gt; confidence(r2)</TT>. 
    <LI>Support, a rule <TT>r1</TT> has priority over a rule <TT>r2</TT> if 
    <TT>confidence(r1)==confidence(r2) &amp;&amp; support(r1)&gt;support(r2)
    </TT>. 
    <
    </OL>
    @param rule1 the given rule to be compared to the second.
    @param rule2 the rule which the given rule1 is to be compared to.
    @return true id rule1 is greater then rule2, and false otherwise. */
    
    private boolean ruleIsCMARgreater2(RuleNodeCMAR rule1, RuleNodeCMAR rule2) {
        // Compare confidences
	if (rule1.confidenceForRule > rule2.confidenceForRule) return(true);
	
	// If confidences are the same compare support values
        if (similar2dec(rule1.confidenceForRule,rule2.confidenceForRule)) {
	   if (rule1.supportForRule > rule2.supportForRule) return(true);
	   }
	
	// Otherwise return false   
	return(false);
	}
	
    /* -------------------------------------------- */
    /*                                              */
    /*              CHI SQUARED TESTING             */
    /*                                              */
    /* -------------------------------------------- */
	    
    /* TEST RULE USING CHI SQUARED TESTING */
    
    /** Tests a classification rule with the given parameters to determine 
    the interestingness/surprisingness of the rule.
    @param supA the support value for the antecedent of the rule. 
    @param supC the support value for the consequent of the rule. 
    @param supAC the support for the rule. 
    @param numR the number of records in the input (training) sets. 
    @return true if Chi squared value is above critical threshold and false
    otherwise.  */
    
    public boolean testRuleUsingChiSquaredTesting(double supA, double supC, 
    					double supAC, double numR) {
	// Calculate Chi squared value
	double chiSquaredValue = getChiSquaredValue(supA,supC,supAC,numR);
				
	// Test Chi Squared value.
	if (chiSquaredValue>threshold) return(true);
	else return(false);
	}
	
    /* GET CHI-SQUARED VALUE */
        
    /** Calculates and returns the Chi-Squared value for a rule. 
    @param supA the support value for the antecedent of the rule. 
    @param supC the support value for the consequent of the rule. 
    @param supAC the support for the rule. 
    @param numR the number of records in the input (training) sets. 
    @return the Chi squared value.  */
    
    private double getChiSquaredValue(double supA, double supC, 
    						double supAC, double numR) {
	// Set values
	supAntecedent = supA;
    	supConsequent = supC;
    	supRule       = supAC;	
    	numRecords    = numR;
    	
	// Calculate observed and expected values
	calculateObsValues();
	calculateExpValues();
	
	// Calculate and return Chi squared value
	return(calcChiSquaredValue());
	}
				
    /* CALCULATE OBSERVED VALUES */
    
    /** Calculates observed values for Chi squared testing calculation. */
     
    private void calculateObsValues() {
        obsValues[0]=supRule;
	obsValues[1]=supAntecedent-supRule;
	obsValues[2]=supConsequent-supRule;
	obsValues[3]=numRecords-supAntecedent-supConsequent+supRule;
	 
	// Calculate additional support values
	supNotAntecedent=numRecords-supAntecedent;
    	supNotConsequent=numRecords-supConsequent;
	}
	
    /* CALIULASTE EXPECTED VALUES */
    
    /** Calculates expected values for Chi squared testing calculation. */
     
    private void calculateExpValues() {
         expValues[0]=(supConsequent*supAntecedent)/numRecords;
	 expValues[1]=(supNotConsequent*supAntecedent)/numRecords;
	 expValues[2]=(supConsequent*supNotAntecedent)/numRecords;
	 expValues[3]=(supNotConsequent*supNotAntecedent)/numRecords;
	 }
	
    /* CALCULATE CHI SQUARED VALUE */
    
    /** Calculates the Chi squared values and returns their sum.
    @return the sum of the Chi Squared values. */ 
    
    private double calcChiSquaredValue() {
        double sumChiSquaredValues = 0.0;
	
	for (int index=0;index<obsValues.length;index++) {
	    double chiValue = Math.pow((obsValues[index]-
	    			    expValues[index]),2.0)/expValues[index];
	    sumChiSquaredValues = sumChiSquaredValues+chiValue;
	    }
	
	// Return
	return(sumChiSquaredValues);
	}
		
    /* ------------------------------------------------------------- */
    /*                                                               */
    /*                     RULE PRUNING (CMAR)                       */
    /*                                                               */
    /* ------------------------------------------------------------- */
    
    /* PRUNE USING COVER */
    
    /** Prunes the current CMAR list of rules according to the "cover" 
    principle.
    @param trainingSet the input data set.	*/
    
    protected void pruneUsingCover(short[][] trainingSet) {
        // Initialise cover array
	int[] cover = new int[trainingSet.length];
	
	// Define rule list references
	RuleNodeCMAR newStartRef = null;
	RuleNodeCMAR markerRef   = null;
	RuleNodeCMAR linkRef     = startCMARrulelist;

	// Loop through rule list
	while (linkRef!=null) {
	    // If no more training records end
	    if (emptyDataSet(trainingSet)) break;
	    // Set cover flag to false, will be set to true of a rule matches 
	    // a record.
	    boolean coverFlag=false;
	    // Loop through training ser
	    for (int index=0;index<trainingSet.length;index++) {
	   	// If record satisfies a rule increment cover element for 
		// record and set cover flag to true to indicate that rule
		// is required by at least one record
		if (isSubset(linkRef.antecedent,trainingSet[index])) {
		   cover[index]++;
		   coverFlag=true; 
		   }
		}
	    // If current rule is required by at least one record (cover flag
	    // set to true) add to new rule list
	    if (coverFlag) {
	    	if (newStartRef==null) newStartRef=linkRef;
		else markerRef.next=linkRef;
		markerRef=linkRef;
		linkRef=linkRef.next;
		markerRef.next=null;
		}	   
	    else linkRef=linkRef.next;
	    // Remove records from training set if adequately covered
	    for (int index=0;index<cover.length;index++) {
	        if (cover[index]>MIN_COVER) trainingSet[index]=null;
	        }
	    }
	
	// Set rule list 
	startCMARrulelist = newStartRef;
	}
    
    /* EMPTY DATA SET */
    
    /** Tests whether a data set is empty or not (all records set to null).
    @param dataSet the input data set.
    @return true if empty, false othjerwise. */
    
    private boolean emptyDataSet(short[][] dataSet) {
        // Loop through given data set
        for (int index=0;index<dataSet.length;index++) 
	    			   if (dataSet[index]!=null) return(false);
	
	// Default
	return(true);
	}	
	
    /* ---------------------------------------------------------------- */
    /*                                                                  */
    /*                        TEST CLASSIFICATION                       */
    /*                                                                  */
    /* ---------------------------------------------------------------- */

    /* TEST CLASSIFICATION */
    /** Tests the generated classification rules using test sets and return
    percentage accuracy. Overrides method in AprioriTFPclass class. */

    protected void testClassification() {	
    	// Check if test data and classifier exists, if not return '0'.
		if (!canTestingBeUndertaken()) {
			accuracy=0.0;
			aucValue=0.0;
			return;
	    	}
	    	
	    // Dimension signal (truth) and response tables. Singal table is the "ground
	    // truth", response table is what is produced by the generated classifier.
		int signalTable[][]   = new int[testDataArray.length][numClasses];
		int responseTable[][] = new int[testDataArray.length][numClasses];
		for (int i=0;i<signalTable.length;i++) {
			for (int j=0;j<signalTable[i].length;j++) {
				signalTable[i][j]=0;
				}
			}
		for (int i=0;i<responseTable.length;i++) {
			for (int j=0;j<responseTable[i].length;j++) {
				responseTable[i][j]=0;
				}
			}
		
		// Set Counters	and varaiables
		int correctClassCounter = 0;
		int wrongClassCounter   = 0;	
		int unclassifiedCounter = 0;
		int offset = numOneItemSets-numClasses+1;


		
	    
        // Commernce testing. Loop through test set
        int index=0;
    	for(;index<testDataArray.length;index++) {
//System.out.println("index = " + index);
            // Note: classifyRecord methods are contained in the 
            // AssocRuleMining class. To calssify without default use 
            // classifyRecord, with defualt use classifyRecordDefault.
            short classResult = classifyRecordWCS(testDataArray[index]);
            if (classResult==0) unclassifiedCounter++;
	    	else {
	        	// Get actual class and add to signal table
	        	short classActual = getLastElement(testDataArray[index]);
	        	int classIndex = classActual-offset;
	        	signalTable[index][classIndex]=1;
	    		// Add to response table.
            	classIndex = classResult-offset;
	        	responseTable[index][classIndex]=1;
	        	// Update counters
				if (classResult == classActual) correctClassCounter++;
	        	else wrongClassCounter++;
				}
	    	}
        
		// Calculate accuracy
		accuracy = ((double) correctClassCounter*100.0/(double) index);

    	// CalculateAUC value.
    	double valueAtotal = 0.0;
    	for (int i=0;i<numClasses-1;i++) {
			for (int j=i+1;j<numClasses;j++) {
				double mww_ij = calcMWWstatValue(i,j,responseTable,signalTable);
				double mww_ji = calcMWWstatValue(j,i,responseTable,signalTable);
				double valueA = (mww_ij + mww_ji)/2;
				valueAtotal = valueAtotal + valueA;
				}
			}
    	
    	// Calculate and assign AUC value
    	aucValue = (2.0/(numClasses*(numClasses-1.0))) * valueAtotal;					
		}	
	
	/** Determine if the testing of generated classifier can be undertaken, i.e. 
    is there a test set? and is there a classifier?
    private boolean true if OK, false otherwise. */
    
    private boolean canTestingBeUndertaken() {

		// Check if test data exists, if not return 'false'
		if (testDataArray==null) {
	    	String s = "WARNING: No test data\n";
	    	if (textArea==null) System.out.print(s);
            else textArea.append(s);
	    	return(false);
	    	}
	
		// Check if any classification rules have been generated, if not 
		// return 'false'
		if (startCMARrulelist==null) {
	    	String s = "No CMAR classification rules generated!\n";
	    	if (textArea==null) System.out.print(s);
            else textArea.append(s);
	    	return(false);
	    	}
    
    	// Otherwise return "true"
    	return(true);
    	}
    /* ------------------------------------------------------------- */
    /*           CLASSIFIER  (USING WEIGHTED CHI SQUARED)            */
    /* ------------------------------------------------------------- */ 	
	
    /* CLASSIFY RECORD (USING WEIGHTED CHI SQUARED) */
    /** Selects the best rule in a rule list according to the Weighted Chi-
    Squared (WCS) Value. <P> Proceed as follows: <OL>
    <LI>Collect rules that satisfy the record. if
    <OL type="i">
    <LI>If consequents of all rules are all identical, or
    only one rule, classify record.
    <LI>Else group rules according to classifier and determine the combined
    effect of the rules in each group, the classifier associated with
    the "strongest group" is then selected.
    </OL>
    @param itemSet the record to be classified.	
    @return the class label (or 0 if no class found).	*/

    protected short classifyRecordWCS(short[] itemSet) {
	RuleNodeCMAR linkRef = startCMARrulelist;
	// We are going to use the startCMARrulelist reference to store a linked
	// list of rules whose antecedent statisfies the given itemset
	RuleNodeCMAR tempRulelist = startCMARrulelist;	
	startCMARrulelist=null;
	
	// Obtain rules that satisfy record (iremSet)
       obtainallRulesForRecord(linkRef,itemSet);

        // If no rules, reset global rule list reference and return 0
	if (startCMARrulelist==null) {
	    startCMARrulelist = tempRulelist;
	    return(0);
	    }
	
	// If only one rule, reset global rule list reference and return class
	if (startCMARrulelist.next== null) {
	    short answer = startCMARrulelist.consequent[0];
	    startCMARrulelist=tempRulelist;
	    return(answer);
	    }
	    
	// If more than one rule but all have the same class, reset global rule 
	// list reference and  return class
	if (onlyOneClass()) {
	    short answer = startCMARrulelist.consequent[0];
	    startCMARrulelist=tempRulelist;
	    return(answer);
	    }
	
	// Otherwise group rules
	RuleNodeCMAR[] ruleGroups = groupRules();
	// Determine Weighted Chi-Squared (WCS) Values for each group
	double[] wcsValues = calcWCSvalues(ruleGroups);
	// Select group with best WCS value and return associated label
	short consequent = selectBestWCS(wcsValues);
	// Reset global rule list reference
	startCMARrulelist=tempRulelist;
	// Return class
	return(consequent);
	} 
    
    /* GROUP RULES */
    
    /** Groups rules contained in a linked list of rules pointed at by 
    <TT>startCMARrulelist</TT> according to their consequent.    
    @return an array of rule groups. */
    
    private RuleNodeCMAR[] groupRules() {
        // Initialise rule groups data structure
	RuleNodeCMAR[] ruleGroups = new RuleNodeCMAR[numClasses];
	for (int index=0;index<ruleGroups.length;index++)
							ruleGroups[index]=null;	
	// Loop through rule list
	RuleNodeCMAR linkRef = startCMARrulelist;
	while (linkRef!=null) {
	   // Identify index for consequent
	   int index = numOneItemSets-linkRef.consequent[0];
           // Add to rule group
	   RuleNodeCMAR ruleCopy = new RuleNodeCMAR(linkRef.antecedent,
	   			     linkRef.consequent,linkRef.supportForRule,
				 linkRef.suppAntecedent,linkRef.suppConsequent,
						    linkRef.confidenceForRule);
	   ruleCopy.next=ruleGroups[index];
	   ruleGroups[index]=ruleCopy;
	   // Increment link reference
	   linkRef=linkRef.next;
	   }
	
	// Return
	return(ruleGroups);
	}	
	
   /* ONLY ONE CLASS */
   
   /** Checks whether given rule list consequents all refer to the same class 
   or not.
   @return true if identical consequent in all rules, false otherwise. */
   
   private boolean onlyOneClass() {
       RuleNodeCMAR linkRef = startCMARrulelist;
       
       // Class for first rule.
       short firstClass = linkRef.consequent[0];
      
       // loop through rest of list.
       linkRef = linkRef.next;
       while (linkRef!=null) {  
           if (linkRef.consequent[0]!=firstClass) return(false);
	   linkRef=linkRef.next;
	   }
	   
       // Default return
       return(true);
       }
	
    /* CALCULATE WEIGHTED CHI SQUARED VALUE FOR RULE GROUPS */
    
    /** Determines and returns the weighted Chi Squared values for the groups of
    rules.
    @param ruleGroups the given groups of rule.
    @return array of weighted Chi-Squared value for a set of rule groups */
    
    private double[] calcWCSvalues(RuleNodeCMAR[] ruleGroups) {
        // Dimension array
	double[] wcsArray = new double[ruleGroups.length];
	
	for (int index=0;index<ruleGroups.length;index++) {
	    RuleNodeCMAR linkRuleNode = ruleGroups[index];
	    double wcsValue = 0.0;
	    while (linkRuleNode != null) {
		double chiSquaredValue = 
				 getChiSquaredValue(linkRuleNode.suppAntecedent,
					linkRuleNode.suppConsequent,
					linkRuleNode.supportForRule,numRecords);
		double chiSquaredUB = 
			   calcChiSquaredUpperBound(linkRuleNode.suppAntecedent,
						   linkRuleNode.suppConsequent);
		wcsValue = 
		      wcsValue + (chiSquaredValue*chiSquaredValue)/chiSquaredUB;
	
		linkRuleNode = linkRuleNode.next;
		}
	    wcsArray[index]=wcsValue;
	    }
	
	// Return
	return(wcsArray);
	}
    
    /* BEST WCS VALUE */
    
    /** Determines the best of the given WCS values and returns the consequent
    associated with this bet value.
    @param wcsArray the given array of weighted Chi-Squared value for a set 
    of rule groups. 
    @return the selected consequent. */
    
    private short selectBestWCS(double[] wcsValues) {
        double bestValue = wcsValues[0];
	int bestIndex    = 0;
	
	for (int index=1;index<wcsValues.length;index++) {
	    if (wcsValues[index]>bestValue) {
	        bestValue=wcsValues[index];
		bestIndex=index;
		}
	    }
	
	// Return
	return((short) (numOneItemSets-bestIndex));
	}
	
    /* CALCULATE CHI SQUARED VALUE UPPER BOUND */
    
    /** Claculates the upper bound for the Chi-Squared value of a rule. 
    @param suppAnte the support for the antecedent of a rule.
    @param suppCons the support for the consequent of a rule.
    @return the Chi-Squared upper bound. */
    
    private double calcChiSquaredUpperBound(double suppAnte, double suppCons) {
        double term;
	
	// Test support for antecedent and confidence and choose minimum
	if (suppAnte<suppCons) term = 
		      Math.pow(suppAnte-((suppAnte*suppCons)/numRecords),2.0);
		     
	else term = Math.pow(suppCons-((suppAnte*suppCons)/numRecords),2.0);
		      
	// Determine e
	double eVlaue = calcWCSeValue(suppAnte,suppCons);
	
	// Rerturn upper bound
	return(term*eVlaue*numRecords);
	}
	
    /* CALCULATE WCS e VALUE. */
    
    /** Calculates and returns the e value for calculating Weighted Chi-Squared
    (WCS) values.
    @param suppAnte the support for the antecedent of a rule.
    @param suppCons the support for the consequent of a rule.
    @return the ECS e value. */
    
    private double calcWCSeValue(double suppAnte, double suppCons) {
        double term1 = 1/(suppAnte*suppCons);
	double term2 = 1/(suppAnte*(numRecords-suppCons));
        double term3 = 1/(suppCons*(numRecords-suppAnte));
	double term4 = 1/((numRecords-suppAnte)*(numRecords-suppCons));
	
	// Return sum
	return(term1+term2+term3+term4); 
	}
	    
    /* ------------------------------------------------------------- */
    /*                                                               */
    /*                  CLASSIFIER  UTILITY METHODS                  */
    /*                                                               */
    /* ------------------------------------------------------------- */ 
	
    /** Places all rules that satisfy the given record in a CMAR rule linked 
    list pointed at by startCMARrulelist field, in the order that rules are 
    presented. <P> Used in Weighted Chi-Squared classification (CMAR) algorithm.
    @param linkref The reference to the start of the existing list of rules.
    @param itemset the record to be classified.	*/

    private void obtainallRulesForRecord(RuleNodeCMAR linkRef, short[] itemSet) {
	RuleNodeCMAR newStartRef = null;
	RuleNodeCMAR markerRef   = null;
	
	// Loop through linked list of existing rules
	while (linkRef!=null) {
	    // If rule satisfies record add to new rule list
	    if (isSubset(linkRef.antecedent,itemSet)) {
	        RuleNodeCMAR newNode = new RuleNodeCMAR(linkRef.antecedent,
				linkRef.consequent,linkRef.supportForRule,
				linkRef.suppAntecedent,linkRef.suppConsequent,
				linkRef.confidenceForRule);
	   	if (newStartRef==null) newStartRef=newNode;
		else markerRef.next=newNode;
		markerRef=newNode; 
		/*if (newStartRef==null) newStartRef=linkRef;
		else markerRef.next=linkRef;
		markerRef=linkRef; */
		}
	    linkRef=linkRef.next;
	    }
	
	// Set rule list 
	startCMARrulelist = newStartRef;
	}

    /* ----------------------------------- */
    /*                                     */
    /*              GET METHODS            */
    /*                                     */
    /* ----------------------------------- */
    
    /* GET NUMBER OF CMAR CLASSIFICATION RULES */

    /**  Returns the number of generated CMAR classification rules.
    @return the number of CRs. */

    public int getNumCMAR_CRs() {
        int number = 0;
        RuleNodeCMAR linkRuleNode = startCMARrulelist;
	
	// Loop through linked list
	while (linkRuleNode != null) {
	    number++;
	    linkRuleNode = linkRuleNode.next;
	    }
	
	// Return
	return(number);
	}
		
    /* ----------------------------------- */
    /*                                     */
    /*              SET METHODS            */
    /*                                     */
    /* ----------------------------------- */
    
    /* SET CHI SQUARED TESTING PARAMETERS */
    
    /** Sets the parameters for Chi-Squared testing. <P> By default 5% is
    assumed.
    @param percentage the user supplied percentage "level of significance". */
    
    public void setChiSquaredTestingParams(double percentage) {
        int percent = (int) (percentage*100);
	
        switch (percent) {
	    case 1000: threshold=THRESHOLD_10;    break;
	    case 500:  threshold=THRESHOLD_5;     break;
	    case 250:  threshold=THRESHOLD_2HALF; break;
	    case 100:  threshold=THRESHOLD_1;     break;
	    case 50:  threshold=THRESHOLD_HALF;   break;
	    default:
	        System.out.println("No hard coded Chi squared critical " +
		"threshold value for " + percentage + "%, selected value " +
		"for 5% (default)");     
	   }  
	}
	
    /* ------------------------------ */
    /*                                */
    /*              OUTPUT            */
    /*                                */
    /* ------------------------------ */
    
    /* OUTPUT CMAR RULE LINKED LIST */
    /** Outputs contents of CMAR rule linked list (if any) */

    public void outputCMARrules() {
        outputRules(startCMARrulelist);
        }


    /** Outputs given CMAR rule list.
    @param ruleList the given rule list. */

    public void outputRules(RuleNodeCMAR ruleList) {

        // Check for empty rule list
        if (ruleList==null) {
	        String s = "No rules generated!\n";
	        if (textArea==null) System.out.println(s);
	        else textArea.append(s);
	        return;
	        }
	        
	    // Heading
        String s = "(#) Ante -> Cons confidence % (Sup. Rule, Sup. Ante, " +
                "Sup. Cons.)\n---------------------------------------------\n";
	    if (textArea==null) System.out.println(s);
	    else textArea.append(s);
	        
        // Loop through rule list
        int number = 1;
        RuleNodeCMAR linkRuleNode = ruleList;
        while (linkRuleNode != null) {
            if (textArea==null) outputRule(number,linkRuleNode);
            else outputRule(textArea,number,linkRuleNode);
            number++;
            linkRuleNode = linkRuleNode.next;
            }
	    }

    /** Outputs a CMAR rule.
    @param number the rule number.
    @param rule the rule to be output. */
    
    private void outputRule(int number, RuleNodeCMAR rule) {
        System.out.print("(" + number + ") ");
        
        // Antecedent
        if (ruleListSchemaOutputFlag) outputItemSetSchema(rule.antecedent);
        else outputItemSet(rule.antecedent);

        // Operator
        System.out.print(" -> ");

        // Consequent
        if (ruleListSchemaOutputFlag) outputItemSetSchema(rule.consequent);
        else outputItemSet(rule.consequent);

        // Details
        System.out.println(" " + twoDecPlaces(rule.confidenceForRule) +
                                           "%, (" + rule.supportForRule + ", " +
		        rule.suppAntecedent + ", " + rule.suppConsequent + ")");
	}

    /** Outputs a CMAR rule (GUI version).
    @param number the rule number.
    @param rule the rule to be output. */

    private void outputRule(JTextArea textArea, int number, RuleNodeCMAR rule) {
        textArea.append("(" + number + ") ");

        // Antecedent
        if (ruleListSchemaOutputFlag)
                            outputItemSetSchema(textArea,rule.antecedent);
        else outputItemSet(textArea,rule.antecedent);

        // Operator
        textArea.append(" -> ");

        // Consequent
        if (ruleListSchemaOutputFlag)
                            outputItemSetSchema(textArea,rule.consequent);
        else outputItemSet(textArea,rule.consequent);

        // Details
        String s = " " + twoDecPlaces(rule.confidenceForRule) +
                                           "%, (" + rule.supportForRule + ", " +
                       rule.suppAntecedent + ", " + rule.suppConsequent + ")\n";
        textArea.append(s);
        }

    /* OUTPUT NUMBER OF CMAR RULES */

    /** Outputs number of generated rules (ARs or CARS). */

    public void outputNumCMARrules() {
        System.out.println("Number of CMAR rules    = " + getNumCMAR_CRs());
        }

    /* ----------------------------------------------------- */
    /*                                                       */
    /*        OUTPUT UTILITIES (DIAGNOSTIC USE ONLY)         */
    /*                                                       */
    /* ----------------------------------------------------- */

    /* OUTPUT CHI SQUARED TESTING VALUES */

    /** Outputs values in Observed or Expected Value array.
    @param values the array to be output. */

    private void outputChiSquaredTestingValues(double[] values) {
        System.out.println(twoDecPlaces(values[0]) + "\t| " +
                           twoDecPlaces(values[1]) + "\t| " +
                          twoDecPlaces(values[0]+values[1]));
        System.out.println(twoDecPlaces(values[2]) + "\t| " +
                           twoDecPlaces(values[3]) + "\t| " +
                          twoDecPlaces(values[2]+values[3]));
        System.out.println("----------------------------------------------");
        System.out.println(twoDecPlaces(values[0]+values[2]) + "\t| " +
                           twoDecPlaces(values[1]+values[3]) + "\t| " +
                twoDecPlaces(values[0]+values[1]+values[2]+values[3]));
        System.out.println("\n");
        }

    /* OUTPUT CHI SQUARED VALUE CALCULATION */

    /** Output chi value calculation process for diagnostic purposes. */

    private void outputChiSquaredValueCalc() {
        double sumTotal=0;

        // Start output
        System.out.println("O\t E\t (O-E)\t (O-E)^2\t ((O-E)^2)/2");

        // Loop
        for (int index=0;index<obsValues.length;index++) {
            double difference = obsValues[index]-expValues[index];
            System.out.print(twoDecPlaces(obsValues[index]) + "\t| " +
                             twoDecPlaces(expValues[index]) + "\t| " +
                                   twoDecPlaces(difference) + "\t| " +
                     twoDecPlaces(Math.pow(difference,2.0)) + "\t| ");
            double total = Math.pow(difference,2.0)/expValues[index];
            System.out.println(twoDecPlaces(total));
            sumTotal=sumTotal+total;
            }

        // End
        System.out.println("\n");
        }

    /* OUTPUT GROUPS */

    /** Outputs rule groups produced using CMAR (used for diagnostic purposes
    only).
    @param ruleGroups the array of rule groups to be output. */

    private void outputGroups(RuleNodeCMAR[] ruleGroups) {
        for (int index=0;index<ruleGroups.length;index++) {
            System.out.println("Group " + index);
            outputRules(ruleGroups[index]);
            System.out.println();
            }
        }
    }
    
