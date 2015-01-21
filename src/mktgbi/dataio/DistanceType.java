/**
 * 
 */
package mktgbi.dataio;

/**
 * @author yingliu
 *
 */
public enum DistanceType {
	
	/**
	 * no need for distance -- a response variable
	 */
	NONE,
	
	/**
	 * Euclidean distance. 
	 */
	EUCLIDEAN, 
	
	/**
	 * Cosine distance
	 */
	COSINE,
	
	/**
	 * Correlation distance
	 */
	
	CORRELATION,
	
	/**
	 * JACCARD distance
	 */
	JACCARD
}
