/**
 * Problem.java
 *
 * @author Antonio J. Nebro
 * @version 1.0
 * Created on 16 de octubre de 2006, 17:04
 */

package mktgbi.moea.problem;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.logging.Logger;

import mktgbi.algorithm.util.Cluster;
import mktgbi.algorithm.util.HeuristicCluster;
import mktgbi.dataio.Config;
import mktgbi.dataio.DataBin;
import mktgbi.dataio.DataSource;
import mktgbi.moea.EdgeVariables;
import mktgbi.moea.Solution;
import mktgbi.moea.SolutionSet;
import mktgbi.moea.operator.EdgeCrossover;
import mktgbi.moea.operator.ICrossover;
import mktgbi.moea.operator.ISelector;
import mktgbi.moea.operator.MixSetSelector;
import mktgbi.util.GaMath;

/**
 * Abstract class representing a multiobjective optimization problem
 */
public abstract class Problem {

	public static long Used_Time = 0;
	/**
	 * Stores the number of objectives of the problem
	 */
	protected int m_numberOfObjectives;
	
	protected static Logger sm_logger = Config.SM_LOGGER; 
	

	protected DataBin[] m_dataBins = null;
	
	// number of different initial methods in initialization of response model
	protected int m_numMixedMethods = Integer.MIN_VALUE;
	
	protected ICrossover m_crossoverOperator = null;
	
	protected ISelector m_selectorOperator = null;
	
	int m_minNumSegments = 0;
	int m_maxNumSegments = 0;
	int m_setSize = 0;

	/**
	 * Constructor.
	 */
	public Problem() {

		m_numberOfObjectives = Config.getNumObjectives();
		
		m_dataBins = new DataBin[m_numberOfObjectives + 1];
		
		for (int index = 0; index < m_numberOfObjectives; index++) {
			m_dataBins[index] = DataSource.getDataBin(index);
		}
		m_dataBins[m_numberOfObjectives] = DataSource.getAll();
		
		m_minNumSegments = Config.getMinNumSegments();
		m_maxNumSegments = Config.getMaxNumSegments();
		m_setSize = m_maxNumSegments - m_minNumSegments + 1;
		
		m_crossoverOperator = new EdgeCrossover();
		m_selectorOperator = new MixSetSelector();
		
	} // Problem


	/**
	 * Gets the the number of objectives of the problem.
	 * 
	 * @return the number of objectives.
	 */
	public int getNumberOfObjectives() {
		return m_numberOfObjectives;
	} // getNumberOfObjectives

	/**
	 * Evaluates a <code>Solution</code> object.
	 * 
	 * @param solution
	 *            The <code>Solution</code> to evaluate.
	 */
	public void evaluate(Solution solution) {
		
		long initTime = System.currentTimeMillis();
		
		int[] assigned = solution.getDecisionVariables().getAssigned();
		double[] objectives = Cluster.calObjectives(assigned);
		for (int index = 0; index < m_numberOfObjectives; index++) {
			solution.setObjective(index, objectives[index]);
		}
		
		Used_Time += (System.currentTimeMillis() - initTime);
	}
	
	public SolutionSet[] initializeSolutions() {
		
		int initSize = Config.getInitSolutionSize();
		SolutionSet[] solutionSets = new SolutionSet[m_setSize];
		
		for (int numSegments = m_minNumSegments; numSegments <= m_maxNumSegments; numSegments++) {
			ArrayList<Solution> solutions = new ArrayList<Solution> (initSize);
			initializeSegmentSolutions(solutions, initSize, numSegments);
			sm_logger.info("Initialized soulutions for number of segments: " + numSegments 
					+ " initialized size is: " + solutions.size());
			
			int setIndex = numSegments - m_minNumSegments;
			solutionSets[setIndex] = new SolutionSet(solutions);
		}
		
		return solutionSets;
	}
	
	
	protected abstract void initializeSegmentSolutions(ArrayList<Solution> solutions,
			int initSize, int numSegments);
	
	
	protected Solution createSolution(HeuristicCluster heuristic, int numSegments) {
		int[] assigned = heuristic.run(numSegments);
		EdgeVariables vars = EdgeVariables.createByAssigned(assigned);
		return (new Solution(vars));
	}


	// make sure the solution is not too close to an existing one
	protected boolean checkObjectiveDistance(Solution newSolution, 
			ArrayList<Solution> solutions) {
		
		boolean farFlag = true;
		double[] newObjectives = newSolution.getObjectives();
		for (Solution savedSolution : solutions) {
			double[] oldObjectives = savedSolution.getObjectives();
			double distance = GaMath.calEuclideanDistance(newObjectives, oldObjectives);
			if (distance < Config.getMinObjectiveDistance()) {
				sm_logger.fine("the new solution is too close. Distance is: " + distance);
				farFlag = false;
				break;
			}
		}
		return farFlag;
	}
	
	
	protected Solution[] fromArrayList(ArrayList<Solution> solutions) {
		int size = solutions.size();
		Solution[] retValues = new Solution[size];
		for (int index = 0; index < size; index++) {
			retValues[index] = solutions.get(index);
		}
		return retValues;
	}
	
	
	public ICrossover getCrossoverOperator() {
		return m_crossoverOperator;
	}
	
	public ISelector getSelectorOperator() {
		return m_selectorOperator;
	}
	
	public  SolutionSet initFromFile(String filename) throws IOException {
		
		SolutionSet solutionSet = new SolutionSet();
		
		FileInputStream fis = new FileInputStream(filename);
		InputStreamReader isr = new InputStreamReader(fis); 
		BufferedReader br = new BufferedReader(isr);
		String line; 
		while ((line = br.readLine()) != null) {
			Solution solution = initFromLine(line);
			solutionSet.add(solution);
		}
		br.close();
		
		return solutionSet;
	}
	
	protected  Solution initFromLine(String line) {
		EdgeVariables dv = EdgeVariables.fromString(line);
		Solution solution = new  Solution(dv);
		return solution;
	}
	
} // Problem
