package mktgbi.moea;

import mktgbi.algorithm.util.Cluster;
import mktgbi.dataio.Config;
import mktgbi.dataio.DataBin;
import mktgbi.dataio.DataSource;
import mktgbi.util.GaConverter;

public class CenterVariables implements IDecisionVariables {
	/**
	 * Stores the decision variables of a solution
	 */
	private double[] m_variables = null;
	private int m_numOfCenters = 0;
	
	// the segment membership array
	private int[] m_assigned = null;
	

	/**
	 * Constructor
	 * 
	 * @param problem
	 *            The problem to solve
	 */
	public CenterVariables(int[] assigned, double[][] centers) {
		m_assigned = assigned;
		m_variables = flatCenters(centers);
	}
	
	private double[] flatCenters(double[][] centers) {
		// make it one dimension
		m_numOfCenters = centers.length;
		
		int centerLength = centers[0].length;
		int numOfVariables = m_numOfCenters * centerLength;
		
		double[] retValue = new double[numOfVariables];
		for (int centerIndex = 0; centerIndex < m_numOfCenters; centerIndex++) {
			for (int i = 0; i < centerLength; i++) {
				int variableIndex = i * centerIndex + i; 
				retValue[variableIndex] = centers[centerIndex][i];
			}
		}
		
		return retValue;
	}

	public CenterVariables(double[] variables) {
		int numOfVars = variables.length;
		m_numOfCenters = numOfVars / Config.getNumColumns();
		
		m_variables = new double[numOfVars];
		for (int index = 0; index < numOfVars; index++) {
			m_variables[index] = variables[index];
		}
		setAssigned();
	}
	
	
	private void setAssigned() {
		int centerLength = m_variables.length / m_numOfCenters;
		double[][] centers = new double[m_numOfCenters][];
		for (int centerIndex = 0; centerIndex < m_numOfCenters; centerIndex++) {
			centers[centerIndex] = new double[centerLength];
			for (int i = 0; i < centerLength; i++) {
				int variableIndex = i * centerIndex + i; 
				centers[centerIndex][i] = m_variables[variableIndex];
			}
		}
		DataBin allDataBin = DataSource.getAll();
		m_assigned = allDataBin.getAssigned(centers);
		Cluster[] clusters = Cluster.createClusters(m_assigned);
		if (Cluster.checkClusterSizes(clusters)) {
			// set to real center
			double[][] assignedCenters = Cluster.calCenters(allDataBin, m_assigned);
			m_variables = flatCenters(assignedCenters);
		} else {
			m_assigned = null;
			m_variables = null;
		}
	}
	
	/**
	 * Copy constructor
	 * 
	 * @param decisionVariables
	 *            The <code>DecisionVariables<code> object to copy.
	 */
	public CenterVariables(CenterVariables decisionVariables) {
		m_variables = decisionVariables.m_variables.clone();
		m_assigned = decisionVariables.m_assigned.clone();
		m_numOfCenters = decisionVariables.m_numOfCenters;
	} // DecisionVariable


	public int size() {
		return m_variables.length;
	} // size
	
	
	public double getSegmentCenter(int index) {
		return m_variables[index];
	}
	
	
	public double[] getSegmentCenters() {
		return m_variables;
	}
	
	
	public void setDoubleVariables(double[] variables) {
		m_variables = variables;
		setAssigned();
	}
	
	
	public int[] getAssigned() {
		return m_assigned;
	}

	
	@Override
	public String toString() {
		return GaConverter.intsToString(m_assigned);
	}
	
	public static CenterVariables fromString(String line) {
		int[] assigned = GaConverter.toInts(line);
		DataBin dataBin = DataSource.getAll();
		double[][] centers = Cluster.calCenters(dataBin, assigned);
		CenterVariables dv = new CenterVariables(assigned, centers);
		return dv;
	}
	
	@Override
	public int[] getEdges() {
		return null;
	}
	
	public int getNumSegments() {
		return m_numOfCenters;
	}

	
}
