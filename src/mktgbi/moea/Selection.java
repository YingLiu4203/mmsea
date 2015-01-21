/**
 * Spea2Fitness.java
 *
 * @author Juanjo Durillo
 * @version 1.0
 *
 */
package mktgbi.moea;

import java.util.*;

import mktgbi.algorithm.util.Distance;
import mktgbi.moea.operator.DominanceComparator;
import mktgbi.moea.operator.FitnessComparator;

/**
 * This class implements some facilities for calculating the Spea2Fitness
 */
public class Selection {

	/**
	 * Stores the distance between solutions
	 */
	private double[][] m_distanceMatrix = null;
	public static long Used_Time = 0;

	/**
	 * Stores the solutionSet to assign the fitness
	 */
	private SolutionSet m_solutionSet = null;
	private Comparator<Solution> m_fitnessComparator = null;
	private Comparator<Solution> m_dominanceComparator =null;
	
	private static final int DOMINATED = Integer.MAX_VALUE;

	/**
	 * Constructor. Creates a new instance of Spea2Fitness for a given
	 * <code>SolutionSet</code>.
	 * 
	 * @param solutionSet
	 *            The <code>SolutionSet</code>
	 */
	public Selection(SolutionSet solutionSet) {
		
		m_solutionSet = solutionSet;
		m_fitnessComparator = new FitnessComparator();
		m_dominanceComparator = new DominanceComparator();
		
	} // Spea2Fitness
	
	/**
	 * Gets 'size' elements from a population of more than 'size' elements using
	 * for this de enviromentalSelection truncation
	 * 
	 * @param size
	 *            The number of elements to get.
	 */
	public SolutionSet environmentalSelection(int selectSize) {
		long initTime = System.currentTimeMillis();
		removeDominated();
		
		int solutionSize = m_solutionSet.size();
		if (selectSize < solutionSize) {
			// remove the extra solutions
			// the most crowded is at the beginning
			int extraSize = solutionSize - selectSize;
			for (int count = 0; count < extraSize; count++) {
				fitnessAssign();
				m_solutionSet.sort(m_fitnessComparator);
				m_solutionSet.remove(m_solutionSet.size() -1);	
			}
		} else {
			fitnessAssign();
		}
		Used_Time += (System.currentTimeMillis() - initTime);
		return m_solutionSet;
	} // environmentalSelection
	
	private void removeDominated() {
		int solutionSetSize = m_solutionSet.size();
	
		// raw fitness is 1 if it's a dominated solution
		for (int solutionIndex = 0; solutionIndex < solutionSetSize; solutionIndex++) {
			for (int j = 0; j < m_solutionSet.size(); j++) {
				if (m_dominanceComparator.compare(
						m_solutionSet.get(solutionIndex), m_solutionSet.get(j)) == 1) {
					
					m_solutionSet.get(solutionIndex).setRank(DOMINATED);
					break;
				} // if
			} // for
		} // for
		
		int solutionIndex = 0;
		while (solutionIndex < m_solutionSet.size()) {
			int dominance = m_solutionSet.get(solutionIndex).getRank();
			if (dominance == DOMINATED) {
				m_solutionSet.remove(solutionIndex);
			} else {
				solutionIndex++;
			} // if
		} // while
	}

	/**
	 * Assigns fitness for all the solutions.
	 */
	private void fitnessAssign() {
		
		int solutionSetSize = m_solutionSet.size();
		
		m_distanceMatrix = Distance.getDistanceMatrix(m_solutionSet);
		for (int i = 0; i < m_solutionSet.size(); i++) {
			Arrays.sort(m_distanceMatrix[i]);
		} // for
		
		// use the sum of the nearest k distances
		int k = (int)(Math.sqrt(solutionSetSize));
		for (int solutionIndex = 0; solutionIndex < m_distanceMatrix.length; solutionIndex++) {
			double distance = 0; 
			for (int j = 0; j < k; j++) {
				distance += m_distanceMatrix[solutionIndex][j];
			}
			double fitness = 1 / (distance + 2.0);
			m_solutionSet.get(solutionIndex).setFitness(fitness);
		} // for
	} // fitnessAsign
	
	
	

	
	
} // Spea2Fitness
