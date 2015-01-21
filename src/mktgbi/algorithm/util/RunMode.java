package mktgbi.algorithm.util;

public enum RunMode {
	/**
	 * Run in the start-to-finish mode, do not save intermediate results
	 */
	DEFAULT,
	
	/**
	 * Run in the start-to-finish mode and save intermediate results
	 */
	
	DETAIL,
	
	/**
	 * read (not generate) chromosome from file for regression segmentation, 
	 * do not save intermediate results 
	 */
	READ_CHROMS,
	
	// read chromosome, save intermediate results
	READ_DETAIL
	
}
