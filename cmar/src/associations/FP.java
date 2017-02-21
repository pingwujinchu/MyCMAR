/*
 *    This program is free software; you can redistribute it and/or modify
 *    it under the terms of the GNU General Public License as published by
 *    the Free Software Foundation; either version 2 of the License, or
 *    (at your option) any later version.
 *
 *    This program is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *    GNU General Public License for more details.
 *
 *    You should have received a copy of the GNU General Public License
 *    alo0ng with this program; if not, write to the Free Software
 *    Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 */

/*
 *    Apriori.java
 *    Copyright (C) 1999 Eibe Frank,Mark Hall, Stefan Mutter
 *
 */

package associations;

import weka.core.AttributeStats;
import weka.core.Capabilities;
import weka.core.FastVector;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.Option;
import weka.core.OptionHandler;
import weka.core.SelectedTag;
import weka.core.Tag;
import weka.core.TechnicalInformation;
import weka.core.TechnicalInformationHandler;
import weka.core.Utils;
import weka.core.Capabilities.Capability;
import weka.core.TechnicalInformation.Field;
import weka.core.TechnicalInformation.Type;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.Remove;

import java.util.*;

import mine.CRTree;

import java.io.*;

/**
 * <!-- globalinfo-start --> Class implementing an Apriori-type algorithm.
 * Iteratively reduces the minimum support until it finds the required number of
 * rules with the given minimum confidence.<br/>
 * The algorithm has an option to mine class association rules. It is adapted as
 * explained in the second reference.<br/>
 * <br/>
 * For more information see:<br/>
 * <br/>
 * R. Agrawal, R. Srikant: Fast Algorithms for Mining Association Rules in Large
 * Databases. In: 20th International Conference on Very Large Data Bases,
 * 478-499, 1994.<br/>
 * <br/>
 * Bing Liu, Wynne Hsu, Yiming Ma: Integrating Classification and Association
 * Rule Mining. In: Fourth International Conference on Knowledge Discovery and
 * Data Mining, 80-86, 1998.
 * <p/>
 * <!-- globalinfo-end -->
 *
 * <!-- technical-bibtex-start --> BibTeX:
 * 
 * <pre>
 * &#64;inproceedings{Agrawal1994,
 *    author = {R. Agrawal and R. Srikant},
 *    booktitle = {20th International Conference on Very Large Data Bases},
 *    pages = {478-499},
 *    publisher = {Morgan Kaufmann, Los Altos, CA},
 *    title = {Fast Algorithms for Mining Association Rules in Large Databases},
 *    year = {1994}
 * }
 * 
 * &#64;inproceedings{Liu1998,
 *    author = {Bing Liu and Wynne Hsu and Yiming Ma},
 *    booktitle = {Fourth International Conference on Knowledge Discovery and Data Mining},
 *    pages = {80-86},
 *    publisher = {AAAI Press},
 *    title = {Integrating Classification and Association Rule Mining},
 *    year = {1998}
 * }
 * </pre>
 * <p/>
 * <!-- technical-bibtex-end -->
 *
 * <!-- options-start --> Valid options are:
 * <p/>
 * 
 * <pre>
 *  -N &lt;required number of rules output&gt;
 *  The required number of rules. (default = 10)
 * </pre>
 * 
 * <pre>
 *  -T &lt;0=confidence | 1=lift | 2=leverage | 3=Conviction&gt;
 *  The metric type by which to rank rules. (default = confidence)
 * </pre>
 * 
 * <pre>
 *  -C &lt;minimum metric score of a rule&gt;
 *  The minimum confidence of a rule. (default = 0.9)
 * </pre>
 * 
 * <pre>
 *  -D &lt;delta for minimum support&gt;
 *  The delta by which the minimum support is decreased in
 *  each iteration. (default = 0.05)
 * </pre>
 * 
 * <pre>
 *  -U &lt;upper bound for minimum support&gt;
 *  Upper bound for minimum support. (default = 1.0)
 * </pre>
 * 
 * <pre>
 *  -M &lt;lower bound for minimum support&gt;
 *  The lower bound for the minimum support. (default = 0.1)
 * </pre>
 * 
 * <pre>
 *  -S &lt;significance level&gt;
 *  If used, rules are tested for significance at
 *  the given level. Slower. (default = no significance testing)
 * </pre>
 * 
 * <pre>
 *  -I
 *  If set the itemsets found are also output. (default = no)
 * </pre>
 * 
 * <pre>
 *  -R
 *  Remove columns that contain all missing values (default = no)
 * </pre>
 * 
 * <pre>
 *  -V
 *  Report progress iteratively. (default = no)
 * </pre>
 * 
 * <pre>
 *  -A
 *  If set class association rules are mined. (default = no)
 * </pre>
 * 
 * <pre>
 *  -c &lt;the class index&gt;
 *  The class index. (default = last)
 * </pre>
 * 
 * <!-- options-end -->
 *
 * @author Eibe Frank (eibe@cs.waikato.ac.nz)
 * @author Mark Hall (mhall@cs.waikato.ac.nz)
 * @author Stefan Mutter (mutter@cs.waikato.ac.nz)
 * @version $Revision: 1.26 $
 */
public class FP extends Associator implements OptionHandler, CARuleMiner, TechnicalInformationHandler {

	/** for serialization */
	static final long serialVersionUID = 3277498842319212687L;
	protected long timecost;
	protected long numRules;
	/** The minimum support. */
	protected double m_minSupport;
	protected double m_minConv;
	protected long minNumRules;
	/** The upper bound on the support */
	protected double m_upperBoundMinSupport;

	/** The lower bound for the minimum support. */
	protected double m_lowerBoundMinSupport;

	/** Metric type: Confidence */
	protected static final int CONFIDENCE = 0;
	/** Metric type: Lift */
	protected static final int LIFT = 1;
	/** Metric type: Leverage */
	protected static final int LEVERAGE = 2;
	/** Metric type: Conviction */
	protected static final int CONVICTION = 3;
	/** Metric types. */
	public static final Tag[] TAGS_SELECTION = { new Tag(CONFIDENCE, "Confidence"), new Tag(LIFT, "Lift"),
			new Tag(LEVERAGE, "Leverage"), new Tag(CONVICTION, "Conviction") };

	/** The selected metric type. */
	protected int m_metricType = CONFIDENCE;

	/** The minimum metric score. */
	protected double m_minMetric;

	/** The maximum number of rules that are output. */
	protected int m_numRules;

	/** Delta by which m_minSupport is decreased in each iteration. */
	protected double m_delta;

	/** Significance level for optional significance test. */
	protected double m_significanceLevel;

	/** Number of cycles used before required number of rules was one. */
	protected int m_cycles;

	/** The set of all sets of itemsets L. */

	protected LinkedList m_Rules;
	protected boolean terminal;
	protected FastVector m_Ls;

	/** The same information stored in hash tables. */
	protected FastVector m_hashtables;

	/** m_counter **/
	protected Hashtable m_carhashtable;

	/** The list of all generated rules. */
	protected FastVector[] m_allTheRules;

	/**
	 * The instances (transactions) to be used for generating the association
	 * rules.
	 */
	protected Instances m_instances;

	/** Output itemsets found? */
	protected boolean m_outputItemSets;

	/** Remove columns with all missing values */
	protected boolean m_removeMissingCols;

	/** Report progress iteratively */
	protected boolean m_verbose;

	/** Only the class attribute of all Instances. */
	public Instances m_onlyClass;

	/** The class index. */
	protected int m_classIndex;

	/** Flag indicating whether class association rules are mined. */
	protected boolean m_car;
	/////// added text
	public String m_text;

	private Instances allData;

	/**
	 * Returns a string describing this associator
	 * 
	 * @return a description of the evaluator suitable for displaying in the
	 *         explorer/experimenter gui
	 */
	public String globalInfo() {
		return "Class implementing an Apriori-type algorithm. Iteratively reduces "
				+ "the minimum support until it finds the required number of rules with "
				+ "the given minimum confidence.\n"
				+ "The algorithm has an option to mine class association rules. It is "
				+ "adapted as explained in the second reference.\n\n" + "For more information see:\n\n"
				+ getTechnicalInformation().toString();
	}

	/**
	 * Returns an instance of a TechnicalInformation object, containing detailed
	 * information about the technical background of this class, e.g., paper
	 * reference or book this class is based on.
	 * 
	 * @return the technical information about this class
	 */
	public TechnicalInformation getTechnicalInformation() {
		TechnicalInformation result;
		TechnicalInformation additional;

		result = new TechnicalInformation(Type.INPROCEEDINGS);
		result.setValue(Field.AUTHOR, "R. Agrawal and R. Srikant");
		result.setValue(Field.TITLE, "Fast Algorithms for Mining Association Rules in Large Databases");
		result.setValue(Field.BOOKTITLE, "20th International Conference on Very Large Data Bases");
		result.setValue(Field.YEAR, "1994");
		result.setValue(Field.PAGES, "478-499");
		result.setValue(Field.PUBLISHER, "Morgan Kaufmann, Los Altos, CA");

		additional = result.add(Type.INPROCEEDINGS);
		additional.setValue(Field.AUTHOR, "Bing Liu and Wynne Hsu and Yiming Ma");
		additional.setValue(Field.TITLE, "Integrating Classification and Association Rule Mining");
		additional.setValue(Field.BOOKTITLE, "Fourth International Conference on Knowledge Discovery and Data Mining");
		additional.setValue(Field.YEAR, "1998");
		additional.setValue(Field.PAGES, "80-86");
		additional.setValue(Field.PUBLISHER, "AAAI Press");

		return result;
	}

	/**
	 * Constructor that allows to sets default values for the minimum confidence
	 * and the maximum number of rules the minimum confidence.
	 */
	public FP() {

		resetOptions();
	}

	/**
	 * Resets the options to the default values.
	 */
	public void resetOptions() {

		m_removeMissingCols = false;
		m_verbose = false;
		m_delta = 0.05;
		m_minMetric = 0.90;
		// m_numRules =20;
		m_numRules = Integer.MAX_VALUE;
		m_lowerBoundMinSupport = 0.01;
		m_upperBoundMinSupport = 1.0;
		m_significanceLevel = -1;
		m_outputItemSets = true;
		m_car = false;
		m_classIndex = -1;
		m_text = "";
	}

	/**
	 * Removes columns that are all missing from the data
	 * 
	 * @param instances
	 *            the instances
	 * @return a new set of instances with all missing columns removed
	 * @throws Exception
	 *             if something goes wrong
	 */
	protected Instances removeMissingColumns(Instances instances) throws Exception {

		int numInstances = instances.numInstances();
		StringBuffer deleteString = new StringBuffer();
		int removeCount = 0;
		boolean first = true;
		int maxCount = 0;

		for (int i = 0; i < instances.numAttributes(); i++) {
			AttributeStats as = instances.attributeStats(i);
			if (m_upperBoundMinSupport == 1.0 && maxCount != numInstances) {
				// see if we can decrease this by looking for the most frequent
				// value
				int[] counts = as.nominalCounts;
				if (counts[Utils.maxIndex(counts)] > maxCount) {
					maxCount = counts[Utils.maxIndex(counts)];
				}
			}
			if (as.missingCount == numInstances) {
				if (first) {
					deleteString.append((i + 1));
					first = false;
				} else {
					deleteString.append("," + (i + 1));
				}
				removeCount++;
			}
		}
		if (m_verbose) {
			System.err.println("Removed : " + removeCount + " columns with all missing " + "values.");
		}
		if (m_upperBoundMinSupport == 1.0 && maxCount != numInstances) {
			m_upperBoundMinSupport = (double) maxCount / (double) numInstances;
			if (m_verbose) {
				System.err.println("Setting upper bound min support to : " + m_upperBoundMinSupport);
			}
		}

		if (deleteString.toString().length() > 0) {
			Remove af = new Remove();
			af.setAttributeIndices(deleteString.toString());
			af.setInvertSelection(false);
			af.setInputFormat(instances);
			Instances newInst = Filter.useFilter(instances, af);

			return newInst;
		}
		return instances;
	}

	/**
	 * Returns default capabilities of the classifier.
	 *
	 * @return the capabilities of this classifier
	 */
	public Capabilities getCapabilities() {
		Capabilities result = super.getCapabilities();

		// attributes
		result.enable(Capability.NOMINAL_ATTRIBUTES);
		result.enable(Capability.MISSING_VALUES);

		// class
		result.enable(Capability.NOMINAL_CLASS);
		result.enable(Capability.MISSING_CLASS_VALUES);

		return result;
	}

	/**
	 * Method that generates all large itemsets with a minimum support, and from
	 * these all association rules with a minimum confidence.
	 *
	 * @param instances
	 *            the instances to be used for generating the associations
	 * @throws Exception
	 *             if rules can't be built successfully
	 */
	public void buildAssociations(Instances instances) throws Exception {

		double[] confidences, supports;
		int[] indices;
		FastVector[] sortedRuleSet;
		int necSupport = 0;

		if (m_removeMissingCols) {
			instances = removeMissingColumns(instances);
		}
		if (m_car && m_metricType != CONFIDENCE)
			throw new Exception("For CAR-Mining metric type has to be confidence!");

		if (m_classIndex == -1)
			instances.setClassIndex(instances.numAttributes() - 1);
		else if (m_classIndex < instances.numAttributes() && m_classIndex >= 0)
			instances.setClassIndex(m_classIndex);
		else
			throw new Exception("Invalid class index.");

		// can associator handle the data?
		getCapabilities().testWithFail(instances);

		m_cycles = 0;
		if (m_car) {
			// m_instances does not contain the class attribute
			m_instances = LabeledItemSet.divide(instances, false);

			// m_onlyClass contains only the class attribute
			m_onlyClass = LabeledItemSet.divide(instances, true);
		} else
			m_instances = instances;

		if (m_numRules == Integer.MAX_VALUE) {
			// Set desired minimum support
			m_minSupport = m_lowerBoundMinSupport;
		} else {
			// Decrease minimum support until desired number of rules found.
			m_minSupport = m_upperBoundMinSupport - m_delta;
			m_minSupport = (m_minSupport < m_lowerBoundMinSupport) ? m_lowerBoundMinSupport : m_minSupport;
		}

		do {

			// Reserve space for variables
			m_Ls = new FastVector();
			m_Rules = new LinkedList<RuleItems>();
			m_hashtables = new FastVector();
			m_allTheRules = new FastVector[6];
			m_allTheRules[0] = new FastVector();
			m_allTheRules[1] = new FastVector();
			m_allTheRules[2] = new FastVector();
			if (m_metricType != CONFIDENCE || m_significanceLevel != -1) {
				m_allTheRules[3] = new FastVector();
				m_allTheRules[4] = new FastVector();
				m_allTheRules[5] = new FastVector();
			}
			sortedRuleSet = new FastVector[6];
			sortedRuleSet[0] = new FastVector();
			sortedRuleSet[1] = new FastVector();
			sortedRuleSet[2] = new FastVector();
			if (m_metricType != CONFIDENCE || m_significanceLevel != -1) {
				sortedRuleSet[3] = new FastVector();
				sortedRuleSet[4] = new FastVector();
				sortedRuleSet[5] = new FastVector();
			}
			if (!m_car) {
				// Find large itemsets and rules
				long t1 = System.currentTimeMillis();
				// findLargeItemSets();
				newfindLargeItemSets();
				// findLargeItemSetFP();
				long t2 = System.currentTimeMillis();
				timecost = t2 - t1; ////////////////////////
				// if (m_significanceLevel != -1 || m_metricType != CONFIDENCE)
				// findRulesBruteForce();
				// else
				// findRulesQuickly();
			} else {
				long t1 = System.currentTimeMillis();
				// findLargeCarItemSets();
				// findCarLargeItemSetFP();
				CMAR();
				long t2 = System.currentTimeMillis();
				timecost = t2 - t1;
				// findCarRulesQuickly();
			}

			// Sort rules according to their support
			/*
			 * supports = new double[m_allTheRules[2].size()]; for (int i = 0; i
			 * < m_allTheRules[2].size(); i++) supports[i] =
			 * (double)((AprioriItemSet)m_allTheRules[1].elementAt(i)).support()
			 * ; indices = Utils.stableSort(supports); for (int i = 0; i <
			 * m_allTheRules[2].size(); i++) {
			 * sortedRuleSet[0].addElement(m_allTheRules[0].elementAt(indices[i]
			 * ));
			 * sortedRuleSet[1].addElement(m_allTheRules[1].elementAt(indices[i]
			 * ));
			 * sortedRuleSet[2].addElement(m_allTheRules[2].elementAt(indices[i]
			 * )); if (m_metricType != CONFIDENCE || m_significanceLevel != -1)
			 * {
			 * sortedRuleSet[3].addElement(m_allTheRules[3].elementAt(indices[i]
			 * ));
			 * sortedRuleSet[4].addElement(m_allTheRules[4].elementAt(indices[i]
			 * ));
			 * sortedRuleSet[5].addElement(m_allTheRules[5].elementAt(indices[i]
			 * )); } }
			 */
			// int j = m_allTheRules[2].size()-1;
			// supports = new double[m_allTheRules[2].size()];
			// for (int i = 0; i < (j+1); i++)
			// supports[j-i] =
			// ((double)((ItemSet)m_allTheRules[1].elementAt(j-i)).support())*(-1);
			// indices = Utils.stableSort(supports);
			// for (int i = 0; i < (j+1); i++) {
			// sortedRuleSet[0].addElement(m_allTheRules[0].elementAt(indices[j-i]));
			// sortedRuleSet[1].addElement(m_allTheRules[1].elementAt(indices[j-i]));
			// sortedRuleSet[2].addElement(m_allTheRules[2].elementAt(indices[j-i]));
			// if (m_metricType != CONFIDENCE || m_significanceLevel != -1) {
			// sortedRuleSet[3].addElement(m_allTheRules[3].elementAt(indices[j-i]));
			// sortedRuleSet[4].addElement(m_allTheRules[4].elementAt(indices[j-i]));
			// sortedRuleSet[5].addElement(m_allTheRules[5].elementAt(indices[j-i]));
			// }
			// }
			//
			// // Sort rules according to their confidence
			// m_allTheRules[0].removeAllElements();
			// m_allTheRules[1].removeAllElements();
			// m_allTheRules[2].removeAllElements();
			// if (m_metricType != CONFIDENCE || m_significanceLevel != -1) {
			// m_allTheRules[3].removeAllElements();
			// m_allTheRules[4].removeAllElements();
			// m_allTheRules[5].removeAllElements();
			// }
			// confidences = new double[sortedRuleSet[2].size()];
			// int sortType = 2 + m_metricType;
			//
			// for (int i = 0; i < sortedRuleSet[2].size(); i++)
			// confidences[i] =
			// ((Double)sortedRuleSet[sortType].elementAt(i)).doubleValue();
			// indices = Utils.stableSort(confidences);
			// for (int i = sortedRuleSet[0].size() - 1;
			// (i >= (sortedRuleSet[0].size() - m_numRules)) && (i >= 0); i--) {
			// m_allTheRules[0].addElement(sortedRuleSet[0].elementAt(indices[i]));
			// m_allTheRules[1].addElement(sortedRuleSet[1].elementAt(indices[i]));
			// m_allTheRules[2].addElement(sortedRuleSet[2].elementAt(indices[i]));
			// if (m_metricType != CONFIDENCE || m_significanceLevel != -1) {
			// m_allTheRules[3].addElement(sortedRuleSet[3].elementAt(indices[i]));
			// m_allTheRules[4].addElement(sortedRuleSet[4].elementAt(indices[i]));
			// m_allTheRules[5].addElement(sortedRuleSet[5].elementAt(indices[i]));
			// }
			// }
			//
			// if (m_verbose) {
			// if (m_Ls.size() > 1) {
			// System.out.println(toString());
			// }
			// }
			// if(m_minSupport == m_lowerBoundMinSupport || m_minSupport -
			// m_delta > m_lowerBoundMinSupport)
			// m_minSupport -= m_delta;
			// else
			// m_minSupport = m_lowerBoundMinSupport;
			//
			// necSupport = Math.round((float)((m_minSupport *
			// (double)m_instances.numInstances())+0.5));
			//
			// m_cycles++;
			// if(m_numRules==Integer.MAX_VALUE)
			// break;
		} while ((m_allTheRules[0].size() < m_numRules) && (Utils.grOrEq(m_minSupport, m_lowerBoundMinSupport))
		/* (necSupport >= lowerBoundNumInstancesSupport) */
		/* (Utils.grOrEq(m_minSupport, m_lowerBoundMinSupport)) */ && (necSupport >= 1));
		m_minSupport += m_delta;
	}

	/**
	 * Method that mines all class association rules with minimum support and
	 * with a minimum confidence.
	 * 
	 * @return an sorted array of FastVector (confidence depended) containing
	 *         the rules and metric information
	 * @param data
	 *            the instances for which class association rules should be
	 *            mined
	 * @throws Exception
	 *             if rules can't be built successfully
	 */
	public FastVector[] mineCARs(Instances data) throws Exception {

		m_car = true;
		buildAssociations(data);
		return m_allTheRules;
	}

	/**
	 * Gets the instances without the class atrribute.
	 *
	 * @return the instances without the class attribute.
	 */
	public Instances getInstancesNoClass() {

		return m_instances;
	}

	/**
	 * Gets only the class attribute of the instances.
	 *
	 * @return the class attribute of all instances.
	 */
	public Instances getInstancesOnlyClass() {

		return m_onlyClass;
	}

	/**
	 * Returns an enumeration describing the available options.
	 *
	 * @return an enumeration of all the available options.
	 */
	public Enumeration listOptions() {

		String string1 = "\tThe required number of rules. (default = " + m_numRules + ")",
				string2 = "\tThe minimum confidence of a rule. (default = " + m_minMetric + ")",
				string3 = "\tThe delta by which the minimum support is decreased in\n",
				string4 = "\teach iteration. (default = " + m_delta + ")",
				string5 = "\tThe lower bound for the minimum support. (default = " + m_lowerBoundMinSupport + ")",
				string6 = "\tIf used, rules are tested for significance at\n",
				string7 = "\tthe given level. Slower. (default = no significance testing)",
				string8 = "\tIf set the itemsets found are also output. (default = no)",
				string9 = "\tIf set class association rules are mined. (default = no)",
				string10 = "\tThe class index. (default = last)",
				stringType = "\tThe metric type by which to rank rules. (default = " + "confidence)";

		FastVector newVector = new FastVector(11);

		newVector.addElement(new Option(string1, "N", 1, "-N <required number of rules output>"));
		newVector.addElement(
				new Option(stringType, "T", 1, "-T <0=confidence | 1=lift | " + "2=leverage | 3=Conviction>"));
		newVector.addElement(new Option(string2, "C", 1, "-C <minimum metric score of a rule>"));
		newVector.addElement(new Option(string3 + string4, "D", 1, "-D <delta for minimum support>"));
		newVector.addElement(new Option("\tUpper bound for minimum support. " + "(default = 1.0)", "U", 1,
				"-U <upper bound for minimum support>"));
		newVector.addElement(new Option(string5, "M", 1, "-M <lower bound for minimum support>"));
		newVector.addElement(new Option(string6 + string7, "S", 1, "-S <significance level>"));
		newVector.addElement(new Option(string8, "I", 0, "-I"));
		newVector.addElement(
				new Option("\tRemove columns that contain " + "all missing values (default = no)", "R", 0, "-R"));
		newVector.addElement(new Option("\tReport progress iteratively. (default " + "= no)", "V", 0, "-V"));
		newVector.addElement(new Option(string9, "A", 0, "-A"));
		newVector.addElement(new Option(string10, "c", 1, "-c <the class index>"));

		return newVector.elements();
	}

