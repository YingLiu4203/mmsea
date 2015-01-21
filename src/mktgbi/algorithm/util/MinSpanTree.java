/**
 * 
 */
package mktgbi.algorithm.util;

import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import mktgbi.dataio.*;
import mktgbi.util.Pair;

/**
 * It initalizes edges using MST mktgbi.algorithm.
 * Then each getEdges() will return the current edges 
 * and then remove the first interestingLinks link from the edges.  
 * @author yingliu
 *
 */
public class MinSpanTree {
	
	private static Logger sm_logger = Config.SM_LOGGER;
	
	private static final int MIN_INTEGER = Integer.MIN_VALUE;
	
	private int m_numRows = 0;
	
	private double[][] m_distances = null;

	// sorted (on ascending distance) neighbor matrix
	private int[][] m_neighbours = null;
	
	
	// the edge is defined as an edge of index --> edges[index]
	// edges calculated from MST with a special case: 
	// !!! No Self reference allowed because
	// it is critical to the meaning of neighbor order
	private int[] m_edges = null; 
	
	
	public MinSpanTree() {		
	}
	
	/**
	 * Initalize cluster data for GA mktgbi.algorithm, create first 
	 * cluster with MST and calculate interestingLinks links. 
	 */
	public int[] getMstEdges(DataBin dataBin) { 
		
		sm_logger.fine("Initializing minimum spanning tree (MST)");
		
		m_numRows = dataBin.getNumRows();
		m_edges = new int[m_numRows];
		
		m_neighbours = new int[m_numRows][];
		for (int row = 0; row < m_numRows; row++) {
			m_neighbours[row] = new int[m_numRows - 1];
		}
		
		m_distances = dataBin.calDistances();
		sortNeighbous();
		calMST();
		
		return m_edges;
	
	}
	
	
	private void calMST() {
		// this method implements Prim's Minimum Spanning Tree (MST) 
		// Select an arbitrary node to start
		// While (there are non-MST nodes (fringes)
		//		select minimum-weight edge between tree and fringe
		//		add the selected edge and node to the tree
		// the result is stored in edges. 
		
		// the nodes of the MST, array index is the order of adding
		int[] treeNodes = new int[m_numRows];
		// if the node is in MST
		boolean[] inTreeFlag = new boolean[m_numRows];
	
		for (int ii =0; ii < inTreeFlag.length; ii++) {
			inTreeFlag[ii] = false;
		}
		
		// initialize the first tree node
		treeNodes[0] = 0;
		inTreeFlag[0] = true;
		
		// the loop index is the next insert position in the tree node set
		// it's also the current tree size
		for (int nextTreeIndex = 1; nextTreeIndex < m_numRows; nextTreeIndex++) {
			
			int existingNode = MIN_INTEGER;	// the node in the tree set
			int addingNode = MIN_INTEGER;	// the node to be added next
			
			// find shortest edge between existing nodes and to-be-added nodes
			double minDistance = Double.MAX_VALUE;
			for (int treeIndex = 0; treeIndex < nextTreeIndex; treeIndex++) {	
				int currentNode = treeNodes[treeIndex];
				// find the nearest node that is not in the tree.
				int neighbour = findNearestNeighbour(currentNode, inTreeFlag);
					
				double distance = m_distances[currentNode][neighbour];
				
				// !!! we use < for performance (do nothing for equality)
				// This doesn't work when all distance are MAX_VALUE,  
				if (distance < minDistance) {
					minDistance = distance;
					existingNode = currentNode;
					addingNode = neighbour;
				}
			}
			
			// add node to MST: new node points to the existing one
			m_edges[addingNode] = existingNode;
			treeNodes[nextTreeIndex] = addingNode;
			inTreeFlag[addingNode] = true;
			
		}
		
		// special case to remove self-reference of first node
		// because node 0 is initially in the tree and treeNode[1] is the nearest neighbor
		m_edges[0] = treeNodes[1];
		
		if (sm_logger.isLoggable(Level.FINE) ) {
			sm_logger.fine("In Initial MST,  First edge:" + m_edges[0] 
					+ " Second edge:" + m_edges[1]
					+ " Next to Last edge:" + m_edges[m_edges.length - 2]  
					+ " Last edge:" + m_edges[m_edges.length - 1]);
		}
	}

	//	 find the nearest node that is not in the tree.
	private int findNearestNeighbour(int currentNode, boolean[] inTreeFlag) {
		
		int neighbour = MIN_INTEGER;
		int length = m_neighbours[currentNode].length;
		for (int order = 0; order < length; order++) {
			neighbour = m_neighbours[currentNode][order];
			if(inTreeFlag[neighbour] == false) {
				break;
			}
		}
		return neighbour;
	}
	
	// the purpose is to initialize neighbors matrix in ascending distances
	private void sortNeighbous() {

		// to store and sort the distancePair: (neighbor, distance.) 
		TreeSet<Pair<Integer, Double>> sortedDistance = new TreeSet<Pair<Integer, Double>>();

		for (int row1 = 0; row1 < m_numRows; row1++) {
			// put all neighbor distances into sorted TreeMap
			for (int row2 = 0; row2 < m_numRows; row2++) {
				//we skip the distance to itself
				if (row1 != row2) {
					Double distance = m_distances[row1][row2];
					Pair<Integer, Double> distancePair = new Pair<Integer, Double>(
							row2, distance);
					sortedDistance.add(distancePair);
				}
			}

			// now add sorted neighbors
			int order = 0;
			for (Pair<Integer, Double> dPair : sortedDistance) {
				int neighbour = dPair.getMember1();
				m_neighbours[row1][order] = neighbour;
				order++;
			}
			sortedDistance.clear();
		}
	}
	
}
