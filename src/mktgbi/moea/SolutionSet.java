/**
 * SolutionSet.java

 */
package mktgbi.moea;

import java.io.*;
import java.util.*;
import java.util.logging.Logger;

import mktgbi.algorithm.util.Cluster;
import mktgbi.dataio.Config;

import mktgbi.moea.operator.DominanceComparator;
import mktgbi.util.GaConverter;
import mktgbi.util.GaFile;
import mktgbi.util.GaMath;

/**
 * Class representing a SolutionSet (a set of solutions)
 */
public class SolutionSet {

	protected static Logger sm_logger = Config.SM_LOGGER; 
	private static final Comparator<Solution> m_dominance = new DominanceComparator();
	
	/**
	 * Stores a list of <code>solution</code> objects.
	 */
	private ArrayList<Solution> m_solutionsList = null;


	public SolutionSet() {
		m_solutionsList = new ArrayList<Solution>();
	} // SolutionSet
	
	
	public SolutionSet(int initSize) {
		m_solutionsList = new ArrayList<Solution>(initSize);
	} // SolutionSet

	public SolutionSet(ArrayList<Solution> solutions) {
		m_solutionsList = solutions;
	}
	/**
	 * Inserts a new solution into the SolutionSet.
	 * 
	 * @param solution
	 *            The <code>Solution</code> to store
	 * @return True If the <code>Solution</code> has been inserted, false
	 *         otherwise.
	 */
	public void add(Solution solution) {
		m_solutionsList.add(solution);
	} // add

	/**
	 * Returns the ith solution in the set.
	 * 
	 * @param i
	 *            Position of the solution to obtain.
	 * @return The <code>Solution</code> at the position i.
	 * @throws IndexOutOfBoundsException.
	 */
	public Solution get(int i) {
		if (i >= m_solutionsList.size()) {
			throw new IndexOutOfBoundsException("Index out of Bound " + i);
		}
		return (m_solutionsList.get(i));
	} // get

	/**
	 * Sorts a SolutionSet using a <code>Comparator</code>.
	 * 
	 * @param comparator
	 *            <code>Comparator</code> used to sort.
	 */
	public void sort(Comparator<Solution> comparator) {
		if (comparator == null) {
			Config.SM_LOGGER.severe("No criterium for compare exist");
			return;
		} // if
		Collections.sort(m_solutionsList, comparator);
	} // sort

	/**
	 * Returns the number of solutions in the SolutionSet.
	 * 
	 * @return The size of the SolutionSet.
	 */
	public int size() {
		return m_solutionsList.size();
	} // size

	/**
	 * Writes the objective funcion values of the <code>Solution</code> objects
	 * into the set in a file.
	 * 
	 * @param path
	 *            The output file name
	 */
	public void printObjectivesToFile(String path) {
		try {
			BufferedWriter bw = GaFile.getFileWriter(path);

			for (int i = 0; i < m_solutionsList.size(); i++) {
				bw.write(m_solutionsList.get(i).toString());
				bw.newLine();
			}

			/* Close the file */
			bw.close();
		} catch (IOException e) {
			GaFile.reportError(path, e);
		}
	} // printObjectivesToFile

	/**
	 * Writes the decision variable values of the <code>Solution</code>
	 * solutions objects into the set in a file.
	 * 
	 * @param path
	 *            The output file name
	 */
	public void printVariablesToFile(String path) {
		try {
			BufferedWriter bw = GaFile.getFileWriter(path);
			for (int i = 0; i < m_solutionsList.size(); i++) {
				bw.write(m_solutionsList.get(i).getDecisionVariables()
						.toString());
				bw.newLine();
			}
			/* Close the file */
			bw.close();
		} catch (IOException e) {
			GaFile.reportError(path, e);
		}
	} // printVariablesToFile
	
	public void printMembersToFile(String path) {
		try {
			BufferedWriter bw = GaFile.getFileWriter(path);
			for (int i = 0; i < m_solutionsList.size(); i++) {
				int assigned[] = m_solutionsList.get(i).getDecisionVariables().getAssigned();
				String line = GaConverter.intsToString(assigned);
				bw.write(line);
				bw.newLine();
			}
			/* Close the file */
			bw.close();
		} catch (IOException e) {
			GaFile.reportError(path, e);
		}
	} 
	
	public void printSegmentSizesToFile(String path) {
		try {
		
			BufferedWriter bw = GaFile.getFileWriter(path);

			for (int i = 0; i < m_solutionsList.size(); i++) {
				IDecisionVariables dvs = m_solutionsList.get(i).getDecisionVariables();
				int[] assigned = dvs.getAssigned();
				Cluster[] clusters = Cluster.createClusters(assigned);
				for (Cluster cluster : clusters) {
					bw.write(cluster.size() + " ");
				}
				bw.newLine();
			}

			/* Close the file */
			bw.close();
		} catch (IOException e) {
			GaFile.reportError(path, e);
		}
	} // printObjectivesToFile

	/**
	 * Deletes the <code>Solution</code> at position i in the set.
	 * 
	 * @param i
	 *            The position of the solution to remove.
	 */
	public void remove(int i) {
		if (i > m_solutionsList.size() - 1) {
			Config.SM_LOGGER.severe("Size is: " + this.size());
		} // if
		m_solutionsList.remove(i);
	} // remove


	/**
	 * Returns a new <code>SolutionSet</code> which is the result of the union
	 * between the current solution set and the one passed as a parameter.
	 * 
	 * @param solutionSet
	 *            SolutionSet to join with the current solutionSet.
	 * @return The result of the union operation.
	 */
	public static SolutionSet union(SolutionSet offspringSet, SolutionSet archiveSet) {
		ArrayList<Solution> offsprings = offspringSet.m_solutionsList;
		ArrayList<Solution> archives = archiveSet.m_solutionsList;
		int newSize = offsprings.size() + archives.size();
		
		ArrayList<Solution> unionSolutions = new ArrayList<Solution>(newSize);
		unionSolutions.addAll(archives);
		for (Solution offspring : offsprings) {
			
			boolean addFlag = true;
			double[] objectives = offspring.getObjectives();
			for(Solution unionSolution : unionSolutions) {
				
				// if this dominates existing solution, add it
				// if this is a dominated solution, don't add it
				// otherwise, check distance
				int dominance = m_dominance.compare(unionSolution, offspring); 
				if (dominance == 1) {
					break;
				} else if (dominance == -1) {
					addFlag = false;
					break;
				} else {
					double[] oldObjectives = unionSolution.getObjectives();
					double distance = GaMath.calEuclideanDistance(objectives, oldObjectives);
					if (distance < Config.getMinObjectiveDistance()) {
						addFlag = false;
						sm_logger.fine("The non-dominated solution is close to an existing one.");
						break;
					}
				} 
			}
			
			if (addFlag) {
				unionSolutions.add(offspring);
				sm_logger.fine("Added a new far-enough solution");
			} else {
				
			}
		}
		return (new SolutionSet(unionSolutions));
	} // union

} // SolutionSet

