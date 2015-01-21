package mktgbi.moea;

import mktgbi.dataio.Config;
import mktgbi.util.GaConverter;

/**
 * Class representing a solution for a problem.
 */
public class Solution {
	/**
	 * Stores the decision variables of the solution.
	 */
	private IDecisionVariables m_decisionVariables = null;

	/**
	 * Stores the objectives values of the solution.
	 */
	private double[] m_objectives = null;

	/**
	 * Stores the number of objective values of the solution
	 */
	private int m_numberOfObjectives;

	/**
	 * Stores the so called fitness value. Used in some metaheuristics
	 */
	private double m_fitness;

	/**
	 * Stores the so called rank of the solution. Used in NSGA-II
	 */
	private int m_rank;

	/**
	 * Constructor. set it to private so no body use it
	 */
	@SuppressWarnings("unused")
	private Solution() {
	} // Solution

	
	/**
	 * Constructor
	 * 
	 * @param problem
	 *            The problem to solve
	 */
	public Solution(IDecisionVariables variables) {
		// -> Initializing state variables and allocating memory

		m_numberOfObjectives = Config.getNumObjectives();
		m_objectives = new double[m_numberOfObjectives];

		// Setting initial values
		m_fitness = 0.0;
	
		m_decisionVariables = variables;
	} // Constructor


	
	public void setFitness(double fitness) {
		m_fitness = fitness;
	} // setFitness

	
	public double getFitness() {
		return m_fitness;
	} // getFitness

	
	public void setObjective(int i, double value) {
		m_objectives[i] = value;
	} // setObjective

	
	public double getObjective(int i) {
		return m_objectives[i];
	} // getObjective
	
	
	public double[] getObjectives() {
		return m_objectives;
	}

	
	public int numberOfObjectives() {
		return m_numberOfObjectives;
	} // numberOfObjectives

	
	@Override
	public String toString() {
		return GaConverter.doublesToString(m_objectives);
	} // toString

	
	public IDecisionVariables getDecisionVariables() {
		return this.m_decisionVariables;
	} // getDecisionVariables

	
	public void setDecisionVariables(IDecisionVariables decisionVariables) {
		this.m_decisionVariables = decisionVariables;
	} // setDecisionVariables

	
	public void setRank(int value) {
		this.m_rank = value;
	} // setRank

	
	public int getRank() {
		return this.m_rank;
	} // getRank

	
} 