	/**
	 * Parses a given list of options.
	 * <p/>
	 * 
	 * <!-- options-start --> Valid options are:
	 * <p/>
	 * 
	 * <pre>
	 *  -N &lt;required number of rules output&gt;
	 *  The required number of rules. (default = 10)
	 * </pre>
	 * 
	 * <pre>
	 *  -T &lt;0=confidence | 1=lift | 2=leverage | 3=Conviction&gt;
	 *  The metric type by which to rank rules. (default = confidence)
	 * </pre>
	 * 
	 * <pre>
	 *  -C &lt;minimum metric score of a rule&gt;
	 *  The minimum confidence of a rule. (default = 0.9)
	 * </pre>
	 * 
	 * <pre>
	 *  -D &lt;delta for minimum support&gt;
	 *  The delta by which the minimum support is decreased in
	 *  each iteration. (default = 0.05)
	 * </pre>
	 * 
	 * <pre>
	 *  -U &lt;upper bound for minimum support&gt;
	 *  Upper bound for minimum support. (default = 1.0)
	 * </pre>
	 * 
	 * <pre>
	 *  -M &lt;lower bound for minimum support&gt;
	 *  The lower bound for the minimum support. (default = 0.1)
	 * </pre>
	 * 
	 * <pre>
	 *  -S &lt;significance level&gt;
	 *  If used, rules are tested for significance at
	 *  the given level. Slower. (default = no significance testing)
	 * </pre>
	 * 
	 * <pre>
	 *  -I
	 *  If set the itemsets found are also output. (default = no)
	 * </pre>
	 * 
	 * <pre>
	 *  -R
	 *  Remove columns that contain all missing values (default = no)
	 * </pre>
	 * 
	 * <pre>
	 *  -V
	 *  Report progress iteratively. (default = no)
	 * </pre>
	 * 
	 * <pre>
	 *  -A
	 *  If set class association rules are mined. (default = no)
	 * </pre>
	 * 
	 * <pre>
	 *  -c &lt;the class index&gt;
	 *  The class index. (default = last)
	 * </pre>
	 * 
	 * <!-- options-end -->
	 *
	 * @param options
	 *            the list of options as an array of strings
	 * @throws Exception
	 *             if an option is not supported
	 */
	public void setOptions(String[] options) throws Exception {

		resetOptions();
		String numRulesString = Utils.getOption('N', options), minConfidenceString = Utils.getOption('C', options),
				deltaString = Utils.getOption('D', options), maxSupportString = Utils.getOption('U', options),
				minSupportString = Utils.getOption('M', options),
				significanceLevelString = Utils.getOption('S', options),
				classIndexString = Utils.getOption('c', options);
		String metricTypeString = Utils.getOption('T', options);
		if (metricTypeString.length() != 0) {
			setMetricType(new SelectedTag(Integer.parseInt(metricTypeString), TAGS_SELECTION));
		}

		if (numRulesString.length() != 0) {
			m_numRules = Integer.parseInt(numRulesString);
		}
		if (classIndexString.length() != 0) {
			m_classIndex = Integer.parseInt(classIndexString);
		}
		if (minConfidenceString.length() != 0) {
			m_minMetric = (new Double(minConfidenceString)).doubleValue();
		}
		if (deltaString.length() != 0) {
			m_delta = (new Double(deltaString)).doubleValue();
		}
		if (maxSupportString.length() != 0) {
			setUpperBoundMinSupport((new Double(maxSupportString)).doubleValue());
		}
		if (minSupportString.length() != 0) {
			m_lowerBoundMinSupport = (new Double(minSupportString)).doubleValue();
		}
		if (significanceLevelString.length() != 0) {
			m_significanceLevel = (new Double(significanceLevelString)).doubleValue();
		}
		m_outputItemSets = Utils.getFlag('I', options);
		m_car = Utils.getFlag('A', options);
		m_verbose = Utils.getFlag('V', options);
		setRemoveAllMissingCols(Utils.getFlag('R', options));
	}

	/**
	 * Gets the current settings of the Apriori object.
	 *
	 * @return an array of strings suitable for passing to setOptions
	 */
	public String[] getOptions() {

		String[] options = new String[20];
		int current = 0;

		if (m_outputItemSets) {
			options[current++] = "-I";
		}

		if (getRemoveAllMissingCols()) {
			options[current++] = "-R";
		}

		options[current++] = "-N";
		options[current++] = "" + m_numRules;
		options[current++] = "-T";
		options[current++] = "" + m_metricType;
		options[current++] = "-C";
		options[current++] = "" + m_minMetric;
		options[current++] = "-D";
		options[current++] = "" + m_delta;
		options[current++] = "-U";
		options[current++] = "" + m_upperBoundMinSupport;
		options[current++] = "-M";
		options[current++] = "" + m_lowerBoundMinSupport;
		options[current++] = "-S";
		options[current++] = "" + m_significanceLevel;
		if (m_car)
			options[current++] = "-A";
		if (m_verbose)
			options[current++] = "-V";
		options[current++] = "-c";
		options[current++] = "" + m_classIndex;

		while (current < options.length) {
			options[current++] = "";
		}
		return options;
	}

	/**
	 * Outputs the size of all the generated sets of itemsets and the rules.
	 * 
	 * @return a string representation of the model
	 */
	public String toString() {

		StringBuffer text = new StringBuffer();
		if (m_text.equals(null)) {
			text.append(".........!!!!");
		}
		text.append(m_text);
		text.append("\nthe cost of finding large item sets:" + timecost);
		text.append("\nthe number of finding large item sets:" + m_Ls.size());
		text.append("\nthe number of finding large item sets:" + numRules);
		if (m_Rules.size() < 1)
			// return "\nNo large itemsets and rules found!\n"+m_Ls.size();
			if (m_Rules.size() == 1) {
			text.append("\nLarge Itemsets L(" + (1) + "):\n");
			for (int j = 0; j < ((FastVector) m_Ls.elementAt(0)).size(); j++)
			text.append(((AprioriItemSet) ((FastVector) m_Ls.elementAt(0)).elementAt(j)).toString(m_instances) + "\n");
			return text.toString();
			}

		text.append("\nApriori\n=======\n\n");
		text.append("Minimum support: " + Utils.doubleToString(m_minSupport, 2) + " ("
				+ ((int) (m_minSupport * (double) m_instances.numInstances() + 0.5)) + " instances)" + '\n');
		text.append("Minimum metric <");
		switch (m_metricType) {
		case CONFIDENCE:
			text.append("confidence>: ");
			break;
		case LIFT:
			text.append("lift>: ");
			break;
		case LEVERAGE:
			text.append("leverage>: ");
			break;
		case CONVICTION:
			text.append("conviction>: ");
			break;
		}
		text.append(Utils.doubleToString(m_minMetric, 2) + '\n');

		if (m_significanceLevel != -1)
			text.append("Significance level: " + Utils.doubleToString(m_significanceLevel, 2) + '\n');
		text.append("Number of cycles performed: " + m_cycles + '\n');
		text.append("\nGenerated sets of large itemsets:\n");
		// if(!m_car){
		// for (int i = 0; i < m_Ls.size(); i++) {
		// text.append("\nSize of set of large itemsets L("+(i+1)+"): "+
		// ((FastVector)m_Ls.elementAt(i)).size()+'\n');
		// if (m_outputItemSets)
		// {
		// text.append("\nLarge Itemsets L("+(i+1)+"):\n");
		// for (int j = 0; j < ((FastVector)m_Ls.elementAt(i)).size(); j++)
		// text.append(((AprioriItemSet)((FastVector)m_Ls.elementAt(i)).elementAt(j)).
		// toString(m_instances)+"\n");
		// }
		// }
		// text.append("\nBest rules found:\n\n");
		// for (int i = 0; i < m_allTheRules[0].size(); i++) {
		// text.append(Utils.doubleToString((double)i+1,
		// (int)(Math.log(m_numRules)/Math.log(10)+1),0)+
		// ". " + ((AprioriItemSet)m_allTheRules[0].elementAt(i)).
		// toString(m_instances)
		// + " ==> " + ((AprioriItemSet)m_allTheRules[1].elementAt(i)).
		// toString(m_instances) +" conf:("+
		// Utils.doubleToString(((Double)m_allTheRules[2].
		// elementAt(i)).doubleValue(),2)+")");
		// if (m_metricType != CONFIDENCE || m_significanceLevel != -1) {
		// text.append((m_metricType == LIFT ? " <" : "")+" lift:("+
		// Utils.doubleToString(((Double)m_allTheRules[3].
		// elementAt(i)).doubleValue(),2)
		// +")"+(m_metricType == LIFT ? ">" : ""));
		// text.append((m_metricType == LEVERAGE ? " <" : "")+" lev:("+
		// Utils.doubleToString(((Double)m_allTheRules[4].
		// elementAt(i)).doubleValue(),2)
		// +")");
		// text.append(" ["+
		// (int)(((Double)m_allTheRules[4].elementAt(i))
		// .doubleValue() * (double)m_instances.numInstances())
		// +"]"+(m_metricType == LEVERAGE ? ">" : ""));
		// text.append((m_metricType == CONVICTION ? " <" : "")+" conv:("+
		// Utils.doubleToString(((Double)m_allTheRules[5].
		// elementAt(i)).doubleValue(),2)
		// +")"+(m_metricType == CONVICTION ? ">" : ""));
		// }
		// text.append('\n');
		// }
		// }
		if (m_car) {

			text.append("\nBest rules found:\n\n");
			ListIterator<RuleItems> ruleiter = m_Rules.listIterator();
			int i = 0;
			while (ruleiter.hasNext()) {
				RuleItems item = ruleiter.next();
				text.append(Utils.doubleToString((double) i + 1, (int) (Math.log(m_numRules) / Math.log(10) + 1), 0)
						+ ". " + item.toString(m_instances, m_onlyClass));
				text.append('\n');
				i++;
			}
		}

		return text.toString();
	}

	/**
	 * Returns the metric string for the chosen metric type
	 * 
	 * @return a string describing the used metric for the interestingness of a
	 *         class association rule
	 */
	public String metricString() {

		switch (m_metricType) {
		case LIFT:
			return "lif";
		case LEVERAGE:
			return "leverage";
		case CONVICTION:
			return "conviction";
		default:
			return "conf";
		}
	}

	/**
	 * Returns the tip text for this property
	 * 
	 * @return tip text for this property suitable for displaying in the
	 *         explorer/experimenter gui
	 */
	public String removeAllMissingColsTipText() {
		return "Remove columns with all missing values.";
	}

	/**
	 * Remove columns containing all missing values.
	 * 
	 * @param r
	 *            true if cols are to be removed.
	 */
	public void setRemoveAllMissingCols(boolean r) {
		m_removeMissingCols = r;
	}

	/**
	 * Returns whether columns containing all missing values are to be removed
	 * 
	 * @return true if columns are to be removed.
	 */
	public boolean getRemoveAllMissingCols() {
		return m_removeMissingCols;
	}

	/**
	 * Returns the tip text for this property
	 * 
	 * @return tip text for this property suitable for displaying in the
	 *         explorer/experimenter gui
	 */
	public String upperBoundMinSupportTipText() {
		return "Upper bound for minimum support. Start iteratively decreasing " + "minimum support from this value.";
	}

	/**
	 * Get the value of upperBoundMinSupport.
	 *
	 * @return Value of upperBoundMinSupport.
	 */
	public double getUpperBoundMinSupport() {

		return m_upperBoundMinSupport;
	}

	/**
	 * Set the value of upperBoundMinSupport.
	 *
	 * @param v
	 *            Value to assign to upperBoundMinSupport.
	 */
	public void setUpperBoundMinSupport(double v) {

		m_upperBoundMinSupport = v;
	}

	/**
	 * Sets the class index
	 * 
	 * @param index
	 *            the class index
	 */
	public void setClassIndex(int index) {

		m_classIndex = index;
	}

	/**
	 * Gets the class index
	 * 
	 * @return the index of the class attribute
	 */
	public int getClassIndex() {

		return m_classIndex;
	}

	/**
	 * Returns the tip text for this property
	 * 
	 * @return tip text for this property suitable for displaying in the
	 *         explorer/experimenter gui
	 */
	public String classIndexTipText() {
		return "Index of the class attribute. If set to -1, the last attribute is taken as class attribute.";

	}

	/**
	 * Sets class association rule mining
	 * 
	 * @param flag
	 *            if class association rules are mined, false otherwise
	 */
	public void setCar(boolean flag) {
		m_car = flag;
	}

	private void setCarhashtable() {
		int num = m_instances.numAttributes();
		m_carhashtable = new Hashtable(num);
	}

	/**
	 * Gets whether class association ruels are mined
	 * 
	 * @return true if class association rules are mined, false otherwise
	 */
	public boolean getCar() {
		return m_car;
	}

	/**
	 * Returns the tip text for this property
	 * 
	 * @return tip text for this property suitable for displaying in the
	 *         explorer/experimenter gui
	 */
	public String carTipText() {
		return "If enabled class association rules are mined instead of (general) association rules.";
	}

	/**
	 * Returns the tip text for this property
	 * 
	 * @return tip text for this property suitable for displaying in the
	 *         explorer/experimenter gui
	 */
	public String lowerBoundMinSupportTipText() {
		return "Lower bound for minimum support.";
	}

	/**
	 * Get the value of lowerBoundMinSupport.
	 *
	 * @return Value of lowerBoundMinSupport.
	 */
	public double getLowerBoundMinSupport() {

		return m_lowerBoundMinSupport;
	}

	/**
	 * Set the value of lowerBoundMinSupport.
	 *
	 * @param v
	 *            Value to assign to lowerBoundMinSupport.
	 */
	public void setLowerBoundMinSupport(double v) {

		m_lowerBoundMinSupport = v;
	}

	/**
	 * Get the metric type
	 *
	 * @return the type of metric to use for ranking rules
	 */
	public SelectedTag getMetricType() {
		return new SelectedTag(m_metricType, TAGS_SELECTION);
	}

	/**
	 * Returns the tip text for this property
	 * 
	 * @return tip text for this property suitable for displaying in the
	 *         explorer/experimenter gui
	 */
	public String metricTypeTipText() {
		return "Set the type of metric by which to rank rules. Confidence is "
				+ "the proportion of the examples covered by the premise that are also "
				+ "covered by the consequence(Class association rules can only be mined using confidence). Lift is confidence divided by the "
				+ "proportion of all examples that are covered by the consequence. This "
				+ "is a measure of the importance of the association that is independent "
				+ "of support. Leverage is the proportion of additional examples covered "
				+ "by both the premise and consequence above those expected if the "
				+ "premise and consequence were independent of each other. The total "
				+ "number of examples that this represents is presented in brackets "
				+ "following the leverage. Conviction is "
				+ "another measure of departure from independence. Conviction is given " + "by ";
	}

	/**
	 * Set the metric type for ranking rules
	 *
	 * @param d
	 *            the type of metric
	 */
	public void setMetricType(SelectedTag d) {

		if (d.getTags() == TAGS_SELECTION) {
			m_metricType = d.getSelectedTag().getID();
		}

		if (m_significanceLevel != -1 && m_metricType != CONFIDENCE) {
			m_metricType = CONFIDENCE;
		}

		if (m_metricType == CONFIDENCE) {
			setMinMetric(0.9);
		}

		if (m_metricType == LIFT || m_metricType == CONVICTION) {
			setMinMetric(1.1);
		}

		if (m_metricType == LEVERAGE) {
			setMinMetric(0.1);
		}
	}

	/**
	 * Returns the tip text for this property
	 * 
	 * @return tip text for this property suitable for displaying in the
	 *         explorer/experimenter gui
	 */
	public String minMetricTipText() {
		return "Minimum metric score. Consider only rules with scores higher than " + "this value.";
	}

	/**
	 * Get the value of minConfidence.
	 *
	 * @return Value of minConfidence.
	 */
	public double getMinMetric() {

		return m_minMetric;
	}

	/**
	 * Set the value of minConfidence.
	 *
	 * @param v
	 *            Value to assign to minConfidence.
	 */
	public void setMinMetric(double v) {

		m_minMetric = v;
	}

	/**
	 * Returns the tip text for this property
	 * 
	 * @return tip text for this property suitable for displaying in the
	 *         explorer/experimenter gui
	 */
	public String numRulesTipText() {
		return "Number of rules to find.";
	}

	/**
	 * Get the value of numRules.
	 *
	 * @return Value of numRules.
	 */
	public int getNumRules() {

		return m_numRules;
	}

	/**
	 * Set the value of numRules.
	 *
	 * @param v
	 *            Value to assign to numRules.
	 */
	public void setNumRules(int v) {

		m_numRules = v;
	}

	/**
	 * Returns the tip text for this property
	 * 
	 * @return tip text for this property suitable for displaying in the
	 *         explorer/experimenter gui
	 */
	public String deltaTipText() {
		return "Iteratively decrease support by this factor. Reduces support "
				+ "until min support is reached or required number of rules has been " + "generated.";
	}

	/**
	 * Get the value of delta.
	 *
	 * @return Value of delta.
	 */
	public double getDelta() {

		return m_delta;
	}

	/**
	 * Set the value of delta.
	 *
	 * @param v
	 *            Value to assign to delta.
	 */
	public void setDelta(double v) {

		m_delta = v;
	}

	/**
	 * Returns the tip text for this property
	 * 
	 * @return tip text for this property suitable for displaying in the
	 *         explorer/experimenter gui
	 */
	public String significanceLevelTipText() {
		return "Significance level. Significance test (confidence metric only).";
	}

	/**
	 * Get the value of significanceLevel.
	 *
	 * @return Value of significanceLevel.
	 */
	public double getSignificanceLevel() {

		return m_significanceLevel;
	}

	/**
	 * Set the value of significanceLevel.
	 *
	 * @param v
	 *            Value to assign to significanceLevel.
	 */
	public void setSignificanceLevel(double v) {

		m_significanceLevel = v;
	}

	/**
	 * Sets whether itemsets are output as well
	 * 
	 * @param flag
	 *            true if itemsets are to be output as well
	 */
	public void setOutputItemSets(boolean flag) {
		m_outputItemSets = flag;
	}

	/**
	 * Gets whether itemsets are output as well
	 * 
	 * @return true if itemsets are output as well
	 */
	public boolean getOutputItemSets() {
		return m_outputItemSets;
	}

	/**
	 * Returns the tip text for this property
	 * 
	 * @return tip text for this property suitable for displaying in the
	 *         explorer/experimenter gui
	 */
	public String outputItemSetsTipText() {
		return "If enabled the itemsets are output as well.";
	}

	/**
	 * Sets verbose mode
	 * 
	 * @param flag
	 *            true if algorithm should be run in verbose mode
	 */
	public void setVerbose(boolean flag) {
		m_verbose = flag;
	}

	/**
	 * Gets whether algorithm is run in verbose mode
	 * 
	 * @return true if algorithm is run in verbose mode
	 */
	public boolean getVerbose() {
		return m_verbose;
	}

	/**
	 * Returns the tip text for this property
	 * 
	 * @return tip text for this property suitable for displaying in the
	 *         explorer/experimenter gui
	 */
	public String verboseTipText() {
		return "If enabled the algorithm will be run in verbose mode.";
	}

	/**
	 * Method that finds all large itemsets for the given set of instances.
	 *
	 * @throws Exception
	 *             if an attribute is numeric
	 */
	private void findLargeItemSets() throws Exception {

		FastVector kMinusOneSets, kSets;
		Hashtable hashtable;
		int necSupport, necMaxSupport, i = 0;

		// Find large itemsets

		// minimum support
		// necSupport = (int)(m_minSupport *
		// (double)m_instances.numInstances()+0.5);
		// necMaxSupport = (int)(m_upperBoundMinSupport *
		// (double)m_instances.numInstances()+0.5);
		double nextMinSupport = m_minSupport * (double) m_instances.numInstances();
		double nextMaxSupport = m_upperBoundMinSupport * (double) m_instances.numInstances();
		if ((double) Math.rint(nextMinSupport) == nextMinSupport) {
			necSupport = (int) nextMinSupport;
		} else {
			necSupport = Math.round((float) (nextMinSupport + 0.5));
		}
		if ((double) Math.rint(nextMaxSupport) == nextMaxSupport) {
			necMaxSupport = (int) nextMaxSupport;
		} else {
			necMaxSupport = Math.round((float) (nextMaxSupport + 0.5));
		}
		kSets = AprioriItemSet.singletons(m_instances);
		// displayKsets(kSets);
		AprioriItemSet.upDateCounters(kSets, m_instances);
		// displayKsets(kSets);
		kSets = AprioriItemSet.deleteItemSets(kSets, necSupport, necMaxSupport);
		// displayKsets(kSets);
		// m_text=m_text+" "+"..."+i+"
		// -----"+kSets.size()+"..."+m_minSupport+"\n";

		if (kSets.size() == 0)
			return;
		do {
			m_Ls.addElement(kSets);
			kMinusOneSets = kSets;
			kSets = AprioriItemSet.mergeAllItemSets(kMinusOneSets, i, m_instances.numInstances());
			hashtable = AprioriItemSet.getHashtable(kMinusOneSets, kMinusOneSets.size());
			m_hashtables.addElement(hashtable);
			// displayKsets(kSets);
			kSets = AprioriItemSet.pruneItemSets(kSets, hashtable);
			// displayKsets(kSets);
			AprioriItemSet.upDateCounters(kSets, m_instances);
			// displayKsets(kSets);
			kSets = AprioriItemSet.deleteItemSets(kSets, necSupport, necMaxSupport);
			// displayKsets(kSets);
			i++;
			// m_text=m_text+" "+"..."+i+"
			// -----"+kSets.size()+"..."+m_minSupport+"\n";

		} while (kSets.size() > 0);
		// findLargeItemSetsByFP();

	}

