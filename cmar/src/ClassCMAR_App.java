/* -------------------------------------------------------------------------- */
/*                                                                            */
/*                APRIORI-TFP CMAR (CLASSIFICATION BASED ON                   */
/*                 MULTIPLE ASSOCIATION RULES) APPLICATION                    */
/*                                                                            */
/*                             Frans Coenen                                   */
/*                                                                            */
/*                           Friday 5 March 2004                              */
/*                          Updated: 6 March 2012                             */
/*                                                                            */
/*                      Department of Computer Science                        */
/*                        The University of Liverpool                         */
/*                                                                            */
/* -------------------------------------------------------------------------- */

import java.io.*;

/* Classification application the CMAR (Classification based on Multiple 
Associate Rules) algorithm proposed by Wenmin Li, Jiawei Han and Jian Pei,
but founded on Apriori-TFP.

Compile using:

javac ClassCMAR_App.java

Run java, Example:

java ClassCMAR_App -FpimaIndians.D42.N768.C2.num -N2 -S5 -C80

(-F filename, -T test set filename, -N number of classes, -S minimum 
support threshold, -C minimum confidence threshold).

Note accuracy of classification is entirely dependent on the user
supplied support and confidence thresholds. */

public class ClassCMAR_App {

    // ------------------- FIELDS ------------------------

    // None

    // ---------------- CONSTRUCTORS ---------------------

    // None

    // ------------------ METHODS ------------------------

    public static void main(String[] args) throws IOException {
		double time1 = (double) System.currentTimeMillis();
	
		// Create instance of class ClassificationPRM	
		AprioriTFP_CMAR newClassification = new AprioriTFP_CMAR(args);
				
		// Read data to be mined from file (method in AssocRuleMining class)
		newClassification.inputDataSet();
	
		// Reorder input data according to frequency of single attributes
		// excluding classifiers. Proceed as follows: (1) create a conversion
		// array (with classifiers left at end), (2) reorder the attributes 
		// according to this array. Do not throw away unsupported attributes 
		// as when data set is split (if distribution is not exactly even) we 
		// may have thrown away supported attributes that contribute to the 
		// generation of CRs. NB Never throw away classifiers even if
		// unsupported!
		newClassification.idInputDataOrdering();  // ClassificationAprioriT
		newClassification.recastInputData();      // AssocRuleMining
	
		// Create training data set (method in ClassificationAprioriT class)
		// assuming a 50:50 split
        newClassification.createTrainingAndTestDataSets();
	
		// Mine data, produce T-tree and generate CRs
		newClassification.startClassification();
		newClassification.outputDuration(time1,
				(double) System.currentTimeMillis());
	 
	 	// Standard output
		newClassification.outputNumFreqSets();
		newClassification.outputNumUpdates();
		newClassification.outputStorage();
		newClassification.outputNumCMARrules();
		double accuracy = newClassification.getAccuracy();
		System.out.println("Accuracy = " + twoDecPlaces(accuracy));
		double aucValue = newClassification.getAUCvalue();
		System.out.println("AUC value = " + fourDecPlaces(aucValue));
		
		// Additional output
		//newClassification.outputTtree();
		newClassification.outputCMARrules();
	
		// End
		System.exit(0);
		}
		
    /* -------------------------------------------------------------- */
    /*                                                                */
    /*                    OUTPUT METHODS                              */
    /*                                                                */
    /* -------------------------------------------------------------- */
	
    /* TWO DECIMAL PLACES */
    
    /** Converts given real number to real number rounded up to two decimal 
    places. 
    @param number the given number.
    @return the number to two decimal places. */ 
    
    protected static double twoDecPlaces(double number) {
    	int numInt = (int) ((number+0.005)*100.0);
		number = ((double) numInt)/100.0;
		return(number);
		}/* FOUR DECIMAL PLACES */

    /** Converts given real number to real number rounded up to four decimal
    places.
    @param number the given number.
    @return the number to gour decimal places. */

    protected static double fourDecPlaces(double number) {
    	int numInt = (int) ((number+0.00005)*10000.0);
		number = ((double) numInt)/10000.0;
		return(number);
		}	
    }
