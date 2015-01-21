package mktgbi.algorithm.util;

import mktgbi.dataio.DataBin;

public class RandomInit extends HeuristicCluster {

	public RandomInit(DataBin dataBin) {
		super(dataBin);
	}
	
	/**
	 * @param assigned 
	 * @param clusters  
	 */
	@Override
	public  boolean changeAssigned(int[] assigned, Cluster[] clusters, int numIteration) {
		
		// do nothing
		// used go generate random chromosomes
		
		return true;
	}
}
