package mktgbi.moea.problem;

import java.util.ArrayList;

import mktgbi.algorithm.util.Cluster;
import mktgbi.algorithm.util.ClusterWise;
import mktgbi.algorithm.util.HeuristicCluster;
import mktgbi.algorithm.util.Kmeans;
import mktgbi.dataio.DataBin;

import mktgbi.moea.EdgeVariables;
import mktgbi.moea.Solution;

public class RegressionSeg extends Problem {
	
	private DataBin m_allDataBin = null;
	
	public RegressionSeg () {
		super();
		
		m_numMixedMethods = 4;
		m_allDataBin = m_dataBins[m_dataBins.length -1];
	}
	
	@Override
	protected void initializeSegmentSolutions(ArrayList<Solution> solutions, int initSize, 
			int numSegments) {
	
		int dimSize = initSize / m_numMixedMethods;
		
		HeuristicCluster clusterWise = new ClusterWise(m_allDataBin);
		HeuristicCluster kmeansOne = new Kmeans(m_dataBins[1]);
		HeuristicCluster kmeansAll = new Kmeans(m_dataBins[m_dataBins.length -1]);
		
		// create solutions from each dimension
		// 1. pure ClusteWise
		createSolutions(solutions, clusterWise, dimSize, numSegments);
		sm_logger.info("Solution set is initialized with ClusterWise. Size: " + solutions.size()); 
		
		// 2. to save time, we use the above clusterwise results and assign by data centers
		int size = solutions.size();
		int startIndex = size - dimSize;
		for (int index = startIndex; index < size; index++) {
			Solution solution = solutions.get(index);
			Solution solution2 = createByCenters(solution);
			if (solution2 != null) {
				boolean saveFlag = checkObjectiveDistance(solution2, solutions);
				if (saveFlag) {
					solutions.add(solution);
				}
			}
		}
		sm_logger.info("Solution set is initialized by resetting members by centers. Size:" 
				+ solutions.size()); 
			
		// 3. k-means + cluserwise
		createSolutions(solutions, kmeansOne, clusterWise, dimSize, numSegments);
		sm_logger.info("Solution set is initialized with K-means followed by ClusterWise. Size: " 
				+ solutions.size()); 
		
		// 4. kmeans for all DataBin		
		int halfSize = dimSize / 2; 
		createSolutions(solutions, kmeansAll, halfSize, numSegments);
		
		// the rest are kmeans for the 2nd DataBin dimension
		int restSize = dimSize - halfSize;
		createSolutions(solutions, kmeansOne, restSize, numSegments);
		sm_logger.info("Solution set is initialized by K-means. Size: " + solutions.size()); 
		
	}
	
	private void createSolutions(ArrayList<Solution> solutions, HeuristicCluster cluster, 
			int size, int numSegments) {
		
		for (int currentSize = 0; currentSize < size; currentSize++) {
			Solution solution = createSolution(cluster, numSegments);
			evaluate(solution);
			// make sure the solution is not too close to an existing one
			boolean saveFlag = checkObjectiveDistance(solution, solutions);
			if (saveFlag) {
				solutions.add(solution);
			} else {
				// try again
				currentSize--;
			}
		}
	}
	
	private Solution createByCenters(Solution solution) {
		int[] assigned = solution.getDecisionVariables().getAssigned();
		double[][] assignedCenters = Cluster.calCenters(m_allDataBin, assigned);
		assigned = m_allDataBin.getAssigned(assignedCenters);
		EdgeVariables vars = EdgeVariables.createByAssigned(assigned);
		Solution solution2 =  new Solution(vars);
		evaluate(solution2);
		return solution2;
	}
	
	// same logic as above except the createSolution() method
	private void createSolutions(ArrayList<Solution> solutions, HeuristicCluster clusterOne, 
			HeuristicCluster clusterTwo, int size, int numSegments) {
		
		for (int currentSize = 0; currentSize < size; currentSize++) {
			Solution solution = createSolution(clusterOne, clusterTwo, numSegments);
			evaluate(solution);
			// make sure the solution is not too close to an existing one
			boolean saveFlag = checkObjectiveDistance(solution, solutions);
			if (saveFlag) {
				solutions.add(solution);
			} else {
				// try again
				currentSize--;
			}
		}
	}
	
	protected Solution createSolution(HeuristicCluster clusterOne, HeuristicCluster clusterTwo,
			int numSegments) {
		int[] assigned = null;
		while (true) {
			assigned = clusterOne.run(numSegments);
			if (clusterTwo.runWithAssigned(assigned, 1)) {
				break;
			}
		}
		
		EdgeVariables vars = EdgeVariables.createByAssigned(assigned);
		Solution solution = new Solution(vars);
		return solution;
	}
}
