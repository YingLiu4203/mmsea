/**
 * 
 */
package mktgbi.algorithm.util;

import mktgbi.dataio.*;
import mktgbi.util.GaMath;
import java.util.*;

/**
 * @author yingliu
 *
 */
public class Cluster {
	
	private ArrayList<Integer> m_members = null;
	
	/**
	 * copy constructor
	 * @param cluster
	 */
	public Cluster(Cluster cluster) {
		
		int numMembers = cluster.m_members.size();
		m_members = new ArrayList<Integer>(numMembers);
		m_members.addAll(cluster.m_members);
	}
	
	
	/**
	 * The only way to create cluster is by createClusters() 
	 * that guarantees the calculation of centers
	 * @param dataBin 
	 * @param clusterNum The id of the cluster.
	 * @param memberNum 
	 * 
	 */
	private Cluster(ArrayList<Integer> members) {
		m_members = members;
	}
	
	
	/**
	 * @return The array of member Ids.
	 */
	public Integer[] get_members() {
		
		Integer[] rows = new Integer[m_members.size()];
		m_members.toArray(rows);
		return rows;
	}
	
	/**
	 * @param index
	 * @return the member in the specified index
	 */
	public int get_member(int index) {
		return m_members.get(index);
	}
	
	
	/**
	 * @param rowNum The row number to be added
	 */
	public void add(int rowNum) {
		m_members.add(rowNum);
	}
	
	
	/**
	 * remove row number with out calculate centers
	 * @param rowNum
	 */
	public void remove(int rowNum) {
		
		if (m_members.size() <= 1) {
			throw (new IllegalStateException("About to remove cluster with only one member."));
		}
		// has to convert rowNum to Object, 
		// otherwise, it is interpreted as the position index to be removed
		Integer row = new Integer(rowNum);
		boolean found = m_members.remove(row);
		
		if (!found) {
			throw (new IllegalStateException("cluster doesn't have row number: " + rowNum));
		}
	}
	
	
	/**
	 * used by Kmeans to filee empty cluster
	 * @return The size (number of members) of this cluster
	 */
	public int size() {
		return m_members.size();
	}
	
	
	/**
	 * We asssume no-gap continuous cluster number from 0 to assigned number
	 * @param dataBin 
	 * @param assigned The assign rows
	 * @return The cluster array
	 */
	@SuppressWarnings("unchecked")
	public static Cluster[] createClusters(int[] assigned) {
		// find the number of clusters
		int maxClusterNum = 0;
		for (int ii = 0; ii < assigned.length; ii++) {
			int clusterNum = assigned[ii];
			if (clusterNum > maxClusterNum) {
				maxClusterNum = clusterNum;
			}
		}
		int numClusters = maxClusterNum + 1;
		
		Cluster[] clusters = new Cluster[numClusters];
		ArrayList<Integer>[] membersArray = new ArrayList[numClusters];
		
		for (int clusterIndex = 0; clusterIndex < numClusters; clusterIndex++ ) {
			membersArray[clusterIndex] = new ArrayList<Integer>();
		}
		
		for (int row = 0; row < assigned.length; row++) {
			int clusterNum = assigned[row];
			membersArray[clusterNum].add(row);
		}
		
		for (int clusterIndex = 0; clusterIndex < numClusters; clusterIndex++ ) {
			clusters[clusterIndex] = new Cluster(membersArray[clusterIndex]);
		}
		
		return clusters;
	}
	
	public static boolean checkClusterSizes(Cluster[] clusters) {
		boolean retValue = true;
		
		int minSize = Config.getMinClusterSize();
		for (Cluster cluster: clusters) {
			if (cluster.size() < minSize) {
				retValue = false;
				break;
			}
		}
		return retValue;
	}
	
 
	/**
	 * @param dataBin 
	 * @param clusters
	 */
	public static double[][] calCenters(DataBin dataBin, Cluster[] clusters) {
		int numClusters = clusters.length; 
		double[][] retValues = new double[numClusters][];
		for (int index = 0; index < numClusters; index++) {
			Integer[] members = clusters[index].get_members();
			retValues[index]= dataBin.calCenters(members);
		}
		return retValues;
	}
	
	/**
	 * @param dataBin 
	 * @param clusters
	 */
	public static double[][] calCenters(DataBin dataBin, int[] assigned) {
		Cluster[] clusters = createClusters(assigned);
		double[][] retValues = calCenters(dataBin, clusters);
		return retValues;
	}
	
	/**
	 * @param dataSource 
	 * @param clusters 
	 * @return Objectives
	 */
	
	// this is called only inside regular run
	public static double[] calObjectives(int[] assigned) {
		
		double[] objectives = null;
		DataBin dataBin = DataSource.getDataBin(0);
			
		if (dataBin.getDistanceType() == DistanceType.NONE) {
			objectives = calObjectives2(assigned, true);
		} else {
			objectives = calObjectives2(assigned, false);
		}
		
		return objectives;
		
	}
	
	
	// some time we calculate linear objectives for joint segmentation
	// this is to be called by analysis code after regular run
	public static double[] calObjectives2(int[] assigned, boolean isLinear) {
		
		Cluster[] clusters = createClusters(assigned); 
		
		int numObjectives = Config.getNumObjectives();
		double[] objectives = new double[numObjectives];
		
	 
		for (int ii = 0; ii < numObjectives; ii++) {
			DataBin dataBin = DataSource.getDataBin(ii);
			
			if (ii == 0 && isLinear) {
				// use all data for response objective
				objectives[ii] = RegressionObjective.calResponseObj(
						DataSource.getAll(), clusters);
			} else {
				objectives[ii] = calWGES(dataBin, clusters);
			}
		}
		
		return objectives;
		
	}
	
	/**
	 * calculate the total deviation of the clusters.
	 * Cluster centers must be initialized by calCenters. 
	 * @param dataBin 
	 * 
	 * @param clusters
	 * @return the Within Group Eta Squared (WGES) 
	 */
	private static double calWGES(DataBin dataBin, Cluster[] clusters) {
		
		double retValue = 0;
		
		
		double bgss = 0;
		// find the total of between group sum of sqaures
		// this is faster than calculating WGSS directly
		for (Cluster cluster: clusters) {
			
			double[] clusterCenters = dataBin.calCenters(cluster.get_members());
			double[] dataBinCenters = dataBin.getCenters();
			 
			bgss += cluster.size() * GaMath.calDistanceSquared(
					clusterCenters, dataBinCenters);
		}
		
		// eta squared = sume of within group suares / total sum of square
		double tss = dataBin.getTSS();
		double wgss = tss - bgss;
		retValue = wgss / tss;
		
		return retValue;
	}
}
