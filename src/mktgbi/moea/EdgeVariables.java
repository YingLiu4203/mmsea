package mktgbi.moea;

import mktgbi.algorithm.util.Cluster;
import mktgbi.dataio.Config;
import mktgbi.dataio.DataBin;
import mktgbi.dataio.DataSource;
import mktgbi.util.GaConverter;
import mktgbi.util.GaRandom;

public class EdgeVariables implements IDecisionVariables {
	
	private static final int MIN_INTEGER = Integer.MIN_VALUE;
	
	private int m_numRows = 0;
	
	private int[] m_assigned = null;
	private int[] m_edges = null;
	Cluster[] m_clusters = null;
	
	
	// we don't want people to use this
	private EdgeVariables() {
		m_numRows = Config.getNumRows();
	}
	
	public static EdgeVariables createByEdges(int[] edges) {
		EdgeVariables vars = new EdgeVariables();
		vars.m_edges = edges;
		vars.m_assigned = vars.decodeEdges();
		vars.m_clusters = Cluster.createClusters(vars.m_assigned);
		
		if (!Cluster.checkClusterSizes(vars.m_clusters)) {
			vars = null;
		}
		return vars;
	}
	
	// this method is only used during solution initialization 
	// The size has been checked therefore no need to check the cluster size
	public static EdgeVariables createByAssigned(int[] assigned) {
		EdgeVariables vars = new EdgeVariables();
		
		vars.m_assigned = assigned;
		vars.m_clusters = Cluster.createClusters(assigned);
		
		vars.decodeAssigned();
		return vars;
		
	}
	
	
	// generate edges from assigned decoding
	// if the row and its edgeEnd in initial MST are in the same group
	// the edge end is changed to as MST's edgeEnd. otherwise,
	// randomly assigned to an edge in the same cluster
	private void decodeAssigned() {

		DataBin dataBin = DataSource.getAll();
		int[] mstEdges = dataBin.getMstEdges();
		m_edges = new int[m_numRows];
		for (int row = 0; row < m_numRows; row++) {
			int clusterNum = m_assigned[row];

			int mstEdgeEnd = mstEdges[row];
			int mstClusterNum = m_assigned[mstEdgeEnd];

			if (clusterNum == mstClusterNum) {
				// this line is critical for convergence
				m_edges[row] = mstEdgeEnd;
			} else {
				Cluster cluster = m_clusters[clusterNum];
				int clusterSize = cluster.size();

				int endNum = GaRandom.nextInt(0, clusterSize - 1);
				int edgeEnd = cluster.get_member(endNum);
				m_edges[row] = edgeEnd;
			}
		}
		
		// now we may generate more clusters than original one
		// we should link islands belong to the same cluster together
		fixEdgesByAssigned();
	}

	// usually we generate more clusters than the original assigned
	private void fixEdgesByAssigned() {

		int[] tempAssigned = decodeEdges();
		Cluster[] tempClusters = Cluster.createClusters(tempAssigned);

		int trueNumClusters = m_clusters.length;
		int tempNumClusters = tempClusters.length;

		// used to check if a true cluster number is mapped already by any test cluster
		boolean[] mapped = new boolean[trueNumClusters];
		for (int ii = 0; ii < trueNumClusters; ii++) {
			mapped[ii] = false;
		}
		
		// from real Cluster Num to one of the test Cluster Num
		int[] mappedClusterNumbers = new int[trueNumClusters];

		for (int testIndex = 0; testIndex < tempNumClusters; testIndex++) {
			// a lengthy search but necessary
			Cluster tempCluster = tempClusters[testIndex];
			int tempMember = tempCluster.get_member(0);
			int trueClusterNum = m_assigned[tempMember];

			if (mapped[trueClusterNum]) {
				// we want to point to already mapped test cluster
				int targetTempClusterNum = mappedClusterNumbers[trueClusterNum];
				Cluster targetTempCluster = tempClusters[targetTempClusterNum];
				int targetClusterSize = targetTempCluster.size();
				
				// map all members of the current temp cluster
				// to  target temp cluster members -- randomly
				Integer[] testRows = tempCluster.get_members();

				for (int testRow : testRows) {
					int endNum = GaRandom.nextInt(0, targetClusterSize - 1);
					int edgeEnd = targetTempCluster.get_member(endNum);
					m_edges[testRow] = edgeEnd;
				}

			} else {
				mappedClusterNumbers[trueClusterNum] = testIndex;
				mapped[trueClusterNum] = true;
			}
		}
	}

	
	/**
	 * decode edges and synch assigned and clusters
	 * 
	 */
	private int[] decodeEdges() {

		int[] retAssigned = new int[m_numRows];

		// remember assigned nodes because we may need to to re-assign them
		int[] assignHistory = new int[m_numRows];

		for (int ii = 0; ii < m_numRows; ii++) {
			retAssigned[ii] = MIN_INTEGER;
		}

		int clusterCounter = 0;
		for (int edgeIndex = 0; edgeIndex < m_numRows; edgeIndex++) {

			int histCount = 0;
			if (retAssigned[edgeIndex] == MIN_INTEGER) {
				// try to assign to a new cluster
				retAssigned[edgeIndex] = clusterCounter;
				assignHistory[histCount] = edgeIndex;
				histCount++;

				int neighbour = m_edges[edgeIndex];
				// assign neighbor chain to the same cluster
				while (retAssigned[neighbour] == MIN_INTEGER) {
					retAssigned[neighbour] = clusterCounter;
					assignHistory[histCount] = neighbour;
					histCount++;
					neighbour = m_edges[neighbour];
				}

				// reassign if one neighbor is already in different cluster
				if (retAssigned[neighbour] != clusterCounter) {
					histCount--;
					int oldCluster = retAssigned[neighbour];
					while (histCount >= 0) {
						retAssigned[assignHistory[histCount]] = oldCluster;
						histCount--;
					}
				} else {
					clusterCounter++;
				}
			}
		}

		return retAssigned;
	}
	
	@Override
	public int[] getAssigned() {
		return m_assigned;
	}
	
	@Override
	public int size() {
		return m_edges.length;
	}
	
	
	@Override
	public int[] getEdges() {
		return m_edges.clone();
	}

	@Override
	public double getSegmentCenter(int index) {
		return 0;
	}

	@Override
	public double[] getSegmentCenters() {
		return null;
	}

	
	public int getNumSegments() {
		return m_clusters.length;
	}
	
	@Override
	public String toString() {
		return GaConverter.intsToString(m_edges);
	}
	
	public static EdgeVariables fromString(String line) {
		int[] edges = GaConverter.toInts(line);
		return createByEdges(edges);
	}

}
