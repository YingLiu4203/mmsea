package mktgbi.algorithm.util;

import java.util.logging.Logger;

import mktgbi.dataio.Config;
import mktgbi.dataio.DataBin;
import mktgbi.util.GaRandom;

public class ClusterWise extends HeuristicCluster {

	private static Logger sm_logger = Config.SM_LOGGER;
	
		
	public ClusterWise(DataBin dataBin) {
		super(dataBin);
	}

	@Override
	public boolean changeAssigned(int[] assigned, Cluster[] clusters, int maxIterationNumber) {
		
		sm_logger.info("Start Clusterwise Regression.");
		// make a copy of current cluster
		Cluster[] testClusters = copyClusters(clusters);
		
		// start from a random row
		int checkRow = GaRandom.nextInt(0, m_numRows - 1);
		boolean isChanged = true;
		int iterationIndex = 0; 
		// stop when all rows are checked and no membership changes
		while(isChanged && (iterationIndex < maxIterationNumber)) {
			sm_logger.info("Clusterwise at iteration: " + iterationIndex);
			isChanged = false;
			iterationIndex++;
			
			for(int rowCount = 0; rowCount < m_numRows; rowCount++) {
				
				int clusterNum = assigned[checkRow];
				// We don't want to take members from the cluster whose size is too smaller
				if (testClusters[clusterNum].size() <= Config.getMinClusterSize()) {
					continue;
				}
				
				if(checkRow(checkRow, assigned, testClusters)) {
					isChanged = true;
				}
				checkRow++;
				
				if (checkRow == m_numRows) {
					checkRow =0;
				}
			}
		}
		
		sm_logger.info("Clusterwise finishes at iteration: " + iterationIndex);
		
		return true;
	}
	
	private Cluster[] copyClusters(Cluster[] clusters) {
		Cluster[] testClusters = new Cluster[clusters.length];
		int clusterIndex = 0;
		for (Cluster originalCluster: clusters) {
			testClusters[clusterIndex] = new Cluster(originalCluster);
			clusterIndex++;
		}
		
		return testClusters;
	}
	
	private boolean checkRow(int rowIndex, int[] assigned, Cluster[] testClusters) {
		
		boolean isChanged = false;
		
		int clusterNum = assigned[rowIndex];
		int minClusterNum = clusterNum;
		
		
		// set minimum to current objective value
		double minObjective = RegressionObjective.calResponseObj(
				m_dataBin, testClusters);
		
		// remove row from current cluster and test all other clusters
		testClusters[clusterNum].remove(rowIndex);
		
		// find the new cluster number that minimize the objective
		for(int clusterId = 0; clusterId < testClusters.length; clusterId++) {
			
			if (clusterId != clusterNum) { 
				// tentative moving current member
				testClusters[clusterId].add(rowIndex);
				
				double changedObjective = RegressionObjective.calResponseObj(
						m_dataBin, testClusters);
				
				if (changedObjective < minObjective) {
					minObjective = changedObjective;
					minClusterNum = clusterId;
					isChanged = true;
				}
				
				// in both case we need to undo the quickAdd to 
				// let other cluster to try
				testClusters[clusterId].remove(rowIndex);
				
			}
		}
		
		if (isChanged) {
			assigned[rowIndex] = minClusterNum;
			// make the change permanent
			testClusters[minClusterNum].add(rowIndex);
			
		} else {
			// nothing changed
			testClusters[clusterNum].add(rowIndex);
		}
		
		return isChanged;
	}

}
