/**
 * 
 */
package mktgbi.dataio;

import mktgbi.algorithm.util.Distance;
import mktgbi.algorithm.util.MinSpanTree;
import mktgbi.util.*;

/**
 * @author yingliu
 * 
 */
public class DataBin {
	
	private int m_numRows = 0;

	private int m_numColumns = 0;

	private DistanceType m_distanceType = null;

	// the read in data are stored here.
	private double[][] m_dataTable = null;

	// we may be able to optimize this for z-score data
	private double[] m_centers = null;
	
	// The total sum of squares
	private double m_TSS = 0;
	
	// cached member because not every DataBin needs this
	private int[] m_mstEdges = null; 

	/**
	 * @param dataTable
	 * @param distanceType
	 */
	public DataBin(double[][] dataTable, DistanceType distanceType) {

		m_dataTable = dataTable;
		m_distanceType = distanceType;
		m_numRows = dataTable.length;
		m_numColumns = dataTable[0].length;
		
		m_centers = calDataCenters(m_dataTable);
		calTSS();

	}
	
	// calculate total sum of square of all rows
	private void calTSS() {
		m_TSS = 0;
		for (int row = 0; row < m_numRows; row++) {
			double[] dataRow = m_dataTable[row];
			double distance = GaMath.calDistanceSquared(dataRow, m_centers);
			m_TSS += distance;
		}
	}
	
	private double[] calDataCenters(double[][] dataTable) {
		int numColumns = dataTable[0].length;
		int numRows = dataTable.length;
		double[] retCenters = new double[numColumns];
		for (int column = 0; column < numColumns; column++) {
			double sum = 0;
			for (int row = 0; row < numRows; row++) {
				sum += dataTable[row][column];
			}
			retCenters[column] = sum / numRows;
		}
		return retCenters;
	}
	
	public int[] getMstEdges() {
		if (m_mstEdges == null) {
			MinSpanTree mst = new MinSpanTree();
			m_mstEdges = mst.getMstEdges(this); 
		}
		
		return m_mstEdges;
	}
	/**
	 * @param rows
	 * @return the centers of the rows
	 */
	public double[] calCenters(Integer[] rows) {

		int numRows = rows.length;
		
		double[][] clusterRows = new double[numRows][];

		for (int ii = 0; ii < numRows; ii++) {
			clusterRows[ii] = m_dataTable[rows[ii]];
		}
		
		double[] retCenters = calDataCenters(clusterRows);
		return retCenters;
	}

	

	/**
	 * @param rowIndex The index of the row to be returned
	 * @return The row with specified index
	 */
	public double[] getRow(int rowIndex) {
		return m_dataTable[rowIndex].clone();
	}

	public double[] getColumnByRows(int columnIndex, Integer[] rows) {
		double[] retColumn = new double[rows.length];
		for (int rowIndex = 0; rowIndex < rows.length; rowIndex++) {
			retColumn[rowIndex] = m_dataTable[rows[rowIndex]][columnIndex];
		}

		return retColumn;
	}

	/**
	 * @return Number of columns
	 */
	public int getNumColumns() {
		return m_numColumns;
	}

	/**
	 * @return number of rows
	 */
	public int getNumRows() {
		return m_numRows;
	}

	/**
	 * @return DataBin centers
	 */
	public double[] getCenters() {
		return m_centers;
	}

	/**
	 * @return The total sum of square of all rows
	 */
	public double getTSS() {
		return m_TSS;
	}

	public DistanceType getDistanceType() {
		return m_distanceType;
	}
	
	public int[] getAssigned(double[][] centers) {
		int[] assigned = new int[m_numRows];
		int numOfClusters = centers.length;
		for (int rowIndex = 0; rowIndex < m_numRows; rowIndex++) {
			double[] centerData = centers[0];
			double[] rowData = m_dataTable[rowIndex];
			double minDistance = GaMath.calEuclideanDistance(rowData, centerData);
			int minCenterNum = 0;
			for (int clusterNum = 1; clusterNum < numOfClusters; clusterNum++) {
				centerData = centers[clusterNum];
				double distance = GaMath.calEuclideanDistance(rowData, centerData);
				if (distance < minDistance) {
					minDistance = distance;
					minCenterNum = clusterNum;
				}
			}
			assigned[rowIndex] = minCenterNum;	
		}
		
		return assigned;
	}
	
	
	// we only calculate the low triangle of the distance matrix
	public double[][] calDistances() {
		double[][] distances = Distance.getDistanceMatrix(m_dataTable);
		return distances;
	}
}
