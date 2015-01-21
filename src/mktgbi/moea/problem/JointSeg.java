package mktgbi.moea.problem;

import java.util.ArrayList;

import mktgbi.algorithm.util.HeuristicCluster;
import mktgbi.algorithm.util.Kmeans;
import mktgbi.moea.Solution;

public class JointSeg extends Problem {

	public JointSeg() {
		super();
		m_numMixedMethods = 3;
	}

	@Override
	protected void initializeSegmentSolutions(ArrayList<Solution> solutions, int initSize,
			int numSegments) {
				
		HeuristicCluster[] kmeansArray = new Kmeans[m_dataBins.length];
		
		for (int objIndex = 0; objIndex < m_dataBins.length; objIndex++) {
			kmeansArray[objIndex] = new Kmeans(m_dataBins[objIndex]);
		}
		
		while (solutions.size() < initSize) {
			// create solutions from each dimension
			for (int objIndex = 0; objIndex < m_dataBins.length; objIndex++) {
				Solution solution = createSolution(kmeansArray[objIndex], numSegments);
				evaluate(solution);
				// make sure the solution is not too close to an existing one
				boolean saveFlag = checkObjectiveDistance(solution, solutions);
				if (saveFlag) {
					solutions.add(solution);
					if (solutions.size() == initSize) {
						break;
					}
				} 
			}
		}
	}

}