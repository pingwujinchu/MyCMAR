/* -------------------------------------------------------------------------- */
/*                                                                            */
/*                APRIORI-TFP CMAR (CLASSIFICATION BASED ON                   */
/*         MULTIPLE ASSOCIATION RULES) CLASSIFIER ONLY APPLICATION            */
/*                                                                            */
/*                             Frans Coenen                                   */
/*                                                                            */
/*                           Friday 5 March 2004                              */
/*                                                                            */
/*                      Department of Computer Science                        */
/*                        The University of Liverpool                         */
/*                                                                            */
/* -------------------------------------------------------------------------- */

import java.io.*;

/* Classification application the CMAR (Classification based on Multiple 
Associate Rules) algorithm proposed by Wenmin Li, Jiawei Han and Jian Pei,
but founded on Apriori-TFP. Build only a classifier does not test accuracy.

Compile using:

javac ClassOnlyCMAR_App.java

Run using java, Example:

java ClassOnlyCMAR_App -FpimaIndians.D42.N768.C2.num -N2 -S1 -C50

(-F filename, -N number of classifiers) would produce a classifier of the
form:

(1)  {1 4 5 7}  ->  {41}  91.48%, (172.0, 188.0, 500.0)
(2)  {4 5 7}  ->  {41}  90.64%, (184.0, 203.0, 500.0)
(3)  {15}  ->  {41}  90.64%, (155.0, 171.0, 500.0)
(4)  {1 5 7}  ->  {41}  89.9%, (187.0, 208.0, 500.0)
(5)  {2 4 7}  ->  {41}  89.74%, (175.0, 195.0, 500.0)
(6)  {1 2 4 7}  ->  {41}  89.18%, (165.0, 185.0, 500.0)
(7)  {5 7}  ->  {41}  88.88%, (200.0, 225.0, 500.0)
(8)  {1 4 7}  ->  {41}  87.9%, (218.0, 248.0, 500.0)
(9)  {4 7}  ->  {41}  87.5%, (231.0, 264.0, 500.0)
(10)  {1 6 7}  ->  {41}  87.3%, (165.0, 189.0, 500.0)
(11)  {6 7}  ->  {41}  86.89%, (179.0, 206.0, 500.0)
(12)  {1 2 7}  ->  {41}  85.59%, (208.0, 243.0, 500.0)
(13)  {1 4 5 6}  ->  {41}  85.55%, (154.0, 180.0, 500.0)
(14)  {2 7}  ->  {41}  85.38%, (222.0, 260.0, 500.0)
(15)  {1 2 4 5}  ->  {41}  83.88%, (177.0, 211.0, 500.0)
(16)  {1 7}  ->  {41}  83.18%, (282.0, 339.0, 500.0)
(17)  {1 5 6}  ->  {41}  83.16%, (163.0, 196.0, 500.0)
(18)  {1 4 6}  ->  {41}  83.11%, (187.0, 225.0, 500.0)
(19)  {2 4 5}  ->  {41}  82.71%, (201.0, 243.0, 500.0)
(20)  {4 5 6}  ->  {41}  82.6%, (171.0, 207.0, 500.0)
(21)  {2 4 6}  ->  {41}  82.44%, (155.0, 188.0, 500.0)
(22)  {7}  ->  {41}  81.74%, (300.0, 367.0, 500.0)
(23)  {1 4 5}  ->  {41}  81.29%, (239.0, 294.0, 500.0)
(24)  {1 2 4}  ->  {41}  80.91%, (229.0, 283.0, 500.0)
(25)  {4 6}  ->  {41}  80.62%, (208.0, 258.0, 500.0)

Percentage value is the confidence. Values in brackets are: support for
rule, support for antecdent (same as that for rule if confidence is to be
100%) and support for consequent.	*/

public class ClassOnlyCMAR_App {

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
		// and set number of rows in training set
		newClassification.inputDataSet();	
		newClassification.setNumRowsInTrainingSet
	                                (newClassification.getNumberOfRows());
	
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
	
		// Mine data, produce T-tree and generate CRs
		newClassification.startClassification();
		double accuracy = newClassification.getAccuracy();
		newClassification.outputDuration(time1,
				(double) System.currentTimeMillis());
	
		// Output
		//newClassification.outputFrequentSets();
		newClassification.outputNumFreqSets();
		newClassification.outputNumUpdates();
		newClassification.outputStorage();
		//newClassification.outputTtree();
		System.out.println("Accuracy = " + accuracy);
		newClassification.outputNumCMARrules();
		newClassification.outputCMARrules();
	
		// End
		System.exit(0);
		}
    }