	private void newfindLargeItemSets() throws Exception {

		FastVector kMinusOneSets, kSets;
		Hashtable hashtable;
		int necSupport, necMaxSupport, i = 0;

		// Find large itemsets

		// minimum support
		// necSupport = (int)(m_minSupport *
		// (double)m_instances.numInstances()+0.5);
		// necMaxSupport = (int)(m_upperBoundMinSupport *
		// (double)m_instances.numInstances()+0.5);
		double nextMinSupport = m_minSupport * (double) m_instances.numInstances();
		double nextMaxSupport = m_upperBoundMinSupport * (double) m_instances.numInstances();
		if ((double) Math.rint(nextMinSupport) == nextMinSupport) {
			necSupport = (int) nextMinSupport;
		} else {
			necSupport = Math.round((float) (nextMinSupport + 0.5));
		}
		if ((double) Math.rint(nextMaxSupport) == nextMaxSupport) {
			necMaxSupport = (int) nextMaxSupport;
		} else {
			necMaxSupport = Math.round((float) (nextMaxSupport + 0.5));
		}

		FastVector ksets = LabeledItemSet.newsingletons(m_instances);
		LabeledItemSet.newupDateCounters(ksets, m_instances);

		ksets = LabeledItemSet.deleteItemSets(ksets, necSupport, necMaxSupport);
		int ksize = ksets.size();
		for (int j = 0; j < ksize; j++) {
			for (int k = j + 1; k < ksize; k++) {
				int nj = ((LabeledItemSet) ksets.elementAt(j)).m_ruleSupCounter;
				int nk = ((LabeledItemSet) ksets.elementAt(k)).m_ruleSupCounter;
				if (nj < nk) {
					ksets.swap(j, k);
				}
			}
		} ///
		FastVector Head = new FastVector();
		for (int j = 0; j < ksets.size(); j++) { // biao L
			LabeledItemSet is = (LabeledItemSet) ksets.elementAt(j);
			LabelListHead ln = new LabelListHead(is);
			Head.addElement(ln);
		}
		CarFPtree ffp = buildCARTree(m_instances, Head);
		FastVector IsetI = mineCarTree(ffp, Head, ffp.root);

		newLabeledToItemSets(IsetI);

	}
	//

	public void displayKsets(FastVector kSets) {
		for (int j = 0; j < kSets.size(); j++) {
			ItemSet is = (ItemSet) kSets.elementAt(j);
			for (int k = 0; k < is.m_items.length; k++) {
				m_text += "." + is.m_items[k];
			}
			m_text += "(" + is.m_counter + ")" + "\t";
		}
		m_text += "\n";
	}

	public void displayHash(int hash[]) {
		m_text += "\n----------hash table-------\n";
		for (int i = 0; i < hash.length; i++) {
			m_text += i + "," + hash[i] + "\t";
		}
		m_text += "\n---------------------------\n";
	}

	public FastVector MergeVector(FastVector a, FastVector b) {
		FastVector result = new FastVector();
		int sizea = a.size();
		int sizeb = b.size();
		result.appendElements(b);
		for (int i = 0; i < sizea; i++) {
			TNode ta = (TNode) a.elementAt(i);
			boolean flag = true;
			for (int j = 0; j < sizeb; j++) {
				TNode tb = (TNode) result.elementAt(j);
				if (ta.equal(tb)) {
					tb.m_counter += ta.m_counter;
					flag = false;
					break;
				}
			}
			if (flag) {
				result.addElement(ta);
			}
		}
		return result;

	}

	public LabelItemSetII mergeLabelItemSetII(LabelItemSetII a, LabelItemSetII b) {
		int vb[] = b.m_items;
		int va[] = a.m_items;
		LabelItemSetII c = new LabelItemSetII(a.m_totalTransactions, a.m_sup.length);
		c.m_items = new int[vb.length];
		// c.m_counter=0;
		if ((!a.smaller(b)) && (!b.smaller(a))) {
			return null;
		}
		if (a.m_counter < b.m_counter) {
			c.m_counter = a.m_counter;
			for (int i = 0; i < a.m_sup.length; i++) {
				c.m_sup[i] = a.m_sup[i];
			}
		} else {
			c.m_counter = b.m_counter;
			for (int i = 0; i < b.m_sup.length; i++) {
				c.m_sup[i] = b.m_sup[i];
			}
		}
		for (int i = 0; i < va.length; i++) {
			if (va[i] != -1 && vb[i] != -1) {
				return null;
			} else if (va[i] != -1) {
				c.m_items[i] = va[i];
			} else {
				c.m_items[i] = vb[i];
			}
		}

		return c;
	}

	public ItemSet mergeTnode(TNode a, TNode b) {
		ItemSet is = new ItemSet(m_instances.numInstances());
		// TNode c=new TNode();
		if (a.attr == b.attr)
			return null;
		is.setItemAt(a.value, a.attr);
		is.setItemAt(b.value, b.attr);
		if (b.value < a.value)
			is.setCounter(b.value);
		else
			is.setCounter(a.value);
		return is;
	}

	public LabelTnode mergeLabelTnode(LabelTnode a, LabelTnode b) {
		int vb[] = b.value.m_items;
		int va[] = a.value.m_items;
		LabelTnode c = new LabelTnode();
		// c.value=new int[vb.length];
		// c.num=0;
		if (a.value.m_classLabel != b.value.m_classLabel)
			return null;
		int total = b.value.m_totalTransactions;
		int clas = b.value.m_classLabel;
		c.value = new LabeledItemSet(total, clas);
		int length = b.value.m_items.length;
		c.value.m_items = new int[length];
		// c.value=b.value.copy();
		for (int i = 0; i < va.length; i++) {
			if (va[i] != -1 && vb[i] != -1) {
				return null;
			} else if (va[i] != -1) {
				c.value.m_items[i] = va[i];
			} else {
				c.value.m_items[i] = vb[i];
			}
		}
		if (b.value.m_ruleSupCounter > a.value.m_ruleSupCounter)
			c.value.m_ruleSupCounter = a.value.m_ruleSupCounter;
		else
			c.value.m_ruleSupCounter = b.value.m_ruleSupCounter;
		return c;
	}

	public LabeledItemSet mergeLabelItemSet(LabeledItemSet a, LabeledItemSet b) {
		int vb[] = b.m_items;
		int va[] = a.m_items;
		// c.value=new int[vb.length];
		// c.num=0;
		if (a.m_classLabel != b.m_classLabel)
			return null;
		int total = b.m_totalTransactions;
		int clas = b.m_classLabel;
		LabeledItemSet c = new LabeledItemSet(total, clas);
		int length = b.m_items.length;
		c.m_items = new int[length];
		for (int i = 0; i < va.length; i++) {
			if (va[i] != -1 && vb[i] != -1) {
				return null;
			} else if (va[i] != -1) {
				c.m_items[i] = va[i];
			} else {
				c.m_items[i] = vb[i];
			}
		}
		if (b.m_ruleSupCounter > a.m_ruleSupCounter)
			c.m_ruleSupCounter = a.m_ruleSupCounter;
		else
			c.m_ruleSupCounter = b.m_ruleSupCounter;
		return c;
	}

	public ItemSet mergeItemSet(ItemSet a, ItemSet b) {
		int vb[] = b.m_items;
		int va[] = a.m_items;

		int total = b.m_totalTransactions;
		ItemSet c = new ItemSet(total);
		int length = b.m_items.length;
		c.m_items = new int[length];
		for (int i = 0; i < va.length; i++) {
			if (va[i] != -1 && vb[i] != -1) {
				return null;
			} else if (va[i] != -1) {
				c.m_items[i] = va[i];
			} else {
				c.m_items[i] = vb[i];
			}
		}
		if (b.m_counter > a.m_counter)
			c.m_counter = a.m_counter;
		else
			c.m_counter = b.m_counter;
		return c;
	}

	/**
	 * Method that finds all association rules and performs significance test.
	 *
	 * @throws Exception
	 *             if an attribute is numeric
	 */
	private void findRulesBruteForce() throws Exception {

		FastVector[] rules;

		// Build rules
		for (int j = 1; j < m_Ls.size(); j++) {
			FastVector currentItemSets = (FastVector) m_Ls.elementAt(j);
			Enumeration enumItemSets = currentItemSets.elements();
			while (enumItemSets.hasMoreElements()) {
				AprioriItemSet currentItemSet = (AprioriItemSet) enumItemSets.nextElement();
				// AprioriItemSet currentItemSet = new
				// AprioriItemSet((ItemSet)enumItemSets.nextElement());
				rules = currentItemSet.generateRulesBruteForce(m_minMetric, m_metricType, m_hashtables, j + 1,
						m_instances.numInstances(), m_significanceLevel);
				for (int k = 0; k < rules[0].size(); k++) {
					m_allTheRules[0].addElement(rules[0].elementAt(k));
					m_allTheRules[1].addElement(rules[1].elementAt(k));
					m_allTheRules[2].addElement(rules[2].elementAt(k));

					m_allTheRules[3].addElement(rules[3].elementAt(k));
					m_allTheRules[4].addElement(rules[4].elementAt(k));
					m_allTheRules[5].addElement(rules[5].elementAt(k));
				}
			}
		}
	}

	/**
	 * Method that finds all association rules.
	 *
	 * @throws Exception
	 *             if an attribute is numeric
	 */
	private void findRulesQuickly() throws Exception {

		FastVector[] rules;

		// Build rules
		for (int j = 1; j < m_Ls.size(); j++) {
			FastVector currentItemSets = (FastVector) m_Ls.elementAt(j);
			Enumeration enumItemSets = currentItemSets.elements();
			while (enumItemSets.hasMoreElements()) {
				AprioriItemSet currentItemSet = (AprioriItemSet) enumItemSets.nextElement();
				// AprioriItemSet currentItemSet = new
				// AprioriItemSet((ItemSet)enumItemSets.nextElement());
				rules = currentItemSet.generateRules(m_minMetric, m_hashtables, j + 1);
				for (int k = 0; k < rules[0].size(); k++) {
					m_allTheRules[0].addElement(rules[0].elementAt(k));
					m_allTheRules[1].addElement(rules[1].elementAt(k));
					m_allTheRules[2].addElement(rules[2].elementAt(k));
				}
			}
		}
	}

	/**
	 *
	 * Method that finds all large itemsets for class association rules for the
	 * given set of instances.
	 * 
	 * @throws Exception
	 *             if an attribute is numeric
	 */
	private void findLargeCarItemSets() throws Exception {

		FastVector kMinusOneSets, kSets;
		Hashtable hashtable;
		int necSupport, necMaxSupport, i = 0;

		// Find large itemsets

		// minimum support
		double nextMinSupport = m_minSupport * (double) m_instances.numInstances();
		double nextMaxSupport = m_upperBoundMinSupport * (double) m_instances.numInstances();
		if ((double) Math.rint(nextMinSupport) == nextMinSupport) {
			necSupport = (int) nextMinSupport;
		} else {
			necSupport = Math.round((float) (nextMinSupport + 0.5));
		}
		if ((double) Math.rint(nextMaxSupport) == nextMaxSupport) {
			necMaxSupport = (int) nextMaxSupport;
		} else {
			necMaxSupport = Math.round((float) (nextMaxSupport + 0.5));
		}

		// find item sets of length one
		kSets = LabeledItemSet.singletons(m_instances, m_onlyClass);
		LabeledItemSet.upDateCounters(kSets, m_instances, m_onlyClass);

		// check if a item set of lentgh one is frequent, if not delete it
		kSets = LabeledItemSet.deleteItemSets(kSets, necSupport, necMaxSupport);
		if (kSets.size() == 0)
			return;
		do {
			m_Ls.addElement(kSets);
			kMinusOneSets = kSets;
			kSets = LabeledItemSet.mergeAllItemSets(kMinusOneSets, i, m_instances.numInstances());
			hashtable = LabeledItemSet.getHashtable(kMinusOneSets, kMinusOneSets.size());
			kSets = LabeledItemSet.pruneItemSets(kSets, hashtable);
			LabeledItemSet.upDateCounters(kSets, m_instances, m_onlyClass);
			kSets = LabeledItemSet.deleteItemSets(kSets, necSupport, necMaxSupport);
			i++;
		} while (kSets.size() > 0);
		// findCarLargeItemSetFP();
	}

	/**
	 * Method that finds all class association rules.
	 *
	 * @throws Exception
	 *             if an attribute is numeric
	 */
	private void findCarRulesQuickly() throws Exception {

		FastVector[] rules;
		double supB[] = getSupB();
		// Build rules
		for (int j = 0; j < m_Ls.size(); j++) {
			FastVector currentLabeledItemSets = (FastVector) m_Ls.elementAt(j);
			Enumeration enumLabeledItemSets = currentLabeledItemSets.elements();
			while (enumLabeledItemSets.hasMoreElements()) {
				LabeledItemSet currentLabeledItemSet = (LabeledItemSet) enumLabeledItemSets.nextElement();
				// rules =
				// currentLabeledItemSet.generateRulesII(supB,m_minMetric,false);
				rules = currentLabeledItemSet.generateRules(m_minMetric, false);
				for (int k = 0; k < rules[0].size(); k++) {
					m_allTheRules[0].addElement(rules[0].elementAt(k));
					m_allTheRules[1].addElement(rules[1].elementAt(k));
					m_allTheRules[2].addElement(rules[2].elementAt(k));
				}
			}
		}
	}

	/**
	 * returns all the rules
	 *
	 * @return all the rules
	 * @see #m_allTheRules
	 */
	public FastVector[] getAllTheRules() {
		return m_allTheRules;
	}

	public double[] getSupB() {
		int len = (m_onlyClass.attribute(0)).numValues();
		double[] supB = new double[len];
		int[] s = new int[len];
		for (int i = 0; i < len; i++) {
			s[i] = 0;
		}
		for (int i = 0; i < m_onlyClass.numInstances(); i++) {
			Instance instance = m_onlyClass.instance(i);
			int classlabel = (int) (m_onlyClass.instance(i).value(0));
			s[classlabel]++;
		}
		for (int i = 0; i < len; i++) {
			supB[i] = (double) s[i] / (double) (m_onlyClass.numInstances());
		}
		return supB;
	}

	private void findLargeItemSetFP() throws Exception {
		FastVector kSets;
		int total = m_instances.numInstances();
		int necSupport, necMaxSupport;
		double nextMinSupport = m_minSupport * (double) m_instances.numInstances();
		double nextMaxSupport = m_upperBoundMinSupport * (double) m_instances.numInstances();
		if ((double) Math.rint(nextMinSupport) == nextMinSupport) {
			necSupport = (int) nextMinSupport;
		} else {
			necSupport = Math.round((float) (nextMinSupport + 0.5));
		}
		if ((double) Math.rint(nextMaxSupport) == nextMaxSupport) {
			necMaxSupport = (int) nextMaxSupport;
		} else {
			necMaxSupport = Math.round((float) (nextMaxSupport + 0.5));
		}

		kSets = ListHead.singleton(m_instances);
		ListHead.upDateCounters(kSets, m_instances);
		kSets = ListHead.deleteItemSets(kSets, necSupport, necMaxSupport);

		int size = kSets.size();
		ListHead[] head = (ListHead[]) kSets.toArray();
		kSets = null;
		for (int j = 0; j < size; j++) {
			for (int k = j + 1; k < size; k++) {
				int nj = head[j].count;
				int nk = head[k].count;
				if (nj < nk) { // swap j,k
					byte tempa = head[j].attr;
					byte tempv = head[j].value;
					head[j].attr = head[k].attr;
					head[j].value = head[k].value;
					head[j].count = nk;
					head[k].attr = tempa;
					head[k].value = tempv;
					head[k].count = nj;
				}
			}
		}

		FPtree fp = buildProTree(m_instances, head);
		// FastVector Iset=mineProTree(fp,head,fp.root);
		// newTnodeToItemSets(Iset);

	}

	private void findCarLargeItemSetFP() throws Exception {
		FastVector kSets, kMinusOneSets;
		FastVector Iset = new FastVector();
		int total = m_instances.numInstances();
		int necSupport, necMaxSupport;
		double[] supB = new double[(m_onlyClass.attribute(0)).numValues()];
		double nextMinSupport = m_minSupport * (double) m_instances.numInstances();
		double nextMaxSupport = m_upperBoundMinSupport * (double) m_instances.numInstances();
		if ((double) Math.rint(nextMinSupport) == nextMinSupport) {
			necSupport = (int) nextMinSupport;
		} else {
			necSupport = Math.round((float) (nextMinSupport + 0.5));
		}
		if ((double) Math.rint(nextMaxSupport) == nextMaxSupport) {
			necMaxSupport = (int) nextMaxSupport;
		} else {
			necMaxSupport = Math.round((float) (nextMaxSupport + 0.5));
		}

		// find item sets of length one
		setCarhashtable();

		FastVector ksets = LabeledItemSet.newsingletons(m_instances);
		LabeledItemSet.newupDateCounters(ksets, m_instances);

		ksets = LabeledItemSet.deleteItemSets(ksets, necSupport, necMaxSupport);
		int ksize = ksets.size();
		for (int j = 0; j < ksize; j++) {
			for (int k = j + 1; k < ksize; k++) {
				int nj = ((LabeledItemSet) ksets.elementAt(j)).m_ruleSupCounter;
				int nk = ((LabeledItemSet) ksets.elementAt(k)).m_ruleSupCounter;
				if (nj < nk) {
					ksets.swap(j, k);
				}
			}
		}
		FastVector Head = new FastVector();
		for (int j = 0; j < ksets.size(); j++) { // biao L
			LabeledItemSet is = (LabeledItemSet) ksets.elementAt(j);
			LabelListHead ln = new LabelListHead(is);
			Head.addElement(ln);
		}
		CarFPtree ffp = buildCARTree(m_instances, Head);
		FastVector IsetI = mineCarTree(ffp, Head, ffp.root);
		int IsizeI = IsetI.size();
		for (int j = 0; j < IsizeI; j++) {
			LabeledItemSet node = (LabeledItemSet) IsetI.elementAt(j);
			rehash(node);
		}
		IsetI = null;
		kSets = LabeledItemSet.singletons(m_instances, m_onlyClass);
		LabeledItemSet.upDateCounters(kSets, m_instances, m_onlyClass);
		kSets = LabeledItemSet.deleteItemSets(kSets, necSupport, necMaxSupport);

		// %%%%%%%%%%%%%%%%%%%%%%%%%
		long tt1 = System.currentTimeMillis();
		int size = kSets.size();
		for (int kk = 0; kk < (m_onlyClass.attribute(0)).numValues(); kk++) {
			FastVector nksets = new FastVector();
			LabeledItemSet lis = (LabeledItemSet) kSets.elementAt(0);
			if (lis.m_classLabel == kk) {
				nksets.addElement(lis);
			}
			for (int j = 1; j < size; j++) {
				lis = (LabeledItemSet) kSets.elementAt(j);
				if (lis.m_classLabel == kk) {
					int flag = 0;
					for (int k = 0; k < nksets.size(); k++) {
						LabeledItemSet temp = (LabeledItemSet) nksets.elementAt(k);
						int tt = temp.m_ruleSupCounter;
						if (tt < lis.m_ruleSupCounter) {
							nksets.insertElementAt(lis, k);
							flag = 1;
							break;
						}
					}
					if (flag == 0)
						nksets.addElement(lis);
				}

			}
			Instances newinstances = new Instances(m_instances);
			newinstances.delete();
			int newsize = 0;
			for (int i = 0; i < total; i++) {
				int classlabel = (int) (m_onlyClass.instance(i).value(0));
				if (classlabel == kk) {
					Instance ins = m_instances.instance(i);
					newinstances.add(ins);
					newsize++;
				}
			}
			supB[kk] = (double) newsize / (double) total;
			FastVector head = new FastVector();
			for (int j = 0; j < nksets.size(); j++) { // biao L
				LabeledItemSet is = (LabeledItemSet) nksets.elementAt(j);
				LabelListHead ln = new LabelListHead(is);
				head.addElement(ln);
			}
			CarFPtree fp = buildCARTree(newinstances, head);

			FastVector IsetIset = mineCarTree(fp, head, fp.root);

			// int sizeII = IsetIset.size();
			// for (int i = 0; i < sizeII; i++){
			// LabeledItemSet node = (LabeledItemSet)IsetIset.elementAt(i);
			// rehash(node);
			// }
			Iset.appendElements(IsetIset);

		}
		long tt2 = System.currentTimeMillis();
		long tt = tt2 - tt1;
		tt = tt / 1000;

		int totalnum = m_instances.numAttributes();
		for (int i = 0; i < totalnum; i++) {
			FastVector Ksets = new FastVector();
			m_Ls.addElement(Ksets);
		}
		int sizeI = Iset.size();
		for (int i = 0; i < sizeI; i++) {

			LabeledItemSet kset = (LabeledItemSet) Iset.elementAt(i);
			ItemSet is = new ItemSet(kset.m_totalTransactions);
			is.m_items = kset.m_items;
			Integer II = (Integer) m_carhashtable.get(is);
			// if (ii == null){
			// continue;
			// }
			int count = II.intValue();
			kset.m_counter = count;

			int index = kset.size() - 1;
			FastVector KSets = (FastVector) m_Ls.elementAt(index);
			int clas = kset.m_classLabel;
			double conf = (double) kset.m_ruleSupCounter / (double) count;
			if (conf == 1)
				conf = 0.999;
			double cov = (1 - supB[clas]) / (1 - conf);
			if (cov > 1) {
				KSets.addElement(kset);
			}

		}
		m_carhashtable = null;
	}

	/**
	 * @param head
	 * @param crtree
	 *            通过FPGrowth算法生成所有的规则并将规则插入到crtree中，
	 *            在将规则插入到crtree中之前对其进行裁剪并使用DBCover进行处理
	 * 
	 */
	public FastVector[] generateRules(FastVector head) {
		FastVector[] result = new FastVector[6];

		return result;
	}

