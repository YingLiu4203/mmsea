/**
 * 
 */
package mktgbi.algorithm.util;

import java.util.logging.Logger;

import mktgbi.dataio.Config;
import mktgbi.dataio.DataBin;
import mktgbi.util.GaMath;

/**
 * @author yingliu
 * 
 */
public class Kmeans extends HeuristicCluster {

	private static Logger sm_logger = Config.SM_LOGGER;

	
	/**
	 * @param dataBin
	 * 
	 */
	public Kmeans(DataBin dataBin) {
		super(dataBin);
	}
	

	@Override
	public boolean changeAssigned(int[] assigned, Cluster[] clusters, int maxIterationNumber) {
	
		boolean retValue = true;
		
		int iteration = 0;
		boolean isChanged = true;
		double[][] clusterCenters = Cluster.calCenters(m_dataBin, clusters);

		while ((isChanged == true) && (iteration < maxIterationNumber)) {
			isChanged = false;
			iteration++;
			for (int rowIndex = 0; rowIndex < m_numRows; rowIndex++) {
				
				// get the distance to its center
				int oldClusterNum = assigned[rowIndex];
				
				int closestClusterNum = findNearestCenter(assigned, 
						rowIndex, clusterCenters);
			
				if (oldClusterNum != closestClusterNum) {
					if ( clusters[oldClusterNum].size() > 1) {
						assigned[rowIndex] = closestClusterNum;
						clusters[closestClusterNum].add(rowIndex);
						clusters[oldClusterNum].remove(rowIndex);
						isChanged = true;
					} else {
						// it is possible that the cluster to be removed has only one member
						continue;
					}
				} 
			} // end for
						
			// we calculate cluster center after all rows are assigned
			if (isChanged) {
				// we don't need to check cluster size here because it is true only if the cluster 
				// size is bigger or equal to 1
				clusterCenters = Cluster.calCenters(m_dataBin, clusters);	
			}	
		}
		
		if (!Cluster.checkClusterSizes(clusters)) {
			retValue = false;
		}

		sm_logger.fine("Kmeans done after " + iteration + " iterations");
		
		return retValue;
	}
	
	private int findNearestCenter(int[] assigned, int rowIndex, 
			double[][] centers) {
		
		int oldClusterNum = assigned[rowIndex];
		int retValue = oldClusterNum;
		
		double[] center = centers[oldClusterNum];
		double[] rowData = m_dataBin.getRow(rowIndex);
		double distance = GaMath.calEuclideanDistance(rowData, center);
		
		for (int num = 0; num < centers.length; num++) {
			if (oldClusterNum != num) {
				double[] otherCenter = centers[num];
				double otherDistance = GaMath.calEuclideanDistance(rowData, otherCenter);

				if (otherDistance < distance) {
					distance = otherDistance;
					retValue = num;
				}
			}
		}
		
		return retValue;
	}
	
}
