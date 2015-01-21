package mktgbi.algorithm.util;

import java.util.logging.Logger;

import mktgbi.dataio.Config;
import mktgbi.dataio.DataBin;
import mktgbi.util.GaMath;
import mktgbi.util.GaRandom;

public  abstract class HeuristicCluster {
	private static Logger sm_logger = Config.SM_LOGGER;
	
	 
	private int m_maxIterationNumber = -1;
	
	protected DataBin m_dataBin = null;
	protected int m_numRows = 0;
	
	// we use two different initialization modes
	private static boolean m_initByCenter = true;  
	private static int INVALID_VALUE = -1;

	public HeuristicCluster(DataBin dataBin) {
		super();
		m_dataBin = dataBin;
		m_numRows = m_dataBin.getNumRows();
		//m_numClusters = Config.getNumSegments();
		m_maxIterationNumber = Config.getMaxIterationNumber();
	}

	/**
	 * Generate specified (no-empty) number of clusters using k-means 
	 * @param numClusters
	 * @param numIterations 
	 * @return Assigned clusters.
	 */
	public int[] run(int numClusters) {
		
		int[] assigned = null;
		while (true) {
			if (m_initByCenter) {
				m_initByCenter = false;
				assigned = randomClusterByCenter(numClusters);
			} else {
				m_initByCenter = true;
				assigned = randomClusterByRow(numClusters);
			}
			
			// generate the clusters for initial assigned
			if (runWithAssigned(assigned)) {
				break;
			}
		}
		return assigned;
	}
	
	// return false if there is an invalid cluster
	public boolean runWithAssigned(int[] assigned) {
		return (runWithAssigned(assigned, m_maxIterationNumber));
	}
	
	// return false if there is an invalid cluster
	public boolean runWithAssigned(int[] assigned, int maxIterationNumber) {
		boolean retValue = false;
		// generate the clusters for initial assigned
		Cluster[] clusters = Cluster.createClusters(assigned);
		if(Cluster.checkClusterSizes(clusters)) {
			if (changeAssigned(assigned, clusters, maxIterationNumber)) {
				retValue = true;
			}
		}
		return retValue;
	}

	// we randomly select K nodes as the center of the cluster
	// and assign each row to those clusters
	private int[] randomClusterByCenter(int numClusters) {
		int[] assigned = new int[m_numRows];
		int[] centerIndex = initRandomCenters(numClusters);
		for (int row = 0; row < m_numRows; row++) {
			// find the nearest from the centers
			// first assign it to the first
			int assignedNum = 0;
			double[] rowData = m_dataBin.getRow(row);
			double[] assignedData = m_dataBin.getRow(centerIndex[assignedNum]);
			double closest = GaMath.calEuclideanDistance(rowData, assignedData);
			
			for (int clusterIndex = 1; clusterIndex < numClusters; clusterIndex++) {
				assignedData = m_dataBin.getRow(centerIndex[clusterIndex]);
				double distance = GaMath.calEuclideanDistance(rowData, assignedData);
				if (distance < closest) {
					closest = distance;
					assignedNum = clusterIndex;
				}
			}
			assigned[row] = assignedNum;
		}
		
		sm_logger.fine("Random egeds are generated");
		return assigned;
	}
	
	
	private int[] initRandomCenters(int numClusters) {
		
		int[] centerIndex = new int[numClusters];
		
		centerIndex[0] = GaRandom.nextInt(0, m_numRows - 1);
		// there is a small chance that we pick the same number more than once
		for (int clusterIndex = 1; clusterIndex < numClusters; clusterIndex++) {
			int randomInt = -1; // an invalid initial value
			boolean uniqueFlag = false;
			while (!uniqueFlag) {
				uniqueFlag = true;
				randomInt = GaRandom.nextInt(0, m_numRows - 1);
				for (int ii = 0; ii < clusterIndex; ii++) {
					if (centerIndex[ii] == randomInt) {
						uniqueFlag = false;
						break;
					}
				}
			}
			centerIndex[clusterIndex] = randomInt;
		}
		return centerIndex;
	}
	
	private int[] randomClusterByRow(int numClusters) {
		// start with invalid assignments 
		int[] assigned = new int[m_numRows];
		for (int row = 0; row < m_numRows; row++) {
			assigned[row] = INVALID_VALUE;
		}
		
		// !!! gurantee to generate the specified number of cluster
		// otherwise, Cluster.createClusters() may fail
		// assign one member to each cluster number
		for (int num = 0; num < numClusters; num++) {
			int row = GaRandom.nextInt(0, m_numRows - 1);
			// to make sure it's unassigned
			while (assigned[row] != INVALID_VALUE) {
				row++;
				if (row == m_numRows) {
					row = 0;
				}
			}
			assigned[row] = num;  // each is a cluster by itself
		}
		
		for (int row = 0; row < m_numRows; row++) {
			if (assigned[row] == INVALID_VALUE) {
				assigned[row] = GaRandom.nextInt(0, numClusters - 1);
			}
		}
		
		return assigned;
	}

	public abstract boolean changeAssigned(int[] assigned, Cluster[] clusters, 
			int maxIterationNumber);

	public final DataBin getDataBin() {
		return m_dataBin;
	}

}