	/**
	 * 该函数生成了所有规则
	 * 
	 * @param instances
	 * @param onlyClass
	 * @param min
	 * @param max
	 * @param minMetric
	 * @return
	 * @throws Exception
	 */
	public FastVector[] findCarLargeItemSetFPTree(Instances instances, Instances onlyClass, double min, double max,
			double minMetric) throws Exception {
		FastVector kSets, kMinusOneSets;
		FastVector Iset = new FastVector();
		FastVector[] allTheRules = new FastVector[4];
		allTheRules[0] = new FastVector();
		allTheRules[1] = new FastVector();
		allTheRules[2] = new FastVector();
		allTheRules[3] = new FastVector();

		m_instances = instances;
		m_onlyClass = onlyClass;
		m_minSupport = min;
		m_upperBoundMinSupport = max;
		m_minMetric = minMetric;
		int total = m_instances.numInstances();
		int necSupport, necMaxSupport;
		double[] supB = new double[(m_onlyClass.attribute(0)).numValues()];
		double nextMinSupport = m_minSupport * (double) m_instances.numInstances();
		double nextMaxSupport = m_upperBoundMinSupport * (double) m_instances.numInstances();
		if ((double) Math.rint(nextMinSupport) == nextMinSupport) {
			necSupport = (int) nextMinSupport;
		} else {
			necSupport = Math.round((float) (nextMinSupport + 0.5));
		}
		if ((double) Math.rint(nextMaxSupport) == nextMaxSupport) {
			necMaxSupport = (int) nextMaxSupport;
		} else {
			necMaxSupport = Math.round((float) (nextMaxSupport + 0.5));
		}

		setCarhashtable();

		// FastVector ksets = LabeledItemSet.newsingletons(m_instances);
		// LabeledItemSet.newupDateCounters(ksets,m_instances);
		// int ksize = ksets.size();
		// ksets = LabeledItemSet.deleteItemSets(ksets, necSupport,
		// necMaxSupport);
		// for(int j=0;j<ksize;j++){///////////////
		// for(int k=j+1;k<ksize;k++){
		// int nj=((LabeledItemSet)ksets.elementAt(j)).m_ruleSupCounter;
		// int nk=((LabeledItemSet)ksets.elementAt(k)).m_ruleSupCounter;
		// if(nj<nk){
		// ksets.swap(j,k);
		// }
		// }
		// } ///
		// FastVector Head=new FastVector();
		// for(int j=0;j<ksets.size();j++){ //biao L
		// LabeledItemSet is=(LabeledItemSet)ksets.elementAt(j);
		// LabelListHead ln=new LabelListHead(is);
		// Head.addElement(ln);
		// }
		// CarFPtree ffp = buildCARTree(m_instances,Head);
		// FastVector IsetI = mineCarTree(ffp,Head,ffp.root);
		// int IsizeI = IsetI.size();
		// for (int j = 0; j < IsizeI; j++){
		// LabeledItemSet node = (LabeledItemSet)IsetI.elementAt(j);
		// rehash(node);
		// }
		//

		int ii = 0;
		Hashtable hashtable;
		kSets = AprioriItemSet.singletons(m_instances);
		AprioriItemSet.upDateCounters(kSets, m_instances);
		kSets = AprioriItemSet.deleteItemSets(kSets, necSupport, necMaxSupport);
		if (kSets.size() == 0)
			return null;
		do {
			int IsizeI = kSets.size();
			for (int j = 0; j < IsizeI; j++) {
				ItemSet node = (ItemSet) kSets.elementAt(j);
				newrehash(node);
			}
			kMinusOneSets = kSets;
			kSets = AprioriItemSet.mergeAllItemSets(kMinusOneSets, ii, m_instances.numInstances());
			hashtable = AprioriItemSet.getHashtable(kMinusOneSets, kMinusOneSets.size());
			kSets = AprioriItemSet.pruneItemSets(kSets, hashtable);
			AprioriItemSet.upDateCounters(kSets, m_instances);
			kSets = AprioriItemSet.deleteItemSets(kSets, necSupport, necMaxSupport);
			ii++;

		} while (kSets.size() > 0);

		kSets = LabeledItemSet.singletons(m_instances, m_onlyClass);
		LabeledItemSet.upDateCounters(kSets, m_instances, m_onlyClass);

		// check if a item set of length one is frequent, if not delete it
		kSets = LabeledItemSet.deleteItemSets(kSets, necSupport, necMaxSupport);
		// m_Ls.addElement(kSets);
		// kMinusOneSets=kSets;
		// %%%%%%%%%%%%%%%%%%%%%%%%%
		long tt1 = System.currentTimeMillis();
		int size = kSets.size();
		for (int kk = 0; kk < (m_onlyClass.attribute(0)).numValues(); kk++) {
			FastVector nksets = new FastVector();
			LabeledItemSet lis = (LabeledItemSet) kSets.elementAt(0);
			if (lis.m_classLabel == kk) {
				nksets.addElement(lis);
			}
			for (int j = 1; j < size; j++) {
				lis = (LabeledItemSet) kSets.elementAt(j);
				if (lis.m_classLabel == kk) {
					int flag = 0;
					for (int k = 0; k < nksets.size(); k++) {
						LabeledItemSet temp = (LabeledItemSet) nksets.elementAt(k);
						int tt = temp.m_ruleSupCounter;
						if (tt < lis.m_ruleSupCounter) {
							nksets.insertElementAt(lis, k);
							flag = 1;
							break;
						}
					}
					if (flag == 0)
						nksets.addElement(lis);
				}
			}
			Instances newinstances = new Instances(m_instances);
			newinstances.delete();
			int newsize = 0;
			for (int i = 0; i < total; i++) {
				int classlabel = (int) (m_onlyClass.instance(i).value(0));
				if (classlabel == kk) {
					Instance ins = m_instances.instance(i);
					newinstances.add(ins);
					newsize++;
				}
			}
			supB[kk] = (float) newsize / total;
			FastVector head = new FastVector();
			for (int j = 0; j < nksets.size(); j++) { // biao L
				LabeledItemSet is = (LabeledItemSet) nksets.elementAt(j);
				LabelListHead ln = new LabelListHead(is);
				head.addElement(ln);
			}
			CarFPtree fp = buildCARTree(newinstances, head);

			FastVector IsetIset = mineCarTree(fp, head, fp.root);

			// int sizeII = IsetIset.size();
			// for (int i = 0; i < sizeII; i++){
			// LabeledItemSet node = (LabeledItemSet)IsetIset.elementAt(i);
			// rehash(node);
			// }
			Iset.appendElements(IsetIset);
		}
		long tt2 = System.currentTimeMillis();
		long tt = tt2 - tt1;
		tt = tt / 1000;

		int totalnum = m_instances.numAttributes();

		int sizeI = Iset.size();
		for (int i = 0; i < sizeI; i++) {

			LabeledItemSet kset = (LabeledItemSet) Iset.elementAt(i);
			ItemSet is = new ItemSet(kset.m_totalTransactions);
			is.m_items = kset.m_items;
			Integer II = (Integer) m_carhashtable.get(is);
			int count = II.intValue();
			kset.m_counter = count;

			int clas = kset.m_classLabel;
			double conf = (double) kset.m_ruleSupCounter / (double) count;
			if (conf == 1)
				conf = 0.999;
			double cov = (1 - supB[clas]) / (1 - conf);
			if (cov > 1) {
				FastVector[] rules = kset.newgenerateRules(conf, cov);
				allTheRules[0].addElement(rules[0].elementAt(0));
				allTheRules[1].addElement(rules[1].elementAt(0));
				allTheRules[2].addElement(rules[2].elementAt(0));
				allTheRules[3].addElement(rules[3].elementAt(0));
			}

		}

		return allTheRules;
	}

	// public FastVector[] newCMAR(Instances instances,Instances
	// onlyClass,double min,double max,double minMetric) throws Exception{
	// *************
	// 添加变量
	// *************
	FastVector[] rules = new FastVector[7];

	public FastVector[] newCMAR(Instances instances, Instances onlyClass, double min, double max, double minMetric)
			throws Exception {
		for (int i = 0; i < 7; i++) {
			rules[i] = new FastVector();
		}

		m_onlyClass = onlyClass;
		m_minSupport = min;
		m_upperBoundMinSupport = max;
		m_instances = instances;
		FastVector kSets;
		FastVector Iset = new FastVector();

		int total = m_instances.numInstances();
		int necSupport, necMaxSupport;
		int totalnum = m_instances.numAttributes();
		int numClass = (m_onlyClass.attribute(0)).numValues();

		double nextMinSupport = m_minSupport * (double) m_instances.numInstances();
		double nextMaxSupport = m_upperBoundMinSupport * (double) m_instances.numInstances();
		if ((double) Math.rint(nextMinSupport) == nextMinSupport) {
			necSupport = (int) nextMinSupport;
		} else {
			necSupport = Math.round((float) (nextMinSupport + 0.5));
		}
		if ((double) Math.rint(nextMaxSupport) == nextMaxSupport) {
			necMaxSupport = (int) nextMaxSupport;
		} else {
			necMaxSupport = Math.round((float) (nextMaxSupport + 0.5));
		}

		kSets = ListHead.singleton(m_instances);
		ListHead.upDateCounters(kSets, m_instances, m_onlyClass);
		kSets = ListHead.deleteItemSets(kSets, necSupport, necMaxSupport);

		int size = kSets.size();

		for (int j = 0; j < size; j++) {
			for (int k = j + 1; k < size; k++) {
				int nj = ((ListHead) kSets.elementAt(j)).count;
				int nk = ((ListHead) kSets.elementAt(k)).count;
				if (nj < nk) {
					kSets.swap(j, k);
				}
			}
		}
		CMARtree fp = buildCMARtree(m_instances, m_onlyClass, kSets);
		double[] sup = getSupB();
		RuleNode ruleroot = new RuleNode();
		LinkedList<TNode> alpha = new LinkedList<TNode>();
		m_numRules = 0;
		m_Rules = new LinkedList();
		int[] countC = new int[totalnum];
		File f = new File("result.dat");
		f.delete();
		mineCMARtree(fp, kSets, alpha, necSupport, necMaxSupport, ruleroot, sup, countC);
		// for (int i = 0; i < totalnum; i++){
		// System.out.println(i+": "+countC[i]);
		// }

		// return m_Rules;
		return rules;
	}

	public void CMAR() throws Exception {
		FastVector kSets;
		FastVector Iset = new FastVector();

		int total = m_instances.numInstances();
		int necSupport, necMaxSupport;
		int totalnum = m_instances.numAttributes();
		int numClass = (m_onlyClass.attribute(0)).numValues();

		double nextMinSupport = m_minSupport * (double) m_instances.numInstances();
		double nextMaxSupport = m_upperBoundMinSupport * (double) m_instances.numInstances();
		if ((double) Math.rint(nextMinSupport) == nextMinSupport) {
			necSupport = (int) nextMinSupport;
		} else {
			necSupport = Math.round((float) (nextMinSupport + 0.5));
		}
		if ((double) Math.rint(nextMaxSupport) == nextMaxSupport) {
			necMaxSupport = (int) nextMaxSupport;
		} else {
			necMaxSupport = Math.round((float) (nextMaxSupport + 0.5));
		}

		kSets = ListHead.singleton(m_instances);
		ListHead.upDateCounters(kSets, m_instances, m_onlyClass);
		kSets = ListHead.deleteItemSets(kSets, necSupport, necMaxSupport);

		int size = kSets.size();

		for (int j = 0; j < size; j++) {
			for (int k = j + 1; k < size; k++) {
				int nj = ((ListHead) kSets.elementAt(j)).count;
				int nk = ((ListHead) kSets.elementAt(k)).count;
				if (nj < nk) {
					kSets.swap(j, k);
				}
			}
		}
		CMARtree fp = buildCMARtree(m_instances, m_onlyClass, kSets);
		double[] sup = getSupB();
		RuleNodeII ruleroot = new RuleNodeII();
		LinkedList<TNode> alpha = new LinkedList<TNode>();
		m_numRules = 0;
		numRules = 0;
		int[] countC = new int[totalnum];
		mineCMARtree(fp, kSets, alpha, necSupport, necMaxSupport, ruleroot, sup, countC);
		for (int i = 0; i < totalnum; i++) {
			System.out.println(i + ":           " + countC[i]);
		}
		int[] nodecount = new int[3];
		searchTree(ruleroot, nodecount);
		System.out.println("total node number:   " + nodecount[0]);
		System.out.println("total result node number:   " + nodecount[1]);
		System.out.println("total result rule number:   " + nodecount[2]);
	}

	private void searchTree(RuleNode root, int[] count) {
		count[0]++;
		if (root.rule.size() > 0) {
			count[1]++;
			count[2] += root.rule.size();
		}
		if (!root.child.isEmpty()) {
			ListIterator<RuleNode> ch = root.child.listIterator();
			while (ch.hasNext()) {
				RuleNode chnode = ch.next();
				searchTree(chnode, count);
			}
		}
	}

	private void searchTree(RuleNodeII root, int[] count) {
		count[0]++;
		if (root.rulenum > 0) {
			count[1]++;
			count[2] += root.rulenum;
		}
		if (root.chnum > 0) {
			for (int i = 0; i < root.chnum; i++) {
				searchTree(root.child[i], count);
			}
		}
	}

	private LinkedList<RuleItems> sort(LinkedList<RuleItems> rules) {
		LinkedList<RuleItems> result = new LinkedList<RuleItems>();

		while (!rules.isEmpty()) {
			RuleItems firstrule = rules.poll();
			ListIterator<RuleItems> seciter = rules.listIterator();
			while (seciter.hasNext()) {
				RuleItems secrule = seciter.next();
				if (secrule.conv > firstrule.conv) {
					double tempconv = firstrule.conv;
					firstrule.conv = secrule.conv;
					secrule.conv = tempconv;
					byte[] temp = firstrule.m_items;
					firstrule.m_items = secrule.m_items;
					secrule.m_items = temp;
				}
			}
			result.add(firstrule);
		}
		return result;
	}

	private CMARtree buildCMARtree(Instances instances, Instances OnlyClass, FastVector head) throws Exception {
		int num = instances.numAttributes();
		int total = instances.numInstances();
		int numClass = OnlyClass.attribute(0).numValues();

		CMARtree fp = new CMARtree(numClass);
		for (int ii = 0; ii < total; ii++) {
			TNode t = fp.root;
			int classlabel = (int) OnlyClass.instance(ii).value(0);
			int[] sup = new int[numClass];
			for (int i = 0; i < numClass; i++) {
				if (i == classlabel) {
					sup[i] = 1;
				} else {
					sup[i] = 0;
				}
			}
			for (int j = 0; j < head.size(); j++) {
				ListHead set = (ListHead) head.elementAt(j);
				if (set.containedBy(instances.instance(ii))) {
					TNode tt = new TNode(set.attr, set.value);
					tt.m_counter = 1;
					tt.sup = new int[numClass];
					for (int jj = 0; jj < sup.length; jj++) {
						tt.sup[jj] = sup[jj];
					}
					if (t.child.size() == 0) {
						t.addChild(tt);
						t = tt;
						set.addNext(tt);
					} else {
						int flag = 1;
						ListIterator<TNode> nodeiter = t.child.listIterator();
						while (nodeiter.hasNext()) {
							TNode node = nodeiter.next();
							if (set.equal(node)) {
								flag = 0;
								t = node;
								t.m_counter++;
								t.sup[classlabel]++;
								break;
							}
						}

						if (flag > 0) {
							t.addChild(tt);
							t = tt;
							set.addNext(tt);
						}

					}
				}
			}
			sup = null;
		}
		return fp;
	}

	// ***********************************************************
	// 针对多棵树
	// ***********************************************************
	private CMARtree buildCMARtree(Instances instances, Instances OnlyClass, FastVector head, int cl) throws Exception {
		int num = instances.numAttributes();
		int total = instances.numInstances();
		int numClass = 1;

		CMARtree fp = new CMARtree(numClass);
		for (int ii = 0; ii < total; ii++) {
			TNode t = fp.root;
			int classlabel = (int) OnlyClass.instance(ii).value(0);
			int[] sup = new int[numClass];
			// for ( int i = 0; i < numClass; i++){
			// if (i == classlabel){
			// sup[cl] = 1;
			// }
			// else{
			// sup[i] = 0;
			// }
			// }
			sup[0] = 1;
			for (int j = 0; j < head.size(); j++) {
				ListHead set = (ListHead) head.elementAt(j);
				if (set.containedBy(instances.instance(ii))) {
					TNode tt = new TNode(set.attr, set.value);
					tt.m_counter = 1;
					tt.sup = new int[numClass];
					for (int jj = 0; jj < sup.length; jj++) {
						tt.sup[jj] = sup[jj];
					}
					if (t.child.size() == 0) {
						t.addChild(tt);
						t = tt;
						set.addNext(tt);
					} else {
						int flag = 1;
						ListIterator<TNode> nodeiter = t.child.listIterator();
						while (nodeiter.hasNext()) {
							TNode node = nodeiter.next();
							if (set.equal(node)) {
								flag = 0;
								t = node;
								t.m_counter++;
								t.sup[0]++;
								break;
							}
						}

						if (flag > 0) {
							t.addChild(tt);
							t = tt;
							set.addNext(tt);
						}

					}
				}
			}
			sup = null;
		}
		return fp;
	}

	// private CMARtree newbuildCMARtree(Instances instances,Instances
	// OnlyClass,FastVector head) throws Exception{
	// int num=instances.numAttributes();
	// int total=instances.numInstances();
	// int numClass = OnlyClass.attribute(0).numValues();
	// CMARtree fp=new CMARtree(num,total,numClass);
	// for (int ii = 0; ii < total; ii++) {
	// LabelTnodeII t=fp.root;
	// int classlabel = (int)OnlyClass.instance(ii).value(0);
	// int[] sup = new int[numClass];
	// for ( int i = 0; i < numClass; i++){
	// if (i == classlabel){
	// sup[i] = 1;
	// }
	// else{
	// sup[i] = 0;
	// }
	// }
	// for(int j = 0;j < head.size(); j++){
	// ListHead set=(ListHead)head.elementAt(j);
	// if(set.containedBy(instances.instance(ii))){
	// LabelTnodeII tt=new LabelTnodeII();
	// tt.m_counter=1;
	//// tt.m_sup = new int[sup.length];
	//// for(int jj = 0; jj < sup.length; jj++)
	//// {
	//// tt.m_sup[jj] = sup[jj];
	//// }
	// tt.m_items = set.items;
	// if(t.chnum==0){
	// t.addChild(tt);
	// t=tt;
	// set.addNextII(tt,sup);
	// }
	// else{
	// int flag=1;
	// for(int jj=0;jj<t.chnum;jj++){
	// LabelTnodeII node = (LabelTnodeII)t.child.elementAt(jj);
	// if (node.m_items.equals(set.items)){
	// flag=0;
	// t=(LabelTnodeII)t.child.elementAt(jj);
	// t.m_counter++;
	//// t.m_sup[classlabel] ++;
	// set.sup[classlabel]++;
	// break;
	// }
	// }
	// if(flag>0){
	// t.addChild(tt);
	// t=tt;
	// set.addNextII(tt,sup);
	// }
	// }
	// }
	// }
	// if(t.m_sup == null){
	// t.m_sup = new int[numClass];
	// }
	// for(int i = 0; i < numClass; i++){
	// t.m_sup[i] += sup[i];
	// }
	// }
	// return fp;
	// }
	//
	private CarFPtree buildCARTree(Instances instances, FastVector head) throws Exception {
		int num = instances.numAttributes();
		int total = instances.numInstances();
		CarFPtree fp = new CarFPtree(num, total);
		for (int ii = 0; ii < instances.numInstances(); ii++) {
			LabelTnode t = fp.root;
			for (int j = 0; j < head.size(); j++) {
				LabelListHead set = (LabelListHead) head.elementAt(j);
				if (set.containedBy(instances.instance(ii))) {
					LabeledItemSet Is = set.is.copy();
					LabelTnode tt = new LabelTnode(Is);
					tt.value.m_ruleSupCounter = 1;
					if (t.chnum == 0) {
						t.addChild(tt);
						t = tt;
						set.addNext(tt);
					} else {
						int flag = 1;
						for (int jj = 0; jj < t.chnum; jj++) {
							if (t.child.get(jj).value.similar(Is)) {
								flag = 0;
								t = t.child.get(jj);
								t.value.m_ruleSupCounter++;
								break;
							}
						}
						if (flag > 0) {
							t.addChild(tt);
							t = tt;
							set.addNext(tt);
						}
					}
				}
			}
		}
		return fp;
	}

	private FPtree buildProTree(Instances instances, ListHead[] head) throws Exception {
		int num = instances.numInstances();
		FPtree fp = new FPtree();

		for (int ii = 0; ii < num; ii++) {
			TNode t = fp.root;
			for (int j = 0; j < head.length; j++) {
				ListHead set = head[j];
				if (set.containedBy(instances.instance(ii))) {
					TNode tt = new TNode(set.attr, set.value);
					if (t.child.size() == 0) {
						t.addChild(tt);
						t = tt;
						set.addNext(tt);
					} else {
						int flag = 1;
						TNode[] ch = (TNode[]) t.child.toArray();
						for (int jj = 0; jj < t.child.size(); jj++) {
							if (ch[j].equal(tt)) {
								flag = 0;
								t = ch[jj];
								t.m_counter++;
								break;
							}
						}
						if (flag > 0) {
							t.addChild(tt);
							t = tt;
							set.addNext(tt);
						}
						ch = null;
					}
				}
			}
			t = null;
		}
		// displayKsets(kSets);
		return fp;
	}

	private void rehash(LabeledItemSet t) {

		int total = t.m_totalTransactions;
		ItemSet is = new ItemSet(total);
		is.m_items = t.m_items;
		int count = t.m_ruleSupCounter;
		if (m_carhashtable.get(is) == null) {
			m_carhashtable.put(is, new Integer(count));
		} else {
			int totalcount = ((Integer) m_carhashtable.get(is)).intValue();
			totalcount += count;
			m_carhashtable.remove(is);
			m_carhashtable.put(is, new Integer(totalcount));
		}
	}

	private void newrehash(ItemSet t) {

		int total = t.m_totalTransactions;
		ItemSet is = new ItemSet(total);
		is.m_items = t.m_items;
		int count = t.m_counter;
		if (m_carhashtable.get(is) == null) {
			m_carhashtable.put(is, new Integer(count));
		}
	}

	private FastVector mineCarTree(CarFPtree fp, FastVector head, LabelTnode a) throws Exception {

		FastVector Result = new FastVector();
		FastVector plist = new FastVector();
		FastVector qlist = new FastVector();
		int i = 0;
		int necSupport, necMaxSupport;
		double nextMinSupport = m_minSupport * (double) m_instances.numInstances();
		double nextMaxSupport = m_upperBoundMinSupport * (double) m_instances.numInstances();
		if ((double) Math.rint(nextMinSupport) == nextMinSupport) {
			necSupport = (int) nextMinSupport;
		} else {
			necSupport = Math.round((float) (nextMinSupport + 0.5));
		}
		if ((double) Math.rint(nextMaxSupport) == nextMaxSupport) {
			necMaxSupport = (int) nextMaxSupport;
		} else {
			necMaxSupport = Math.round((float) (nextMaxSupport + 0.5));
		}

		////////
		// boolean hashflag = a.equal(fp.root);
		////////

		int len = m_instances.numAttributes();
		LabelTnode t0 = fp.root;
		int total = m_instances.numInstances();
		CarFPtree q = new CarFPtree(len, total);
		LabelTnode q0 = q.root;
		// m_text+="\n the number of children nodes:"+t0.chnum+t0.num;
		if (t0.chnum == 1) {
			FastVector single = new FastVector();
			LabelTnode t = t0.child.get(0);
			LabeledItemSet is = t.value;
			single.addElement(is);

			///// put into hashtable
			// rehash(t,hashflag);
			////////////////////

			i++;
			while (t.chnum == 1) {
				i++;
				t = t.child.get(0);
				single.addElement(t.value);
				// rehash(t,hashflag);
			}
			for (int j = 0; j < t.chnum; j++) {
				LabelTnode tc = t.child.get(j);
				q0.addChild(tc);
			}
			t.chnum = 0;
			t.child = null;
			// m_text+="\n........"+i+"\n";

			FastVector ksets = single;
			plist.appendElements(single);
			// Result.appendElements(ksets);
			for (int j = 0; j < i - 1; j++) {
				FastVector k1sets = new FastVector();
				k1sets = mergeLabelItemSets(ksets, j, total);
				if (k1sets == null)
					break;
				plist.appendElements(k1sets);
				ksets = k1sets;
			}

		} else {
			q0 = t0;
		}
		FastVector newhead = new FastVector();
		for (int j = i; j < head.size(); j++) {
			LabelListHead node = (LabelListHead) head.elementAt(j);
			if (node != null)
				newhead.addElement(node);
		}
		if (q0.chnum > 0) {
			for (int j = newhead.size() - 1; j >= 0; j--) {

				LabelTnode b = new LabelTnode();
				LabelListHead lj = (LabelListHead) newhead.elementAt(j);
				LabeledItemSet label = lj.is;
				b.value = label.copy();
				FastVector subList = new FastVector();

				FastVector list = new FastVector();
				for (int l = 0; l < lj.nextnum; l++) {
					FastVector tl = new FastVector();
					FastVector nextList = lj.next;
					LabelTnode tl0 = (LabelTnode) nextList.elementAt(l);
					LabelTnode t = tl0.father;
					while (t.father != null) {
						LabelTnode rt = new LabelTnode();
						rt.value = t.value.copy();
						rt.value.m_counter = 0;
						rt.value.m_ruleSupCounter = tl0.value.m_ruleSupCounter;
						tl.addElement(rt);
						t = t.father;
					}
					if (tl.size() > 0) {
						list.addElement(tl);
					}
				}

				FastVector CpTlist = new FastVector();
				if (list.size() == 0) {
					if (b != null) {
						qlist.addElement(b.value);
						// rehash(b,hashflag);
					}
					continue;
				}
				FastVector t00 = (FastVector) list.elementAt(0);
				int size0 = t00.size();
				for (int l = 0; l < size0; l++) {
					LabelTnode temp0 = (LabelTnode) t00.elementAt(l);
					if (temp0 != null) {
						LabeledItemSet set = temp0.value;
						LabelListHead ln = new LabelListHead(set);
						CpTlist.addElement(ln);
					}
				}
				int listsize = list.size();
				for (int l = 1; l < listsize; l++) {
					FastVector tl = (FastVector) list.elementAt(l);
					if (tl == null)
						continue;
					int sizetl = tl.size();
					for (int x = 0; x < sizetl; x++) {
						LabelTnode tempT = (LabelTnode) tl.elementAt(x);
						if (tempT == null)
							continue;
						int flag = 1;
						for (int xx = 0; xx < CpTlist.size(); xx++) {
							LabelListHead tempC = (LabelListHead) CpTlist.elementAt(xx);
							if (tempC.equal(tempT)) {
								tempC.is.m_ruleSupCounter += tempT.value.m_ruleSupCounter;
								flag = 0;
								break;
							}
						}
						if (flag == 1) {
							LabelListHead ln = new LabelListHead(tempT.value);
							CpTlist.addElement(ln);
						}
					}
				}
				if (necSupport > 0) {
					CpTlist = deleteLabelVector(necSupport, CpTlist);
				}
				if (CpTlist.size() == 0) {

					qlist.addElement(b.value);
					// rehash(b,hashflag);
					continue;
				}
				if (listsize == 1) {

					FastVector ksets = (FastVector) list.elementAt(0);
					FastVector kset = new FastVector();
					for (int index = 0; index < ksets.size(); index++) {
						LabeledItemSet iset = ((LabelTnode) ksets.elementAt(index)).value;
						subList.addElement(iset);
						kset.addElement(iset);
					}

					int deep = kset.size() - 1;
					for (int x = 0; x < deep; x++) {
						FastVector k1sets = new FastVector();
						k1sets = mergeLabelItemSets(kset, x, total);
						if (k1sets == null)
							break;
						subList.appendElements(k1sets);
						kset = k1sets;
					}

				} else {
					int CpTsize = CpTlist.size();
					for (int x = 0; x < CpTsize; x++) {
						LabelListHead tempC = (LabelListHead) CpTlist.elementAt(x);
						for (int xx = x + 1; xx < CpTsize; xx++) {
							LabelListHead tempD = (LabelListHead) CpTlist.elementAt(xx);
							if (tempD.is.m_ruleSupCounter > tempC.is.m_ruleSupCounter) {
								CpTlist.swap(x, xx);
							}
						}

					}
					// m_text+=necSupport+CpTlist.size()+"\n";

					// CarFPtree subTree = buildCarCondFPtree(list,CpTlist);
					// if(subTree.root.chnum>0){
					// subList=mineCarTree(subTree,CpTlist,b);
					// }
				}

				subList = b.mergeVector(subList);
				subList.addElement(b.value);
				qlist.appendElements(subList);

			}
		}
		Result.appendElements(plist);
		Result.appendElements(qlist);
		int sizep = plist.size();
		int sizeq = qlist.size();
		for (int j = 0; j < sizep; j++) {
			LabeledItemSet pt = (LabeledItemSet) plist.elementAt(j);
			for (int k = 0; k < sizeq; k++) {
				LabeledItemSet qt = (LabeledItemSet) qlist.elementAt(k);
				LabeledItemSet re = mergeLabelItemSet(pt, qt);
				if (re != null) {
					Result.addElement(re);
				}
			}
		}

		return Result;
	}

	public FastVector mergeLabelItemSets(FastVector itemSets, int size, int totalTrans) {

		FastVector newVector = new FastVector();
		LabelTnode tnode;
		LabeledItemSet result;
		int numFound, k;

		for (int i = 0; i < itemSets.size(); i++) {
			LabeledItemSet first = ((LabeledItemSet) itemSets.elementAt(i));
			out: for (int j = i + 1; j < itemSets.size(); j++) {
				LabeledItemSet second = ((LabeledItemSet) itemSets.elementAt(j));
				while (first.m_classLabel != second.m_classLabel) {
					j++;
					if (j == itemSets.size())
						break out;
					second = (LabeledItemSet) itemSets.elementAt(j);
				}
				result = new LabeledItemSet(totalTrans, first.m_classLabel);
				result.m_items = new int[first.m_items.length];

				// Find and copy common prefix of size 'size'
				numFound = 0;
				k = 0;
				while (numFound < size) {
					if (first.m_items[k] == second.m_items[k]) {
						if (first.m_items[k] != -1)
							numFound++;
						result.m_items[k] = first.m_items[k];
					} else
						continue out;
					k++;
				}

				// Check difference
				while (k < first.m_items.length) {
					if ((first.m_items[k] != -1) && (second.m_items[k] != -1))
						break;
					else {
						if (first.m_items[k] != -1)
							result.m_items[k] = first.m_items[k];
						else
							result.m_items[k] = second.m_items[k];
					}
					k++;
				}
				if (k == first.m_items.length) {
					if (first.m_ruleSupCounter < second.m_ruleSupCounter)
						result.m_ruleSupCounter = first.m_ruleSupCounter;
					else
						result.m_ruleSupCounter = second.m_ruleSupCounter;
					result.m_counter = 0;

					newVector.addElement(result);
				}
			}
		}

		return newVector;
	}

	private void newMerge(RuleNode root, FastVector ksets, LinkedList<TNode> alpha, int total, int itemsize,
			int supsize, int min, double[] supB, int[] countc) {
		// m_numRules = 0;
		int numClass = supsize;

		int len = ksets.size();
		int[][] list = new int[len][3 + supsize];
		FastVector tlist = new FastVector();
		int i = 0;
		for (i = 0; i < ksets.size(); i++) {
			ListHead t = (ListHead) ksets.elementAt(i);
			list[i][0] = t.attr;
			list[i][1] = t.value;
			list[i][2] = t.count;
			tlist.addElement(t.next.elementAt(0));
			boolean flag = true;
			for (int k = 0; k < supsize; k++) {
				list[i][3 + k] = t.sup[k];
				if (t.sup[k] <= min)
					continue;
				flag = false;
				m_numRules++;
				//////// panduan shifou conv > 1
				double conf = (double) t.sup[k] / (double) t.count;
				if (conf == 1)
					conf = 0.999;
				double convic = (1 - supB[k]) / (1 - conf);
				if (convic > 1) {
					countc[alpha.size()]++;
					byte[] items = new byte[itemsize + 1];
					for (int j = 0; j < itemsize; j++)
						items[j] = -1;
					items[t.attr] = t.value;
					items[itemsize] = (byte) k;
					ListIterator<TNode> nodeiter = alpha.listIterator();
					while (nodeiter.hasNext()) {
						TNode node = nodeiter.next();
						items[node.attr] = node.value;
						node = null;
					}
					RuleItems rule = new RuleItems(items, convic);

					// m_Ls.addElement(rule);
					// m_Rules.add(rule);
					// addRule(rule);
					addRule(rule, root);
					// ***********************************
					// 将规则添加到结果集中
					// ***********************************
					int[] arr = new int[items.length - 1];
					for (int g = 0; g < arr.length; g++) {
						arr[k] = items[g];
					}
					ItemSet iset = new ItemSet(arr);
					// if(!rules[0].contains(iset)){
					rules[0].addElement(iset);
					rules[1].addElement(new ItemSet(new int[] { k }));
					rules[2].addElement((double) t.sup[k]);
					rules[3].addElement(conf);
					rules[4].addElement(convic);
					
					double x2 = calculateX2((TNode)t.next.elementAt(0),supB);
					rules[5].addElement(x2);
					
					rules[6].addElement(t.count);
					// }
					// ****************************************

					numRules++;
				}

			}
			if (flag) {
				len = i + 1;
				break;
			}
		}

		if (len == 1)
			return;

		for (int count = 2; count <= len; count++) {
			int[] group = new int[count];
			for (i = 0; i < count; i++) {
				group[i] = i;
			}

			{
				int counter = list[group[count - 1]][2];

				for (int kk = 0; kk < supsize; kk++) {
					int supkk = list[group[count - 1]][3 + kk];
					if (supkk > min) {
						m_numRules++;
						double conf = (double) supkk / (double) counter;
						if (conf == 1)
							conf = 0.999;
						double conv = (1 - supB[kk]) / (1 - conf);
						if (conv > 1) {
							countc[alpha.size() + count - 1]++;
							byte[] items = new byte[itemsize + 1];
							for (int cc = 0; cc < itemsize; cc++)
								items[cc] = -1;
							for (int j = 0; j < group.length; j++) {
								int index = group[j];
								items[list[index][0]] = (byte) list[index][1];
							}
							items[itemsize] = (byte) kk;
							ListIterator<TNode> nodeiter = alpha.listIterator();
							while (nodeiter.hasNext()) {
								TNode node = nodeiter.next();
								items[node.attr] = node.value;
								node = null;
							}
							RuleItems rule = new RuleItems(items, conv);
							// m_Ls.addElement(rule);
							// addRule(rule);
							addRule(rule, root);

							// ***********************************
							// 将规则添加到结果集中
							// ***********************************
							int[] arr = new int[items.length - 1];
							for (int g = 0; g < arr.length; g++) {
								arr[g] = items[g];
							}
							ItemSet iset = new ItemSet(arr);
							// if(!rules[0].contains(iset)){
							rules[0].addElement(iset);
							rules[1].addElement(new ItemSet(new int[] { kk }));
							rules[2].addElement((double) supkk);
							rules[3].addElement(conf);
							rules[4].addElement(conv);
							double x2 = calculateX2(list[group[count - 1]],supB);
							rules[5].addElement(x2);
							
							rules[6].addElement(list[group[count - 1]][2]);
							// }
							// ****************************************
							numRules++;
						}
					}
				}
			}
			while (true) {
				int Maxi = -1;
				for (int j = 0; j < count; j++) {
					if (group[j] < (len - count + j)) {
						if (group[j] > Maxi)
							Maxi = j;
					}
				}
				if (Maxi == -1)
					break;
				group[Maxi]++;
				for (int j = Maxi + 1; j < count; j++) {
					group[j] = group[j - 1] + 1;
				}
				{
					int counter = list[group[count - 1]][2];

					for (int kk = 0; kk < supsize; kk++) {
						int supkk = list[group[count - 1]][3 + kk];
						if (supkk > min) {
							m_numRules++;
							double conf = (double) supkk / (double) counter;
							if (conf == 1)
								conf = 0.999;
							double conv = (1 - supB[kk]) / (1 - conf);
							if (conv > 1) {
								countc[alpha.size() + count - 1]++;
								byte[] items = new byte[itemsize + 1];
								for (int cc = 0; cc < itemsize; cc++)
									items[cc] = -1;
								for (int j = 0; j < group.length; j++) {
									int index = group[j];
									items[list[index][0]] = (byte) list[index][1];
								}
								items[itemsize] = (byte) kk;
								ListIterator<TNode> nodeiter = alpha.listIterator();
								while (nodeiter.hasNext()) {
									TNode node = nodeiter.next();
									items[node.attr] = node.value;
									node = null;
								}
								RuleItems rule = new RuleItems(items, conv);
								// m_Ls.addElement(rule);
								// addRule(rule);
								addRule(rule, root);

								// ***********************************
								// 将规则添加到结果集中
								// ***********************************
								int[] arr = new int[items.length - 1];
								for (int g = 0; g < arr.length; g++) {
									arr[g] = items[g];
								}
								ItemSet iset = new ItemSet(arr);
//								if (!rules[0].contains(iset)) {
									rules[0].addElement(iset);
									rules[1].addElement(new ItemSet(new int[] { kk }));
									rules[2].addElement((double) supkk);
									rules[3].addElement(conf);
									rules[4].addElement(conv);
									double x2 = calculateX2(list[group[count - 1]],supB);
									rules[5].addElement(x2);
									
									rules[6].addElement(list[group[count - 1]][2]);
//								}
								// ****************************************
								numRules++;
							}
						}
					}
				}
			}
		}

		return;
	}

	private void newMerge(RuleNodeII root, FastVector ksets, LinkedList<TNode> alpha, int total, int itemsize,
			int supsize, int min, double[] supB, int[] countc) {
		// m_numRules = 0;
		int numClass = supsize;

		int len = ksets.size();
		int[][] list = new int[len][3 + supsize];
		int i = 0;
		for (i = 0; i < ksets.size(); i++) {
			ListHead t = (ListHead) ksets.elementAt(i);
			list[i][0] = t.attr;
			list[i][1] = t.value;
			list[i][2] = t.count;
			boolean flag = true;
			for (int k = 0; k < supsize; k++) {
				list[i][3 + k] = t.sup[k];
				if (t.sup[k] <= min)
					continue;
				flag = false;
				m_numRules++;
				//////// panduan shifou conv > 1
				double conf = (double) t.sup[k] / (double) t.count;
				if (conf == 1)
					conf = 0.999;
				double convic = (1 - supB[k]) / (1 - conf);
				if (convic > 1) {
					countc[alpha.size()]++;
					byte[] items = new byte[itemsize + 1];
					for (int j = 0; j < itemsize; j++)
						items[j] = -1;
					items[t.attr] = t.value;
					items[itemsize] = (byte) k;
					ListIterator<TNode> nodeiter = alpha.listIterator();
					while (nodeiter.hasNext()) {
						TNode node = nodeiter.next();
						items[node.attr] = node.value;
						node = null;
					}
					RuleItems rule = new RuleItems(items, convic);
					// m_Ls.addElement(rule);
					// m_Rules.add(rule);
					// addRule(rule);
					// addRule(rule,root);
					numRules++;
				}

			}
			if (flag) {
				len = i + 1;
				break;
			}
		}

		if (len == 1)
			return;

		for (int count = 2; count <= len; count++) {
			int[] group = new int[count];
			for (i = 0; i < count; i++) {
				group[i] = i;
			}

			{
				int counter = list[group[count - 1]][2];

				for (int kk = 0; kk < supsize; kk++) {
					int supkk = list[group[count - 1]][3 + kk];
					if (supkk > min) {
						m_numRules++;
						double conf = (double) supkk / (double) counter;
						if (conf == 1)
							conf = 0.999;
						double conv = (1 - supB[kk]) / (1 - conf);
						if (conv > 1) {
							countc[alpha.size() + count - 1]++;
							byte[] items = new byte[itemsize + 1];
							for (int cc = 0; cc < itemsize; cc++)
								items[cc] = -1;
							for (int j = 0; j < group.length; j++) {
								int index = group[j];
								items[list[index][0]] = (byte) list[index][1];
							}
							items[itemsize] = (byte) kk;
							ListIterator<TNode> nodeiter = alpha.listIterator();
							while (nodeiter.hasNext()) {
								TNode node = nodeiter.next();
								items[node.attr] = node.value;
								node = null;
							}
							RuleItems rule = new RuleItems(items, conv);
							
							// m_Ls.addElement(rule);
							// addRule(rule);
							// addRule(rule,root);
							numRules++;
						}
					}
				}
			}
			while (true) {
				int Maxi = -1;
				for (int j = 0; j < count; j++) {
					if (group[j] < (len - count + j)) {
						if (group[j] > Maxi)
							Maxi = j;
					}
				}
				if (Maxi == -1)
					break;
				group[Maxi]++;
				for (int j = Maxi + 1; j < count; j++) {
					group[j] = group[j - 1] + 1;
				}
				{
					int counter = list[group[count - 1]][2];

					for (int kk = 0; kk < supsize; kk++) {
						int supkk = list[group[count - 1]][3 + kk];
						if (supkk > min) {
							m_numRules++;
							double conf = (double) supkk / (double) counter;
							if (conf == 1)
								conf = 0.999;
							double conv = (1 - supB[kk]) / (1 - conf);
							if (conv > 1) {
								countc[alpha.size() + count - 1]++;
								byte[] items = new byte[itemsize + 1];
								for (int cc = 0; cc < itemsize; cc++)
									items[cc] = -1;
								for (int j = 0; j < group.length; j++) {
									int index = group[j];
									items[list[index][0]] = (byte) list[index][1];
								}
								items[itemsize] = (byte) kk;
								ListIterator<TNode> nodeiter = alpha.listIterator();
								while (nodeiter.hasNext()) {
									TNode node = nodeiter.next();
									items[node.attr] = node.value;
									node = null;
								}
								RuleItems rule = new RuleItems(items, conv);
								// m_Ls.addElement(rule);
								// addRule(rule);
								// addRule(rule,root);
								numRules++;
							}
						}
					}
				}
			}
		}

		return;
	}

	private void addRule(RuleItems rule) {
		try {
			RandomAccessFile file = new RandomAccessFile("result.dat", "rw");
			long cur = file.length();
			file.seek(cur);
			file.write(rule.m_items);
			file.writeDouble(rule.conv);
			file.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void addRule(RuleItems rule, RuleNode root) {
		RuleNode node = root;
		byte i = 0;
		for (i = 0; i < rule.m_items.length - 1; i++) {
			if (rule.m_items[i] > -1) {
				if (node.child == null || node.child.isEmpty()) {
					RuleNode n = new RuleNode(i, rule.m_items[i]);
					node.addChild(n);
					node = n;
				} else {
					ListIterator<RuleNode> chiter = node.child.listIterator();
					boolean flag = true;
					while (chiter.hasNext()) {
						RuleNode ch = chiter.next();
						if (ch.attr == i && ch.value == rule.m_items[i]) {
							flag = false;
							node = ch;
							break;
						}
					}
					if (flag) {
						RuleNode n = new RuleNode(i, rule.m_items[i]);
						node.addChild(n);
						node = n;
					}

				}
			}
		}
		node.addRule(rule.m_items[i], rule.conv);
	}

	private void addRule(RuleItems rule, RuleNodeII root) {
		RuleNodeII node = root;
		byte i = 0;
		for (i = 0; i < rule.m_items.length - 1; i++) {
			if (rule.m_items[i] > -1) {
				if (node.child == null || node.child.length == 0) {
					RuleNodeII n = new RuleNodeII(i, rule.m_items[i]);
					node.addChild(n);
					node = n;
				} else {

					boolean flag = true;
					for (int j = 0; j < node.chnum; j++) {
						if (node.child[j].attr == i && node.child[j].value == rule.m_items[i]) {
							flag = false;
							node = node.child[j];
							break;
						}
					}
					if (flag) {
						RuleNodeII n = new RuleNodeII(i, rule.m_items[i]);
						node.addChild(n);
						node = n;
					}

				}
			}
		}
		node.addRule(rule.m_items[i], rule.conv);
	}

	private void newMergeII(RuleNode ruleroot, FastVector ksets, LinkedList<TNode> alpha, int total, int itemsize,
			int supsize, int min, double[] supB, int[] countc) {
		for (int i = 0; i < ksets.size(); i++) {
			///////////////////////////////////////////////// outer loop
			TNode subroot = (TNode) ksets.elementAt(i);
			RuleNode subruleroot = new RuleNode(subroot.attr, subroot.value);

			for (byte k = 0; k < subroot.sup.length; k++) {
				if (subroot.sup[k] >= min) {
					double conf = (double) subroot.sup[k] / (double) subroot.m_counter;
					if (conf == 1)
						conf = 0.999;
					double conv = (1 - supB[k]) / (1 - conf);
					if (conv > 1) {
						subruleroot.addRule(k, conv);
					}
				}
			}
			if (subruleroot.rule == null) {
				continue;
			}
			numRules += subruleroot.rule.size();
			ListIterator<RuleNode> ruleiter = ruleroot.child.listIterator();
			boolean flag = true;
			while (ruleiter.hasNext()) {
				RuleNode ch = ruleiter.next();
				if (ch.equal(subruleroot)) {
					ch.addRule(subruleroot);
					flag = false;
					subruleroot = ch;
					break;
				}
			}
			if (flag) {
				ruleroot.addChild(subruleroot);
			} ///// add the first top level node
			for (int j = i - 1; j >= 0; j--) { // subtree totally has i+1 level
				TNode t = (TNode) ksets.elementAt(j);
				RuleNode node = new RuleNode(t.attr, t.value);
				// for (int k = 0;)
			}
		}

		return;
	}

	public int getHashcode(byte a, byte v) {
		int result = 0;
		if (a < 0)
			return -1;
		for (int j = 0; j < a; j++) {
			result += m_instances.attribute(j).numValues();
		}
		result += v;
		return result;
	}

	public byte[] getItem(int code) {
		int hashcode = code;
		int len = m_instances.numAttributes();
		byte[] item = new byte[2];
		int i = 0;
		// int value = 0;
		for (i = 0; i < len; i++) {
			int num = m_instances.attribute(i).numValues();
			if (hashcode < num)
				break;
			else {
				hashcode -= num;
			}
		}
		item[0] = (byte) i;
		item[1] = (byte) hashcode;
		return item;

	}

	// 该函数需要进行测试，结果与预期结果不同
	private void mineCMARtree(CMARtree fp, FastVector head, LinkedList<TNode> alpha, int min, int max, RuleNode result,
			double[] supB, int[] countc) throws Exception {

		int i = 0;
		int len = m_instances.numAttributes();
		int total = m_instances.numInstances();
		int numClass = m_onlyClass.attribute(0).numValues();
		int numAttr = 0;
		for (int j = 0; j < len; j++) {
			numAttr += m_instances.attribute(j).numValues();
		}
		TNode t0 = fp.root;

		if (t0.child.size() == 1) { //////// is or not single path
			TNode t = (TNode) t0.child.get(0);
			i++;
			while (t.child.size() == 1) {
				i++;
				t = (TNode) t.child.get(0);
			}
		}
		if (i == head.size()) { // single path
			newMerge(result, head, alpha, total, len, numClass, min, supB, countc);
			return;
		}

		if (t0.child.size() > 0) {
			int newsize = head.size();
			for (int j = (newsize - 1); j >= 0; j--) {

				ListHead lj = (ListHead) head.elementAt(j);
				TNode b = new TNode(lj.attr, lj.value);
				b.sup = lj.sup;
				b.m_counter = lj.count;
				for (int cc = 0; cc < b.sup.length; cc++) {
					if (b.sup[cc] > min) {
						m_numRules++;
						double conf = (double) b.sup[cc] / (double) b.m_counter;
						if (conf == 1)
							conf = 0.999;
						double conv = (1 - supB[cc]) / (1 - conf);
						if (conv > 1) {
							countc[alpha.size()]++;
							byte[] items = new byte[len + 1];
							for (byte c = 0; c < len; c++) {
								items[c] = -1;
							}
							if (!alpha.isEmpty()) {
								ListIterator<TNode> nodeiter = alpha.listIterator();
								while (nodeiter.hasNext()) {
									TNode node = nodeiter.next();
									items[node.attr] = node.value;
								}
							}
							items[lj.attr] = lj.value;
							items[len] = (byte) cc;
							RuleItems rule = new RuleItems(items, conv);

							// ***********************************
							// 将规则添加到结果集中
							// ***********************************
							int[] arr = new int[items.length - 1];
							for (int k = 0; k < arr.length; k++) {
								arr[k] = items[k];
							}
							ItemSet iset = new ItemSet(arr);
							// if(!rules[0].contains(iset)){
							rules[0].addElement(iset);
							rules[1].addElement(new ItemSet(new int[] { cc }));
							rules[2].addElement((double) b.sup[cc]);
							rules[3].addElement(conf);
							rules[4].addElement(conv);
							double x2 = calculateX2(b,supB);
							rules[5].addElement(x2);
							rules[6].addElement(b.m_counter);
							// }
							// ****************************************
							// m_Ls.addElement(rule);
							// addRule(rule);
							addRule(rule, result);
							numRules++;
						}

					}
				}
				int[] table = new int[numAttr];
				FastVector list = new FastVector();
				int nextnum = lj.nextnum;

				for (int l = 0; l < nextnum; l++) {
					LabelItemSetII setl = new LabelItemSetII(total, numClass);
					FastVector nextList = lj.next;
					TNode tl0 = (TNode) nextList.elementAt(l);
					setl.m_sup = tl0.sup;
					setl.m_items = new int[len];

					for (int ll = 0; ll < setl.m_items.length; ll++) {
						setl.m_items[ll] = -1;
					}
					setl.m_counter = tl0.m_counter;
					TNode t = tl0.father;
					while (t.father != null) {
						int index = getHashcode(t.attr, t.value);
						if (index > -1)
							table[index] += tl0.m_counter;

						setl.m_items[t.attr] = t.value; // form Cond Pattens
						t = t.father;
					}
					if (setl.size() > 0) {
						list.addElement(setl);
					}
				}
				if (list.size() > 0) {

					FastVector CpTlist = new FastVector();// the list head of
															// new cond-patten
															// tree
					for (int cc = 0; cc < numAttr; cc++) {
						if (table[cc] > min) {
							byte[] av = getItem(cc);
							ListHead lh = new ListHead(table[cc], av[0], av[1]);
							lh.sup = new int[numClass];
							CpTlist.addElement(lh);

						}
					}
					table = null;
					if (CpTlist.size() == 0) {
						continue;
					}

					int CpTsize = CpTlist.size();
					for (int x = 0; x < CpTsize; x++) {
						for (int xx = x + 1; xx < CpTsize; xx++) {
							ListHead tempC = (ListHead) CpTlist.elementAt(x);
							ListHead tempD = (ListHead) CpTlist.elementAt(xx);
							if (tempD.count > tempC.count) {
								CpTlist.swap(x, xx);
							}
						}
					}

					///////// cond-Fp tree build and mine
					LinkedList<TNode> beta = new LinkedList<TNode>();
					beta.addAll(alpha);
					beta.add(b);
					CMARtree subTree = buildCFPtree(list, CpTlist);
					list = null;
					if (subTree.root.child.size() > 0) {
						mineCMARtree(subTree, CpTlist, beta, min, max, result, supB, countc);
					}
					if (alpha.isEmpty())
						System.out.println("numRules:" + numRules);
				}

			}
		}

		return;
	}
	
	//*************************************
	//计算卡方值
	//*************************************
	/**
	 * 用于计算卡方值（待测试）
	 * @param iset
	 * @param cl
	 * @param fp
	 * @param head
	 * @return
	 */
	public double calculateX2(TNode tnode,double[] supB){
		double x2 = 0;
        for(int i = 0 ; i < supB.length ; i++){
        	int Oij = tnode.sup[i];
        	double Eij = supB[i]*tnode.m_counter;
        	x2 += Math.pow((Oij - Eij),2) / Eij;
        }
        for(int i = 0 ; i < supB.length ; i++){
        	double Oij = supB[i]*m_onlyClass.numInstances() - tnode.sup[i];
        	double Eij = supB[i]*(m_onlyClass.numInstances() - tnode.m_counter);
        	x2 += Math.pow((Oij - Eij),2) / Eij;
        }
		return x2;
	}
	
	/**
	 * 重载之前的方法(待测试)
	 * @param tnode
	 * @param supB
	 * @return
	 */
	public double calculateX2(int[]arr,double[] supB){
		double x2 = 0;
        for(int i = 0 ; i < supB.length ; i++){
        	int Oij = arr[3+i];
        	double Eij = supB[i]*arr[2];
        	x2 += Math.pow((Oij - Eij),2) / Eij;
        }
        for(int i = 0 ; i < supB.length ; i++){
        	double Oij = supB[i]*m_onlyClass.numInstances() - arr[3+i];
        	double Eij = supB[i]*(m_onlyClass.numInstances() - arr[2]);
        	x2 += Math.pow((Oij - Eij),2) / Eij;
        }
		return x2;
	}
	
	
	private void mineCMARtree(CMARtree fp, FastVector head, LinkedList<TNode> alpha, int min, int max,
			RuleNodeII result, double[] supB, int[] countc) throws Exception {

		int necSupport, i = 0;
		necSupport = min; // minmum support
		int necMaxSupport = max;

		int len = m_instances.numAttributes();
		int total = m_instances.numInstances();
		int numClass = m_onlyClass.attribute(0).numValues();
		int numAttr = 0;
		for (int j = 0; j < len; j++) {
			numAttr += m_instances.attribute(j).numValues();
		}
		TNode t0 = fp.root;

		if (t0.child.size() == 1) { //////// is or not single path
			TNode t = (TNode) t0.child.get(0);
			i++;
			while (t.child.size() == 1) {
				i++;
				t = (TNode) t.child.get(0);
			}
		}
		if (i == head.size()) { // single path
			newMerge(result, head, alpha, total, len, numClass, min, supB, countc);
			return;
		}

		if (t0.child.size() > 0) {
			int newsize = head.size();
			for (int j = (newsize - 1); j >= 0; j--) {

				ListHead lj = (ListHead) head.elementAt(j);
				TNode b = new TNode(lj.attr, lj.value);
				b.sup = lj.sup;
				b.m_counter = lj.count;
				if (b.m_counter < min)
					continue;
				boolean nofit = true;
				for (int cc = 0; cc < b.sup.length; cc++) {
					if (b.sup[cc] > min) {
						nofit = false;
						m_numRules++;
						double conf = (double) b.sup[cc] / (double) b.m_counter;
						if (conf == 1)
							conf = 0.999;
						double conv = (1 - supB[cc]) / (1 - conf);
						if (conv > 1) {
							countc[alpha.size()]++;
							byte[] items = new byte[len + 1];
							for (byte c = 0; c < len; c++) {
								items[c] = -1;
							}
							if (!alpha.isEmpty()) {
								ListIterator<TNode> nodeiter = alpha.listIterator();
								while (nodeiter.hasNext()) {
									TNode node = nodeiter.next();
									items[node.attr] = node.value;
								}
							}
							items[lj.attr] = lj.value;
							items[len] = (byte) cc;
							RuleItems rule = new RuleItems(items, conv);
							// m_Ls.addElement(rule);
							// addRule(rule);
							addRule(rule, result);
							numRules++;
						}

					}
				}
				if (nofit)
					continue;
				int[] table = new int[numAttr];
				FastVector list = new FastVector();
				int nextnum = lj.nextnum;

				for (int l = 0; l < nextnum; l++) {
					LabelItemSetII setl = new LabelItemSetII(total, numClass);
					FastVector nextList = lj.next;
					TNode tl0 = (TNode) nextList.elementAt(l);
					setl.m_sup = tl0.sup;
					setl.m_items = new int[len];

					for (int ll = 0; ll < setl.m_items.length; ll++) {
						setl.m_items[ll] = -1;
					}
					setl.m_counter = tl0.m_counter;
					TNode t = tl0.father;
					while (t.father != null) {
						int index = getHashcode(t.attr, t.value);
						if (index > -1)
							table[index] += tl0.m_counter;

						setl.m_items[t.attr] = t.value; // form Cond Pattens
						t = t.father;
					}
					if (setl.size() > 0) {
						list.addElement(setl);
					}
				}
				if (list.size() > 0) {

					FastVector CpTlist = new FastVector();// the list head of
															// new cond-patten
															// tree
					for (int cc = 0; cc < numAttr; cc++) {
						if (table[cc] > min) {
							byte[] av = getItem(cc);
							ListHead lh = new ListHead(table[cc], av[0], av[1]);
							lh.sup = new int[numClass];
							CpTlist.addElement(lh);

						}
					}
					table = null;
					if (CpTlist.size() == 0) {
						continue;
					}

					int CpTsize = CpTlist.size();
					for (int x = 0; x < CpTsize; x++) {
						for (int xx = x + 1; xx < CpTsize; xx++) {
							ListHead tempC = (ListHead) CpTlist.elementAt(x);
							ListHead tempD = (ListHead) CpTlist.elementAt(xx);
							if (tempD.count > tempC.count) {
								CpTlist.swap(x, xx);
							}
						}
					}

					///////// cond-Fp tree build and mine
					LinkedList<TNode> beta = new LinkedList<TNode>();
					beta.addAll(alpha);
					beta.add(b);
					CMARtree subTree = buildCFPtree(list, CpTlist);
					list = null;
					if (subTree.root.child.size() > 0) {
						mineCMARtree(subTree, CpTlist, beta, min, max, result, supB, countc);
					}
					if (alpha.isEmpty())
						System.out.println("numRules:" + numRules);
				}

			}
		}

		return;
	}

	private void newmineCMARtree(CMARtree fp, FastVector head, LinkedList<TNode> alpha, int min, int max,
			RuleNode result, double[] supB, int[] countc) throws Exception {

		int necSupport, i = 0;
		necSupport = min; // minmum support
		int necMaxSupport = max;

		int len = m_instances.numAttributes();
		int total = m_instances.numInstances();
		int numClass = m_onlyClass.attribute(0).numValues();
		int numAttr = 0;
		for (int j = 0; j < len; j++) {
			numAttr += m_instances.attribute(j).numValues();
		}
		TNode t0 = fp.root;

		if (t0.child.size() == 1) { //////// is or not single path
			TNode t = (TNode) t0.child.get(0);
			i++;
			while (t.child.size() == 1) {
				i++;
				t = (TNode) t.child.get(0);
			}
		}
		if (i == head.size()) { // single path
			newMerge(result, head, alpha, total, len, numClass, min, supB, countc);
			return;
		}

		if (t0.child.size() > 0) {
			int newsize = head.size();
			for (int j = (newsize - 1); j >= 0; j--) {

				ListHead lj = (ListHead) head.elementAt(j);
				TNode b = new TNode(lj.attr, lj.value);
				b.sup = lj.sup;
				b.m_counter = lj.count;
				for (int cc = 0; cc < b.sup.length; cc++) {
					if (b.sup[cc] > min) {
						m_numRules++;
						double conf = (double) b.sup[cc] / (double) b.m_counter;
						if (conf == 1)
							conf = 0.999;
						double conv = (1 - supB[cc]) / (1 - conf);
						if (conv > 1) {
							countc[alpha.size()]++;
							byte[] items = new byte[len + 1];
							for (byte c = 0; c < len; c++) {
								items[c] = -1;
							}
							if (!alpha.isEmpty()) {
								ListIterator<TNode> nodeiter = alpha.listIterator();
								while (nodeiter.hasNext()) {
									TNode node = nodeiter.next();
									items[node.attr] = node.value;
								}
							}
							items[lj.attr] = lj.value;
							items[len] = (byte) cc;
							RuleItems rule = new RuleItems(items, conv);
							// m_Ls.addElement(rule);
							addRule(rule, result);
							numRules++;
						}

					}
				}
				int[] table = new int[numAttr];
				FastVector list = new FastVector();
				int nextnum = lj.nextnum;

				for (int l = 0; l < nextnum; l++) {
					LabelItemSetII setl = new LabelItemSetII(total, numClass);
					FastVector nextList = lj.next;
					TNode tl0 = (TNode) nextList.elementAt(l);
					setl.m_sup = tl0.sup;
					setl.m_items = new int[len];

					for (int ll = 0; ll < setl.m_items.length; ll++) {
						setl.m_items[ll] = -1;
					}
					setl.m_counter = tl0.m_counter;
					TNode t = tl0.father;
					while (t.father != null) {
						int index = getHashcode(t.attr, t.value);
						if (index > -1)
							table[index] += tl0.m_counter;

						setl.m_items[t.attr] = t.value; // form Cond Pattens
						t = t.father;
					}
					if (setl.size() > 0) {
						list.addElement(setl);
					}
				}
				if (list.size() > 0) {

					FastVector CpTlist = new FastVector();// the list head of
															// new cond-patten
															// tree
					for (int cc = 0; cc < numAttr; cc++) {
						if (table[cc] > min) {
							byte[] av = getItem(cc);
							ListHead lh = new ListHead(table[cc], av[0], av[1]);
							lh.sup = new int[numClass];
							CpTlist.addElement(lh);

						}
					}
					table = null;
					if (CpTlist.size() == 0) {
						continue;
					}

					int CpTsize = CpTlist.size();
					for (int x = 0; x < CpTsize; x++) {
						for (int xx = x + 1; xx < CpTsize; xx++) {
							ListHead tempC = (ListHead) CpTlist.elementAt(x);
							ListHead tempD = (ListHead) CpTlist.elementAt(xx);
							if (tempD.count > tempC.count) {
								CpTlist.swap(x, xx);
							}
						}
					}

					///////// cond-Fp tree build and mine
					LinkedList<TNode> beta = new LinkedList<TNode>();
					beta.addAll(alpha);
					beta.add(b);
					CMARtree subTree = buildCFPtree(list, CpTlist);
					list = null;
					if (subTree.root.child.size() > 0) {
						newmineCMARtree(subTree, CpTlist, beta, min, max, result, supB, countc);
					}
					if (alpha.isEmpty())
						System.out.println("numRules:" + numRules);
				}

			}
		}

		return;
	}

	/*
	 * private FastVector mineProTree(FPtree fp,ListHead[] head,TNode a) throws
	 * Exception{
	 * 
	 * FastVector Result=new FastVector(); FastVector plist=new FastVector();
	 * FastVector qlist=new FastVector(); int necSupport,necMaxSupport,i = 0;
	 * double nextMinSupport = m_minSupport*(double)m_instances.numInstances();
	 * double nextMaxSupport =
	 * m_upperBoundMinSupport*(double)m_instances.numInstances();
	 * if((double)Math.rint(nextMinSupport) == nextMinSupport){ necSupport =
	 * (int) nextMinSupport; } else{ necSupport =
	 * Math.round((float)(nextMinSupport+0.5)); }
	 * if((double)Math.rint(nextMaxSupport) == nextMaxSupport){ necMaxSupport =
	 * (int) nextMaxSupport; } else{ necMaxSupport =
	 * Math.round((float)(nextMaxSupport+0.5)); } int total =
	 * m_instances.numInstances(); int len=m_instances.numAttributes(); TNode
	 * t0=fp.root; FPtree q=new FPtree(); TNode q0=q.root;
	 * 
	 * if(t0.chnum==1){ FastVector single=new FastVector(); TNode
	 * t=t0.child.get(0); ItemSet is = new ItemSet(t,total);
	 * single.addElement(is); i++; while(t.chnum==1){ i++; t=t.child.get(0); is
	 * = new ItemSet(t,total); single.addElement(is); } for(int
	 * j=0;j<t.chnum;j++){ TNode tc=t.child.get(j); q0.addChild(tc); }
	 * t.chnum=0; t.child=null; t = null;
	 * 
	 * FastVector ksets=single; plist.appendElements(single); single = null;
	 * 
	 * for(int j=0;j<i-1;j++) { FastVector k1sets=new FastVector(); //
	 * k1sets=newTNodeMerge(ksets,j); k1sets =
	 * AprioriItemSet.mergeAllItemSets(ksets, j, total);
	 * plist.appendElements(k1sets); ksets=k1sets; } ksets = null; } else{
	 * q0=t0; }
	 * 
	 * if(q0.chnum>0){ for(int j = head.length-1; j >= i; j--){
	 * 
	 * ListHead lj = head[j]; TNode b=new TNode(lj.attr,lj.value); b.m_counter =
	 * lj.count; ItemSet s = new ItemSet(b,total); qlist.addElement(s); s =
	 * null; FastVector subList=new FastVector(); FastVector list=new
	 * FastVector();
	 * 
	 * for(int l=0;l<lj.nextnum;l++){ FastVector tl=new FastVector(); FastVector
	 * nextList= lj.next; TNode tl0=(TNode)nextList.elementAt(l); TNode
	 * t=tl0.father; while(t.father!=null){ TNode rt=new TNode(t); rt.m_counter
	 * = tl0.m_counter; tl.addElement(rt); t=t.father; } if(tl.size()>0){
	 * list.addElement(tl); } }
	 * 
	 * if(list.size()==0){ continue; } ArrayList<ListHead> CpTlist=new
	 * ArrayList<ListHead>(); FastVector t00=(FastVector)list.elementAt(0);
	 * for(int l=0;l<t00.size();l++){ TNode temp0=(TNode)t00.elementAt(l);
	 * ListHead ln=new ListHead(temp0.m_counter,temp0.attr,temp0.value);
	 * CpTlist.add(ln); } int listsize=list.size(); for(int l = 1;l < listsize;
	 * l++){ FastVector tl=(FastVector)list.elementAt(l); for(int
	 * x=0;x<tl.size();x++){ TNode tempT=(TNode)tl.elementAt(x); int flag=1;
	 * for(int xx = 0;xx < CpTlist.size();xx++){ ListHead
	 * tempC=(ListHead)CpTlist.get(xx); if(tempC.equal(tempT)){
	 * tempC.count+=tempT.m_counter; flag=0; break; } } if(flag == 1){ ListHead
	 * ln=new ListHead(tempT.m_counter,tempT.attr,tempT.value); CpTlist.add(ln);
	 * } } } if(necSupport>0){
	 * CpTlist=ListHead.deleteItemSets(CpTlist,necSupport,necMaxSupport); }
	 * if(CpTlist.size()==0){ continue; } ListHead[] cptHead =
	 * (ListHead[])CpTlist.toArray(); CpTlist = null; if(listsize == 1){
	 * FastVector ksets = new FastVector(); FastVector
	 * k_1sets=(FastVector)list.elementAt(0); for(int xx = 0; xx < ksets.size();
	 * xx++){ TNode node = (TNode)k_1sets.elementAt(xx); ItemSet is = new
	 * ItemSet(node,total); ksets.addElement(is); }
	 * subList.appendElements(ksets); int deep = ksets.size()-1; for(int x = 0;
	 * x < deep; x++){ FastVector k1sets=new FastVector(); k1sets =
	 * AprioriItemSet.mergeAllItemSets(ksets, x, total);
	 * subList.appendElements(k1sets); ksets=k1sets; } ksets = null;
	 * 
	 * } else{ int CpTsize=cptHead.length;
	 * 
	 * for(int x = 0;x < CpTsize; x++){ ListHead tempC=cptHead[x]; for(int
	 * xx=x+1;xx<CpTsize;xx++){ ListHead tempD=cptHead[xx];
	 * if(tempD.count>tempC.count){ int tempa = tempC.attr; int tempv =
	 * tempC.value; int tempcount = tempC.count; tempC.attr = tempD.attr;
	 * tempC.value = tempD.value; tempC.count = tempD.count; tempD.attr = tempa;
	 * tempD.value = tempv; tempD.count = tempcount; } }
	 * 
	 * }
	 * 
	 * FPtree subTree=buildCondFPtree(list,cptHead); if(subTree.root.chnum>0){
	 * subList=mineProTree(subTree,cptHead,b); } } b.mergeVector(subList);
	 * qlist.appendElements(subList); subList = null; } }
	 * Result.appendElements(plist); Result.appendElements(qlist); for(int
	 * j=0;j<plist.size();j++){ ItemSet pt=(ItemSet)plist.elementAt(j); for(int
	 * k=0;k<qlist.size();k++){ ItemSet qt=(ItemSet)qlist.elementAt(k); ItemSet
	 * re=mergeItemSet(pt,qt); Result.addElement(re); } } plist = null; qlist =
	 * null; return Result; }
	 * 
	 * private FastVector newmineProTree(FPtree fp,ListHead[] head,TNode a,int
	 * min,int max) throws Exception{
	 * 
	 * FastVector Result=new FastVector(); FastVector plist=new FastVector();
	 * FastVector qlist=new FastVector(); int necSupport,i = 0; necSupport =
	 * min; int necMaxSupport = max ; int total = m_instances.numInstances();
	 * int len=m_instances.numAttributes(); TNode t0=fp.root; FPtree q=new
	 * FPtree(); TNode q0=q.root;
	 * 
	 * if(t0.child.size()==1){ FastVector single=new FastVector();
	 * 
	 * TNode t=t0.child.get(0); ItemSet is = new ItemSet(t,total);
	 * single.addElement(is); i++; while(t.chnum==1){ i++; t=t.child.get(0);
	 * single.addElement(t); } for(int j=0;j<t.chnum;j++){ TNode
	 * tc=t.child.get(j); q0.addChild(tc); } t.chnum=0; t.child=null; t = null;
	 * 
	 * 
	 * FastVector ksets=single; plist.appendElements(single); single = null;
	 * 
	 * for(int j=0;j<i-1;j++) { FastVector k1sets=new FastVector();
	 * //k1sets=newTNodeMerge(ksets,j); k1sets =
	 * AprioriItemSet.mergeAllItemSets(ksets, j, total);
	 * plist.appendElements(k1sets); ksets=k1sets; } ksets = null; } else{
	 * q0=t0; }
	 * 
	 * if(q0.chnum>0){ int sizenew = head.length; for(int j=sizenew - 1; j >= i;
	 * j--){ ListHead lj=head[j]; TNode b=new TNode(lj.attr,lj.value);
	 * b.m_counter=lj.count; FastVector subList=new FastVector(); FastVector
	 * list=new FastVector(); setCarhashtable(); int nextnumber = lj.nextnum;
	 * for(int l = 0; l < nextnumber; l++){ FastVector tl=new FastVector();
	 * FastVector nextList=lj.next; TNode tl0=(TNode)nextList.elementAt(l);
	 * ///t10 mining TNode t=tl0.father;
	 * 
	 * while(t.father!=null){ TNode rt=new TNode(t); rt.m_counter =
	 * tl0.m_counter; tl.addElement(rt); rehashTnode(rt); t=t.father; }
	 * if(tl.size()>0){ list.addElement(tl); } }
	 * 
	 * int listsize = list.size(); if(listsize == 1){ FastVector ksets = new
	 * FastVector(); FastVector k_1sets=(FastVector)list.elementAt(0); for(int
	 * xx = 0; xx < ksets.size(); xx++){ TNode node =
	 * (TNode)k_1sets.elementAt(xx); ItemSet is = new ItemSet(node,total);
	 * ksets.addElement(is); } subList.appendElements(ksets); int deep =
	 * ksets.size()-1; for(int x = 0; x < deep; x++){ FastVector k1sets=new
	 * FastVector(); k1sets = AprioriItemSet.mergeAllItemSets(ksets, x, total);
	 * subList.appendElements(k1sets); ksets=k1sets; } ksets = null;
	 * 
	 * } else{ Enumeration enu = m_carhashtable.keys(); FastVector ksets = new
	 * FastVector(); FastVector toDelete = new FastVector();
	 * while(enu.hasMoreElements()){ ItemSet Is = (ItemSet)enu.nextElement();
	 * int sup = ((Integer)m_carhashtable.get(Is)).intValue(); if(sup >
	 * necSupport){ TNode node = new TNode(Is); node.m_counter = sup;
	 * ksets.addElement(node); } else{ AprioriItemSet Ais = new
	 * AprioriItemSet(m_instances.numInstances()); Ais.m_counter = sup;
	 * Ais.m_items = Is.m_items; toDelete.addElement(Ais); } }
	 * m_carhashtable.clear(); if(ksets.size() == 0) { subList.addElement(b);
	 * continue; } subList.appendElements(ksets);
	 * 
	 * for(int k= 0; k < listsize; k++){ ksets = new FastVector(); FastVector
	 * k_sets = (FastVector)list.elementAt(k); int k_size = k_sets.size();
	 * if(toDelete.size() > 0){ for (int kk = 0; kk < k_size; kk++){ TNode node
	 * = (TNode)k_sets.elementAt(kk); if(node.containedBy(toDelete)) continue;
	 * else ksets.addElement(node); } }else{ ksets = k_sets; } k_sets = null; //
	 * int ksize = ksets.size(); int deep=ksets.size()-1; for (int x = 0; x <
	 * deep; x++){ FastVector k1sets=new FastVector();
	 * k1sets=newTNodeMerge(ksets,x); int k1size = k1sets.size(); for (int xx =
	 * 0; xx < k1size; xx++){ TNode tnode = (TNode)k1sets.elementAt(xx);
	 * rehashTnode(tnode); } ksets=k1sets; }
	 * 
	 * } enu = m_carhashtable.keys(); ksets = new FastVector();
	 * while(enu.hasMoreElements()){ ItemSet Is = (ItemSet)enu.nextElement();
	 * int sup = ((Integer)m_carhashtable.get(Is)).intValue(); if(sup >
	 * necSupport){ TNode node = new TNode(); node.num = sup; node.value =
	 * Is.m_items; ksets.addElement(node); } } m_carhashtable.clear();
	 * subList.appendElements(ksets);
	 * 
	 * }
	 * 
	 * subList=b.mergeVector(subList); subList.addElement(b);
	 * qlist.appendElements(subList); } } Result.appendElements(plist);
	 * Result.appendElements(qlist); for(int j=0;j<plist.size();j++){ TNode
	 * pt=(TNode)plist.elementAt(j); for(int k=0;k<qlist.size();k++){ TNode
	 * qt=(TNode)qlist.elementAt(k); TNode re=mergeTnode(pt,qt);
	 * Result.addElement(re); } }
	 * 
	 * return Result; }
	 * 
	 * 
	 * public void TnodeToItemSets(ArrayList<TNode> list) throws Exception{ int
	 * size=list.size(); ArrayList<TNode> ISlist=new ArrayList<TNode>();
	 * FastVector ksets=new FastVector(); // FastVector mLs=new FastVector(); //
	 * FastVector hash=new FastVector(); Hashtable hashtable; for(int
	 * i=0;i<size;i++){ TNode node=list.get(i); int lsize=ISlist.size();
	 * if(lsize==0){ ISlist.add(node); } else{ boolean flag=true; for(int
	 * j=0;j<lsize;j++){ TNode t=ISlist.get(j); if(node.size()<=t.size()){
	 * ISlist.add(j,node); flag=false; break; } } if(flag){ ISlist.add(node); }
	 * } } int k=ISlist.size(); int kk=1;
	 * 
	 * for(int i=0;i<k;i++){ TNode t=ISlist.get(i); AprioriItemSet set=new
	 * AprioriItemSet(m_instances.numInstances()); set.m_counter=t.num;
	 * set.m_items=t.value; if(t.size()==kk){ ksets.addElement(set); //
	 * displayItemset(set); } else{ m_text+="\n-----------------------"+kk+"\n";
	 * m_Ls.addElement(ksets); hashtable = AprioriItemSet.getHashtable(ksets,
	 * ksets.size()); m_hashtables.addElement(hashtable); FastVector k1sets=new
	 * FastVector(); k1sets.addElement(set); ksets=k1sets; kk++; } }
	 * m_Ls.addElement(ksets); hashtable = AprioriItemSet.getHashtable(ksets,
	 * ksets.size()); m_hashtables.addElement(hashtable);
	 * 
	 * }
	 */
	public void newTnodeToItemSets(FastVector list) throws Exception {
		int size = list.size();

		int numA = m_instances.numAttributes();
		for (int i = 0; i < numA; i++) {
			FastVector ksets = new FastVector();
			m_Ls.addElement(ksets);
		}
		for (int i = 0; i < size; i++) {
			ItemSet node = (ItemSet) list.elementAt(i);
			int nsize = node.size();
			FastVector kSets = (FastVector) m_Ls.elementAt(nsize - 1);
			kSets.addElement(node);
		}

		for (int i = 0; i < numA; i++) {
			FastVector ksets = (FastVector) m_Ls.elementAt(i);
			Hashtable hashtable = AprioriItemSet.getHashtable(ksets, ksets.size());
			m_hashtables.addElement(hashtable);
		}
	}

	public void newLabeledToItemSets(FastVector list) throws Exception {
		int size = list.size();

		int numA = m_instances.numAttributes();
		for (int i = 0; i < numA; i++) {
			FastVector ksets = new FastVector();
			m_Ls.addElement(ksets);
		}
		for (int i = 0; i < size; i++) {
			LabeledItemSet node = (LabeledItemSet) list.elementAt(i);
			int nsize = node.size();
			AprioriItemSet is = new AprioriItemSet(m_instances.numInstances());
			is.m_items = new int[node.m_items.length];
			for (int j = 0; j < node.m_items.length; j++) {
				is.m_items[j] = node.m_items[j];
			}
			is.m_counter = node.m_ruleSupCounter;
			FastVector kSets = (FastVector) m_Ls.elementAt(nsize - 1);
			kSets.addElement(is);
		}
		int i = 0;
		FastVector ksets = (FastVector) m_Ls.elementAt(i);
		while (ksets.size() > 0) {
			Hashtable hashtable = AprioriItemSet.getHashtable(ksets, ksets.size());
			m_hashtables.addElement(hashtable);
			i++;
			ksets = (FastVector) m_Ls.elementAt(i);
			if (ksets == null)
				break;
		}
	}

	public void displayItemset(AprioriItemSet is) {
		int len = is.m_items.length;
		for (int i = 0; i < len; i++) {
			m_text += is.m_items[i];
		}
		m_text += "\t" + is.m_counter + "\n";
	}

	public FastVector deleteVector(int min, FastVector list) throws Exception {
		FastVector result = new FastVector();
		for (int x = 0; x < list.size(); x++) {
			ListHead tempC = (ListHead) list.elementAt(x);
			if (tempC.count > min) {
				result.addElement(tempC);
			}
		}
		return result;
	}

	public FastVector deleteTVector(int min, FastVector list) throws Exception {
		FastVector result = new FastVector();
		int size = list.size();
		for (int x = 0; x < size; x++) {
			TNode tempC = (TNode) list.elementAt(x);
			if (tempC.m_counter > min) {
				result.addElement(tempC);
			}
		}
		return result;
	}

	public FastVector deleteLabelVector(int min, FastVector list) throws Exception {
		FastVector result = new FastVector();
		int listsize = list.size();
		for (int x = 0; x < listsize; x++) {
			LabelListHead tempC = (LabelListHead) list.elementAt(x);
			if (tempC == null)
				continue;
			if (tempC.is.m_ruleSupCounter > min) {
				result.addElement(tempC);
			}
		}
		return result;
	}

	private CMARtree buildCFPtree(FastVector instances, FastVector head) throws Exception {

		int size = instances.size();
		int numClass = m_onlyClass.attribute(0).numValues();
		CMARtree fp = new CMARtree(numClass);
		for (int ii = 0; ii < size; ii++) {
			TNode t = fp.root;
			LabelItemSetII instance = (LabelItemSetII) instances.elementAt(ii);
			int[] sup = instance.m_sup;
			int headsize = head.size();
			for (int j = 0; j < headsize; j++) {
				ListHead set = (ListHead) head.elementAt(j);
				if (set.containedBy(instance)) {
					TNode tt = new TNode(set.attr, set.value);
					tt.m_counter = instance.m_counter;
					tt.sup = new int[sup.length];
					for (int jj = 0; jj < sup.length; jj++) {
						tt.sup[jj] = sup[jj];
					}
					if (t.child.size() == 0) {
						t.addChild(tt);
						t = tt;
						set.addNextII(tt);
					} else {
						int flag = 1;
						for (int jj = 0; jj < t.child.size(); jj++) {
							TNode node = (TNode) t.child.get(jj);
							if (set.equal(node)) {
								flag = 0;
								t = node;
								t.m_counter += instance.m_counter;
								for (int k = 0; k < instance.m_sup.length; k++) {
									t.sup[k] = t.sup[k] + instance.m_sup[k];
									set.sup[k] = set.sup[k] + instance.m_sup[k];
								}

								break;
							}
						}
						if (flag > 0) {
							t.addChild(tt);
							t = tt;
							set.addNextII(tt);
						}
					}
				}
			}
		}
		return fp;
	}
	/*
	 * private CMARtree newbuildCFPtree(FastVector instances,FastVector head)
	 * throws Exception{ int num=m_instances.numAttributes(); int size =
	 * instances.size(); int total=m_instances.numInstances(); int numClass =
	 * m_onlyClass.attribute(0).numValues(); CMARtree fp=new CMARtree(numClass);
	 * for (int ii = 0; ii < size; ii++) { TNode t=fp.root; LabelItemSetII
	 * instance = (LabelItemSetII)instances.elementAt(ii); int[] sup = new
	 * int[numClass]; sup = instance.m_sup; int headsize = head.size(); for(int
	 * j = 0; j < headsize; j++){ ListHead set=(ListHead)head.elementAt(j);
	 * if(set.containedBy(instance)){ TNode tt=new TNode();
	 * tt.m_counter=instance.m_counter; // tt.m_sup = new int[sup.length]; //
	 * for(int jj = 0; jj < sup.length; jj++) // { // tt.m_sup[jj] = sup[jj]; //
	 * } tt.m_items = set.items; if(t.chnum==0){ t.addChild(tt); t=tt;
	 * set.addNextII(tt,sup); } else{ int flag=1; for(int jj=0;jj<t.chnum;jj++){
	 * TNode node = (TNode)t.child.elementAt(jj); if
	 * (node.m_items.equals(set.items)){ flag=0; t=(TNode)t.child.elementAt(jj);
	 * t.m_counter += instance.m_counter; for(int k = 0; k <
	 * instance.m_sup.length; k++){ // t.m_sup[k] =
	 * t.m_sup[k]+instance.m_sup[k]; set.sup[k] = set.sup[k] +
	 * instance.m_sup[k]; }
	 * 
	 * break; } } if(flag>0){ t.addChild(tt); t=tt; set.addNextII(tt,sup); } } }
	 * } if(t.m_sup == null){ t.m_sup = sup; } else{ for (int i = 0; i <
	 * numClass; i++){ t.m_sup[i] += sup[i]; } } } return fp; }
	 * 
	 * private FPtree buildCondFPtree(FastVector list,ListHead[]CList) throws
	 * Exception{ // int numSets=m_instances.numAttributes(); FPtree fp=new
	 * FPtree(); int numInstance=list.size(); int size=CList.length; for (int ii
	 * = 0; ii <numInstance;ii++) { TNode t=fp.root; FastVector
	 * tempList=(FastVector)list.elementAt(ii); for(int j=0;j<size;j++){
	 * ListHead set=CList[j]; if(set.containedBy(tempList)){ int
	 * count=((TNode)tempList.elementAt(0)).m_counter; TNode tt=new
	 * TNode(set.attr,set.value); tt.m_counter = count; if(t.chnum==0){
	 * t.addChild(tt); t=tt; set.addNext(tt); } else{ int flag=1; TNode[] ch =
	 * (TNode[])t.child.toArray(); for(int jj=0;jj<t.chnum;jj++){
	 * if(set.equal(ch[jj])){ flag=0; t = ch[jj]; t.m_counter += count; break; }
	 * } if(flag>0){ t.addChild(tt); t=tt; set.addNext(tt); } ch = null; } } set
	 * = null; } tempList = null; t =null; } return fp; } private CarFPtree
	 * buildCarCondFPtree(FastVector list,FastVector head) throws Exception{ int
	 * numSets=m_instances.numAttributes(); CarFPtree fp=new
	 * CarFPtree(numSets,m_instances.numInstances()); int
	 * numInstance=list.size(); int size=head.size(); for (int ii = 0; ii
	 * <numInstance;ii++) { LabelTnode t = fp.root; for(int j=0;j<size;j++){
	 * FastVector tempList=(FastVector)list.elementAt(ii); LabelListHead
	 * set=(LabelListHead)head.elementAt(j); if(set.containedBy(tempList)){
	 * LabeledItemSet Is=set.is.copy(); LabelTnode tt=new LabelTnode(Is); int
	 * count=((LabelTnode)tempList.elementAt(0)).value.m_ruleSupCounter;
	 * tt.value.m_ruleSupCounter=count; if(t.chnum==0){ t.addChild(tt); t=tt;
	 * set.addNext(tt); } else{ int flag=1; for(int jj=0;jj<t.chnum;jj++){
	 * if(t.child.get(jj).value.similar(Is)){ flag=0; t=t.child.get(jj);
	 * t.value.m_ruleSupCounter+=count; break; } } if(flag>0){ t.addChild(tt);
	 * t=tt; set.addNext(tt); } } } } } return fp; }
	 */

	// private void displayTree(TNode node){
	//
	// for(int i=0;i<node.value.length;i++){
	// m_text+=","+node.value[i];
	// }
	// m_text+="("+node.num+")\n";
	// if(node.father==null){
	// m_text+="father :null\n";
	// }
	// else{
	// m_text+="father:";
	// TNode f=node.father;
	// for(int i=0;i<f.value.length;i++){
	// m_text+=","+f.value[i];
	// }
	// m_text+="\n";
	// }
	// if(node.chnum>0){
	// for(int i=0;i<node.chnum;i++){
	// TNode t=node.child.get(i);
	// displayTree(t);
	// }
	// }
	//
	// }

	/*
	 * private FastVector newTNodeMerge(FastVector ksets,int size){ FastVector
	 * resultList=new FastVector(); //TNode result; int numFound, k; for (int i
	 * = 0; i < ksets.size(); i++) { TNode first = (TNode)ksets.elementAt(i);
	 * //m_text+="\n========"+i; out: for (int j = i+1; j < ksets.size(); j++) {
	 * // m_text+="\t======="+j; TNode second = (TNode)ksets.elementAt(j); TNode
	 * result=new TNode(); result.value=new int[first.value.length]; numFound =
	 * 0; k = 0; while (numFound < size) { if (first.value[k] ==
	 * second.value[k]) { if (first.value[k] != -1) numFound++; result.value[k]
	 * = first.value[k]; } else break out; k++; } // Check difference while (k <
	 * first.value.length) { if ((first.value[k] != -1) && (second.value[k] !=
	 * -1)) break; else { if (first.value[k] != -1){ result.value[k] =
	 * first.value[k]; } else{ result.value[k] = second.value[k]; } } k++; }
	 * 
	 * if (k == first.value.length) { if(first.num < second.num){
	 * result.num=first.num; } else{ result.num=second.num; }
	 * 
	 * resultList.addElement(result); } } } return resultList; }
	 */

	/**
	 * @param instances
	 * @param m_onlyClass
	 * @return
	 * @throws Exception
	 *             获取headertable
	 */
	public FastVector getISet1(Instances instances, Instances m_onlyClass) throws Exception {
		FastVector fs = new FastVector();
		// int len = ins.numInstances();
		int numClass = m_onlyClass.attribute(0).numValues();
		for (int i = 0; i < instances.numInstances(); i++) {
			Instance ins = instances.instance(i);
			Instance insclass = m_onlyClass.instance(i);
			for (int j = 0; j < instances.numAttributes(); j++) {
				if (instances.attribute(j).isNumeric())
					throw new Exception("Can't handle numeric attributes!");
				ListHead lh = new ListHead();
				lh.count = 1;
				lh.attr = (byte) j;
				lh.value = (byte) ins.value(j);
				if (lh.sup == null)
					lh.sup = new int[numClass];
				int classlabel = (int) insclass.value(0);
				lh.sup[classlabel] = 1;
				if (fs.size() == 0)
					fs.addElement(lh);
				else {
					boolean hasNode = false;
					for (int k = 0; k < fs.size(); k++) {
						ListHead lk = (ListHead) fs.elementAt(k);
						if (lk.equal(lh)) {
							hasNode = true;
							lk.count++;
							lk.sup[classlabel]++;
							break;
						}
					}
					if (hasNode == false) {
						fs.addElement(lh);
					}
				}
			}
		}
		return fs;
	}

	public FastVector buildClassifyNorules(Instances instances, Instances onlyClass, double min, double max,
			double minMetric, double minConv) throws Exception {

		m_instances = instances;
		m_onlyClass = onlyClass;
		m_minSupport = min;
		m_upperBoundMinSupport = max;
		m_minMetric = minMetric;
		m_minConv = minConv;

		FastVector kSets;
		int necSupport, necMaxSupport;

		double nextMinSupport = m_minSupport * (double) m_instances.numInstances();
		double nextMaxSupport = m_upperBoundMinSupport * (double) m_instances.numInstances();
		if ((double) Math.rint(nextMinSupport) == nextMinSupport) {
			necSupport = (int) nextMinSupport;
		} else {
			necSupport = Math.round((float) (nextMinSupport + 0.5));
		}
		if ((double) Math.rint(nextMaxSupport) == nextMaxSupport) {
			necMaxSupport = (int) nextMaxSupport;
		} else {
			necMaxSupport = Math.round((float) (nextMaxSupport + 0.5));
		}

		// kSets = ListHead.singleton(m_instances);
		// ListHead.upDateCounters(kSets,m_instances,m_onlyClass);
		kSets = getISet1(m_instances, m_onlyClass);
		kSets = ListHead.deleteItemSets(kSets, necSupport, necMaxSupport);

		int size = kSets.size();

		for (int j = 0; j < size; j++) {
			for (int k = j + 1; k < size; k++) {
				int nj = ((ListHead) kSets.elementAt(j)).count;
				int nk = ((ListHead) kSets.elementAt(k)).count;
				if (nj < nk) {
					kSets.swap(j, k);
				}
			}
		}

		CMARtree fp = buildCMARtree(m_instances, m_onlyClass, kSets);
		return kSets;
	}

	// *******************************************************************
	// 新添加的构建树的方法，针对多棵树
	// ********************************************************************
	public FastVector buildClassifyNorules(Instances allData, Instances instances, Instances onlyClass, double min,
			double max, double minMetric, double minConv, int classLabel) throws Exception {

		this.allData = allData;
		m_instances = instances;
		m_onlyClass = onlyClass;
		m_minSupport = min;
		m_upperBoundMinSupport = max;
		m_minMetric = minMetric;
		m_minConv = minConv;

		FastVector kSets;
		int necSupport, necMaxSupport;

		double nextMinSupport = m_minSupport * (double)m_instances.numAttributes()*(double)m_instances.numAttributes()/ (double) allData.numInstances(); // 多颗树中支持度仍然需要按照全局的进行计算
		double nextMaxSupport = m_upperBoundMinSupport * (double) allData.numInstances(); //// 多颗树中支持度仍然需要按照全局的进行计算
		if ((double) Math.rint(nextMinSupport) == nextMinSupport) {
			necSupport = (int) nextMinSupport;
		} else {
			necSupport = Math.round((float) (nextMinSupport + 0.5));
		}
		if ((double) Math.rint(nextMaxSupport) == nextMaxSupport) {
			necMaxSupport = (int) nextMaxSupport;
		} else {
			necMaxSupport = Math.round((float) (nextMaxSupport + 0.5));
		}

		// kSets = ListHead.singleton(m_instances);
		// ListHead.upDateCounters(kSets,m_instances,m_onlyClass);
		kSets = getISetU(m_instances, m_onlyClass);
		kSets = ListHead.deleteItemSets(kSets, necSupport, necMaxSupport);

		int size = kSets.size();

		for (int j = 0; j < size; j++) {
			for (int k = j + 1; k < size; k++) {
				int nj = ((ListHead) kSets.elementAt(j)).count;
				int nk = ((ListHead) kSets.elementAt(k)).count;
				if (nj < nk) {
					kSets.swap(j, k);
				}
			}
		}

		CMARtree fp = buildCMARtree(m_instances, m_onlyClass, kSets,classLabel);
		return kSets;
	}
	
	
	
	//*************************************************************
	//针对多棵树
	//*************************************************************
	public FastVector getISetU(Instances instances, Instances m_onlyClass) throws Exception {
		FastVector fs = new FastVector();
		// int len = ins.numInstances();
		int numClass = 1;
		for (int i = 0; i < instances.numInstances(); i++) {
			Instance ins = instances.instance(i);
			Instance insclass = m_onlyClass.instance(i);
			for (int j = 0; j < instances.numAttributes(); j++) {
				if (instances.attribute(j).isNumeric())
					throw new Exception("Can't handle numeric attributes!");
				ListHead lh = new ListHead();
				lh.count = 1;
				lh.attr = (byte) j;
				lh.value = (byte) ins.value(j);
				if (lh.sup == null)
					lh.sup = new int[numClass];
				int classlabel = (int) insclass.value(0);
				lh.sup[0] = 1;
				if (fs.size() == 0)
					fs.addElement(lh);
				else {
					boolean hasNode = false;
					for (int k = 0; k < fs.size(); k++) {
						ListHead lk = (ListHead) fs.elementAt(k);
						if (lk.equal(lh)) {
							hasNode = true;
							lk.count++;
							lk.sup[0]++;
							break;
						}
					}
					if (hasNode == false) {
						fs.addElement(lh);
					}
				}
			}
		}
		return fs;
	}

	public double[] calculatePro(Instance toTest, FastVector head, double[] supB) {
		//////////////////////////// for each test instance, find the
		//////////////////////////// cond-patten base and build CP-tree
		terminal = false;
		minNumRules = 80000;// Integer.MAX_VALUE;
		numRules = 0;
		int len = m_instances.numAttributes();
		int numClass = (m_onlyClass.attribute(0)).numValues();
		int total = m_instances.numInstances();
		int numAttr = 0;
		double[] pro = new double[numClass];
		for (int j = 0; j < len; j++) {
			numAttr += m_instances.attribute(j).numValues();
		}
		int min, max;
		double nextMinSupport = m_minSupport * (double) m_instances.numInstances();
		double nextMaxSupport = m_upperBoundMinSupport * (double) m_instances.numInstances();
		if ((double) Math.rint(nextMinSupport) == nextMinSupport) {
			min = (int) nextMinSupport;
		} else {
			min = Math.round((float) (nextMinSupport + 0.5));
		}
		if ((double) Math.rint(nextMaxSupport) == nextMaxSupport) {
			max = (int) nextMaxSupport;
		} else {
			max = Math.round((float) (nextMaxSupport + 0.5));
		}
		int size = head.size();
		for (int j = (size - 1); j >= 0; j--) {
			ListHead lj = (ListHead) head.elementAt(j);
			if (!lj.containedBy(toTest)) {
				continue;
			}
			TNode b = new TNode(lj.attr, lj.value);
			b.sup = lj.sup;
			b.m_counter = lj.count;
			boolean nofit = true;
			for (int cc = 0; cc < b.sup.length; cc++) {
				if (b.sup[cc] > min) { // new changed
					nofit = false;

					double conf = (double) b.sup[cc] / (double) b.m_counter;
					if (conf == 1)
						conf = 0.999;
					double conv = (1 - supB[cc]) / (1 - conf);
					if (conv >= m_minConv) {
						numRules++;
						if (numRules > minNumRules)
							terminal = true;
						double weight = calWeight(conv, 1, len);
						pro[cc] += weight;
					}
				}
			}
			if (nofit)
				continue;
			int[] table = new int[numAttr];
			FastVector list = new FastVector();
			int nextnum = lj.nextnum;
			for (int l = 0; l < nextnum; l++) {
				LabelItemSetII setl = new LabelItemSetII(total, numClass);
				FastVector nextList = lj.next;
				TNode tl0 = (TNode) nextList.elementAt(l);
				setl.m_sup = tl0.sup;
				setl.m_items = new int[len];

				for (int ll = 0; ll < setl.m_items.length; ll++) {
					setl.m_items[ll] = -1;
				}
				setl.m_counter = tl0.m_counter;
				TNode t = tl0.father;
				while (t.father != null) {
					if (t.containedBy(toTest)) {
						int index = getHashcode(t.attr, t.value);
						if (index > -1)
							table[index] += tl0.m_counter;
						setl.m_items[t.attr] = t.value; // form Cond Pattens
					}
					t = t.father;
				}
				if (setl.size() > 0) {
					list.addElement(setl);
				}
			}
			if (list.size() > 0) {

				FastVector CpTlist = new FastVector();// the list head of new
														// cond-patten tree
				for (int cc = 0; cc < numAttr; cc++) {
					if (table[cc] >= min) {
						byte[] av = getItem(cc);
						ListHead lh = new ListHead(table[cc], av[0], av[1]);
						lh.sup = new int[numClass];
						CpTlist.addElement(lh);

					}
				}
				table = null;
				if (CpTlist.size() == 0) {
					continue;
				}

				int CpTsize = CpTlist.size();
				for (int x = 0; x < CpTsize; x++) {
					for (int xx = x + 1; xx < CpTsize; xx++) {
						ListHead tempC = (ListHead) CpTlist.elementAt(x);
						ListHead tempD = (ListHead) CpTlist.elementAt(xx);
						if (tempD.count > tempC.count) {
							CpTlist.swap(x, xx);
						}
					}
				}

				///////// cond-Fp tree build and mine
				try {
					CMARtree subTree = buildCFPtree(list, CpTlist);

					LinkedList<TNode> beta = new LinkedList<TNode>();
					beta.add(b);
					if (subTree.root.child.size() > 0) {
						mineCMARtree(subTree, CpTlist, beta, min, max, supB, pro);
						if (terminal) {
							// System.out.println("the final number of rules
							// :"+numRules);
							return pro;
						}
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		// System.out.println("the final number of rules :"+numRules);

		return pro;
	}

	// ****************************************************************
	// 多棵树的投票方法
	// ****************************************************************
	public double[] calculateVote(Instance toTest, FastVector head, double[] supB,int classLabel) {
		//////////////////////////// for each test instance, find the
		//////////////////////////// cond-patten base and build CP-tree
		terminal = false;
		minNumRules = 80000;// Integer.MAX_VALUE;
		numRules = 0;
		int len = m_instances.numAttributes();
		int numClass = 1;
		int total = m_instances.numInstances();
		int numAttr = 0;
		double[] pro = new double[numClass];

		for (int j = 0; j < len; j++) {
			numAttr += m_instances.attribute(j).numValues();
		}
		int min, max;
		double nextMinSupport = m_minSupport * (double) m_instances.numInstances();
		double nextMaxSupport = m_upperBoundMinSupport * (double) m_instances.numInstances();
		if ((double) Math.rint(nextMinSupport) == nextMinSupport) {
			min = (int) nextMinSupport;
		} else {
			min = Math.round((float) (nextMinSupport + 0.5));
		}
		if ((double) Math.rint(nextMaxSupport) == nextMaxSupport) {
			max = (int) nextMaxSupport;
		} else {
			max = Math.round((float) (nextMaxSupport + 0.5));
		}
		int size = head.size();
		for (int j = (size - 1); j >= 0; j--) {
			ListHead lj = (ListHead) head.elementAt(j);
			if (!lj.containedBy(toTest)) {
				continue;
			}
			TNode b = new TNode(lj.attr, lj.value);
			b.sup = lj.sup;
			b.m_counter = lj.count;
			boolean nofit = true;
			for (int cc = 0; cc < b.sup.length; cc++) {
				if (b.sup[cc] > min) { // new changed
					nofit = false;

					double conf = (double) b.sup[cc] / (double) b.m_counter;
					if (conf == 1)
						conf = 0.999;
					double conv = (1 - supB[cc]) / (1 - conf);
					if (conv >= m_minConv) {
						numRules++;
						if (numRules > minNumRules)
							terminal = true;
						double weight = calWeight(conv, 1, len);
//						System.out.println(cc);
						pro[cc] += weight;
					}
				}
			}
			if (nofit)
				continue;
			int[] table = new int[numAttr];
			FastVector list = new FastVector();
			int nextnum = lj.nextnum;
			for (int l = 0; l < nextnum; l++) {
				LabelItemSetII setl = new LabelItemSetII(total, numClass);
				FastVector nextList = lj.next;
				TNode tl0 = (TNode) nextList.elementAt(l);
				setl.m_sup = tl0.sup;
				setl.m_items = new int[len];

				for (int ll = 0; ll < setl.m_items.length; ll++) {
					setl.m_items[ll] = -1;
				}
				setl.m_counter = tl0.m_counter;
				TNode t = tl0.father;
				while (t.father != null) {
					if (t.containedBy(toTest)) {
						int index = getHashcode(t.attr, t.value);
						if (index > -1)
							table[index] += tl0.m_counter;
						setl.m_items[t.attr] = t.value; // form Cond Pattens
					}
					t = t.father;
				}
				if (setl.size() > 0) {
					list.addElement(setl);
				}
			}
			if (list.size() > 0) {

				FastVector CpTlist = new FastVector();// the list head of new
														// cond-patten tree
				for (int cc = 0; cc < numAttr; cc++) {
					if (table[cc] >= min) {
						byte[] av = getItem(cc);
						ListHead lh = new ListHead(table[cc], av[0], av[1]);
						lh.sup = new int[numClass];
						CpTlist.addElement(lh);

					}
				}
				table = null;
				if (CpTlist.size() == 0) {
					continue;
				}

				int CpTsize = CpTlist.size();
				for (int x = 0; x < CpTsize; x++) {
					for (int xx = x + 1; xx < CpTsize; xx++) {
						ListHead tempC = (ListHead) CpTlist.elementAt(x);
						ListHead tempD = (ListHead) CpTlist.elementAt(xx);
						if (tempD.count > tempC.count) {
							CpTlist.swap(x, xx);
						}
					}
				}

				///////// cond-Fp tree build and mine
				try {
					CMARtree subTree = buildCFPtree(list, CpTlist);

					LinkedList<TNode> beta = new LinkedList<TNode>();
					beta.add(b);
					if (subTree.root.child.size() > 0) {
						mineCMARtreeU(subTree, CpTlist, beta, min, max, supB, pro,classLabel);
						if (terminal) {
							// System.out.println("the final number of rules
							// :"+numRules);
							return pro;
						}
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		// System.out.println("the final number of rules :"+numRules);

		return pro;
	}

	//**********************************************************************
	//针对多棵树
	//**********************************************************************
	private void mineCMARtreeU(CMARtree fp, FastVector head, LinkedList<TNode> alpha, int min, int max, double[] supB,
			double[] dPro , int classLabel) throws Exception {

		int i = 0;
		int len = m_instances.numAttributes();
		int total = m_instances.numInstances();
		int numClass = 1;
		int numAttr = 0;
		for (int j = 0; j < len; j++) {
			numAttr += m_instances.attribute(j).numValues();
		}
		TNode t0 = fp.root;

		if (t0.child.size() == 1) { //////// is or not single path
			TNode t = (TNode) t0.child.get(0);
			i++;
			while (t.child.size() == 1) {
				i++;
				t = (TNode) t.child.get(0);
			}
		}
		if (i == head.size()) { // single path
			newMergeU(head, alpha.size(), total, len, numClass, min, supB, dPro,classLabel);
			return;
		}

		if (t0.child.size() > 0) {
			int newsize = head.size();
			for (int j = (newsize - 1); j >= 0; j--) {

				ListHead lj = (ListHead) head.elementAt(j);
				TNode b = new TNode(lj.attr, lj.value);
				b.sup = lj.sup;
				b.m_counter = lj.count;
				boolean nofit = true;
				for (int cc = 0; cc < b.sup.length; cc++) {

					if (b.sup[cc] > min) {// = new changed

						nofit = false;
						double conf = (double) b.sup[cc] / (double) b.m_counter;
						if (conf == 1)
							conf = 0.999;
						double conv = (1 - supB[classLabel]) / (1 - conf);
						if (conv >= m_minConv) {
							numRules++;
							if (numRules > minNumRules) {
								terminal = true;
								return;
							}
							int rulelen = alpha.size() + 1;
							double weight = calWeight(conv, rulelen, len);
							dPro[cc] += weight;
						}

					}
				}
				if (nofit)
					continue;
				int[] table = new int[numAttr];
				FastVector list = new FastVector();
				int nextnum = lj.nextnum;

				for (int l = 0; l < nextnum; l++) {
					LabelItemSetII setl = new LabelItemSetII(total, numClass);
					FastVector nextList = lj.next;
					TNode tl0 = (TNode) nextList.elementAt(l);
					setl.m_sup = tl0.sup;
					setl.m_items = new int[len];

					for (int ll = 0; ll < setl.m_items.length; ll++) {
						setl.m_items[ll] = -1;
					}
					setl.m_counter = tl0.m_counter;
					TNode t = tl0.father;
					while (t.father != null) {
						int index = getHashcode(t.attr, t.value);
						if (index > -1)
							table[index] += tl0.m_counter;

						setl.m_items[t.attr] = t.value; // form Cond Pattens
						t = t.father;
					}
					if (setl.size() > 0) {
						list.addElement(setl);
					}
				}
				if (list.size() > 0) {

					FastVector CpTlist = new FastVector();// the list head of
															// new cond-patten
															// tree
					for (int cc = 0; cc < numAttr; cc++) {
						if (table[cc] >= min) {
							byte[] av = getItem(cc);
							ListHead lh = new ListHead(table[cc], av[0], av[1]);
							lh.sup = new int[numClass];
							CpTlist.addElement(lh);

						}
					}
					table = null;
					if (CpTlist.size() == 0) {
						continue;
					}

					int CpTsize = CpTlist.size();
					for (int x = 0; x < CpTsize; x++) {
						for (int xx = x + 1; xx < CpTsize; xx++) {
							ListHead tempC = (ListHead) CpTlist.elementAt(x);
							ListHead tempD = (ListHead) CpTlist.elementAt(xx);
							if (tempD.count > tempC.count) {
								CpTlist.swap(x, xx);
							}
						}
					}

					///////// cond-Fp tree build and mine
					LinkedList<TNode> beta = new LinkedList<TNode>();
					beta.addAll(alpha);
					beta.add(b);
					CMARtree subTree = buildCFPtree(list, CpTlist);
					list = null;
					if (subTree.root.child.size() > 0) {
						mineCMARtreeU(subTree, CpTlist, beta, min, max, supB, dPro,classLabel);
						if (terminal)
							return;
					}

				}

			}
		}

		return;
	}
	
	//********************************************************
	//针对多棵树
	//********************************************************
	private void newMergeU(FastVector ksets, int sizealpha, int total, int itemsize, int supsize, int min,
			double[] supB, double[] dPro,int classLabel) {

		int len = ksets.size();
		int i = 0;
		for (i = 0; i < len; i++) {
			ListHead t = (ListHead) ksets.elementAt(i);
			boolean flag = true;
			

				if (t.sup[0] > min) {// = new changed
					double conf = (double) t.sup[0] / (double) t.count;
					if (conf == 1)
						conf = 0.999;
					double conv = (1 - supB[classLabel]) / (1 - conf);
					if (conv >= m_minConv) {
						flag = false;
						for (int j = 0; j < (i + 1); j++) {
							int ruleLength = j + 1 + sizealpha;
							//
							double weight = calWeight(conv, ruleLength, itemsize);
							int temp = cal(i, j);
							dPro[0] += weight * temp;
							numRules += temp;
							if (numRules > minNumRules) {
								terminal = true;
								return;
							}
						}

					}
				}
			
			if (flag) {
				break;
			}
		}

		return;
	}
	
	private void newMerge(FastVector ksets, int sizealpha, int total, int itemsize, int supsize, int min, double[] supB,
			double[] dPro) {

		int numClass = supsize;
		int len = ksets.size();
		int[][] list = new int[len][3 + supsize];
		int i = 0;
		for (i = 0; i < ksets.size(); i++) {
			ListHead t = (ListHead) ksets.elementAt(i);
			list[i][0] = t.attr;
			list[i][1] = t.value;
			list[i][2] = t.count;
			boolean flag = true;
			for (int k = 0; k < supsize; k++) {
				list[i][3 + k] = t.sup[k];
				if (t.sup[k] < min)// =
					continue;
				flag = false;
				m_numRules++;
				//////// panduan shifou conv > 1
				double conf = (double) t.sup[k] / (double) t.count;
				if (conf == 1)
					conf = 0.999;
				double convic = (1 - supB[k]) / (1 - conf);
				if (convic > m_minConv) {
					double d = itemsize - sizealpha - 1;
					if (d == 0) {
						d = 0.01;
					}
					dPro[k] += convic / d;
				}

			}
			if (flag) {
				len = i + 1;
				break;
			}
		}

		if (len == 1)
			return;

		for (int count = 2; count <= len; count++) {
			int[] group = new int[count];
			for (i = 0; i < count; i++) {
				group[i] = i;
			}

			{
				int counter = list[group[count - 1]][2];

				for (int kk = 0; kk < supsize; kk++) {
					int supkk = list[group[count - 1]][3 + kk];
					if (supkk >= min)//
					{
						double conf = (double) supkk / (double) counter;
						if (conf == 1)
							conf = 0.999;
						double conv = (1 - supB[kk]) / (1 - conf);
						if (conv > m_minConv) {
							double d = itemsize - sizealpha - count;
							if (d == 0) {
								d = 0.01;
							}
							dPro[kk] += conv / d;
						}
					}
				}
			}
			while (true) {
				int Maxi = -1;
				for (int j = 0; j < count; j++) {
					if (group[j] < (len - count + j)) {
						if (group[j] > Maxi)
							Maxi = j;
					}
				}
				if (Maxi == -1)
					break;
				group[Maxi]++;
				for (int j = Maxi + 1; j < count; j++) {
					group[j] = group[j - 1] + 1;
				}
				{
					int counter = list[group[count - 1]][2];

					for (int kk = 0; kk < supsize; kk++) {
						int supkk = list[group[count - 1]][3 + kk];
						if (supkk >= min)// =
						{
							m_numRules++;
							double conf = (double) supkk / (double) counter;
							if (conf == 1)
								conf = 0.999;
							double conv = (1 - supB[kk]) / (1 - conf);
							if (conv > m_minConv) {
								double d = itemsize - sizealpha - count;
								if (d == 0) {
									d = 0.01;
								}
								dPro[kk] += conv / d;
							}
						}
					}
				}
			}
		}

		return;
	}

	private void newMergeII(FastVector ksets, int sizealpha, int total, int itemsize, int supsize, int min,
			double[] supB, double[] dPro) {

		int len = ksets.size();
		int i = 0;
		for (i = 0; i < len; i++) {
			ListHead t = (ListHead) ksets.elementAt(i);
			boolean flag = true;
			;
			for (int k = 0; k < supsize; k++) {

				if (t.sup[k] > min) {// = new changed
					double conf = (double) t.sup[k] / (double) t.count;
					if (conf == 1)
						conf = 0.999;
					double conv = (1 - supB[k]) / (1 - conf);
					if (conv >= m_minConv) {
						flag = false;
						for (int j = 0; j < (i + 1); j++) {
							int ruleLength = j + 1 + sizealpha;
							//
							double weight = calWeight(conv, ruleLength, itemsize);
							int temp = cal(i, j);
							dPro[k] += weight * temp;
							numRules += temp;
							if (numRules > minNumRules) {
								terminal = true;
								return;
							}
						}

					}
				}
			}
			if (flag) {
				break;
			}
		}

		return;
	}

	private int cal(int m, int n) {
		double result = 1;
		if (n == 0)
			return (int) result;
		else {

			double mm = m;
			double nn;
			if (m - n > n) {
				nn = n;
			} else
				nn = m - n;
			while (nn > 0) {
				result = result * (mm / nn);
				mm--;
				nn--;
			}
		}
		return (int) result;

	}

	private void mineCMARtree(CMARtree fp, FastVector head, LinkedList<TNode> alpha, int min, int max, double[] supB,
			double[] dPro) throws Exception {

		int i = 0;
		int len = m_instances.numAttributes();
		int total = m_instances.numInstances();
		int numClass = m_onlyClass.attribute(0).numValues();
		int numAttr = 0;
		for (int j = 0; j < len; j++) {
			numAttr += m_instances.attribute(j).numValues();
		}
		TNode t0 = fp.root;

		if (t0.child.size() == 1) { //////// is or not single path
			TNode t = (TNode) t0.child.get(0);
			i++;
			while (t.child.size() == 1) {
				i++;
				t = (TNode) t.child.get(0);
			}
		}
		if (i == head.size()) { // single path
			newMergeII(head, alpha.size(), total, len, numClass, min, supB, dPro);
			return;
		}

		if (t0.child.size() > 0) {
			int newsize = head.size();
			for (int j = (newsize - 1); j >= 0; j--) {

				ListHead lj = (ListHead) head.elementAt(j);
				TNode b = new TNode(lj.attr, lj.value);
				b.sup = lj.sup;
				b.m_counter = lj.count;
				boolean nofit = true;
				for (int cc = 0; cc < b.sup.length; cc++) {

					if (b.sup[cc] > min) {// = new changed

						nofit = false;
						double conf = (double) b.sup[cc] / (double) b.m_counter;
						if (conf == 1)
							conf = 0.999;
						double conv = (1 - supB[cc]) / (1 - conf);
						if (conv >= m_minConv) {
							numRules++;
							if (numRules > minNumRules) {
								terminal = true;
								return;
							}
							int rulelen = alpha.size() + 1;
							double weight = calWeight(conv, rulelen, len);
							dPro[cc] += weight;
						}

					}
				}
				if (nofit)
					continue;
				int[] table = new int[numAttr];
				FastVector list = new FastVector();
				int nextnum = lj.nextnum;

				for (int l = 0; l < nextnum; l++) {
					LabelItemSetII setl = new LabelItemSetII(total, numClass);
					FastVector nextList = lj.next;
					TNode tl0 = (TNode) nextList.elementAt(l);
					setl.m_sup = tl0.sup;
					setl.m_items = new int[len];

					for (int ll = 0; ll < setl.m_items.length; ll++) {
						setl.m_items[ll] = -1;
					}
					setl.m_counter = tl0.m_counter;
					TNode t = tl0.father;
					while (t.father != null) {
						int index = getHashcode(t.attr, t.value);
						if (index > -1)
							table[index] += tl0.m_counter;

						setl.m_items[t.attr] = t.value; // form Cond Pattens
						t = t.father;
					}
					if (setl.size() > 0) {
						list.addElement(setl);
					}
				}
				if (list.size() > 0) {

					FastVector CpTlist = new FastVector();// the list head of
															// new cond-patten
															// tree
					for (int cc = 0; cc < numAttr; cc++) {
						if (table[cc] >= min) {
							byte[] av = getItem(cc);
							ListHead lh = new ListHead(table[cc], av[0], av[1]);
							lh.sup = new int[numClass];
							CpTlist.addElement(lh);

						}
					}
					table = null;
					if (CpTlist.size() == 0) {
						continue;
					}

					int CpTsize = CpTlist.size();
					for (int x = 0; x < CpTsize; x++) {
						for (int xx = x + 1; xx < CpTsize; xx++) {
							ListHead tempC = (ListHead) CpTlist.elementAt(x);
							ListHead tempD = (ListHead) CpTlist.elementAt(xx);
							if (tempD.count > tempC.count) {
								CpTlist.swap(x, xx);
							}
						}
					}

					///////// cond-Fp tree build and mine
					LinkedList<TNode> beta = new LinkedList<TNode>();
					beta.addAll(alpha);
					beta.add(b);
					CMARtree subTree = buildCFPtree(list, CpTlist);
					list = null;
					if (subTree.root.child.size() > 0) {
						mineCMARtree(subTree, CpTlist, beta, min, max, supB, dPro);
						if (terminal)
							return;
					}

				}

			}
		}

		return;
	}

	/**
	 * the method for calculating the weight
	 * 
	 * @param support
	 *            the support of the rule
	 * @param conf
	 *            the confidence of the rule
	 * @param rulelen
	 *            the length of the rule without the classlabel
	 * @param size
	 *            the size of an instance without the classlabel
	 */
	private double calWeight(double support, double conf, int rulelen, int size) {
		double weight = 0;
		// weight = conv;
		// double d = size - rulelen;
		// if (d == 0)
		// d = 0.01;
		weight = conf * support * rulelen / size;

		// weight = conv * rulelen / size;
		// weight = conv * (rulelen+1) /(size+1);
		return weight;
	}

	/**
	 * the method for calculating the weight
	 * 
	 * @param conv
	 *            the confidence of the rule
	 * @param rulelen
	 *            the length of the rule without the classlabel
	 * @param size
	 *            the size of an instance without the classlabel
	 */
	private double calWeight(double conv, int rulelen, int size) {
		double weight = 0;
		// weight = conv;
		double d = size - rulelen;
		if (d == 0)
			d = 0.01;
		weight = conv / d;
		// weight = conv* rulelen;

		// weight = conv * rulelen / size;
		// weight = conv * (rulelen+1) /(size+1);
		return weight;
	}

	/**
	 * Main method.
	 * 
	 * @param args
	 *            the commandline options
	 */
	public static void main(String[] args) {
		// args={"-t zoo.arff "};
		// int a=1;
		String[] arg = { "-t", "balance/zoo.arff", "-M", "0.02", "-A", "-C", "0" };
		runAssociator(new FP(), arg);
		// for (int i = 0; i < 10;i++){
		// System.out.println(cal(10,i));
		// }

	}
}
