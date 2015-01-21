
package mktgbi.algorithm.util;

import mktgbi.moea.Solution;
import mktgbi.moea.SolutionSet;
import mktgbi.util.GaMath;

/**
 * This class implements some facilities for distances
 */
public class Distance {

	/**
	 * Returns a matrix with distances between solutions in a
	 * <code>SolutionSet</code>.
	 * 
	 * @param solutionSet
	 *            The <code>SolutionSet</code>.
	 * @return a matrix with distances.
	 */
	public static double[][] getDistanceMatrix(SolutionSet solutionSet) {

		double[][] objectives = new double[solutionSet.size()][];
		for (int i = 0; i < solutionSet.size(); i++) {
			Solution solution = solutionSet.get(i);
			double[] solutionObjectives = solution.getObjectives();
			objectives[i] = solutionObjectives;
		}
		
		double[][] distance = getDistanceMatrix(objectives);
		return distance;
	} // distanceMatrix
	
	
	public static double[][] getDistanceMatrix(double[][] dataTable) {
		int tableLength = dataTable.length;

		// The matrix of distances
		double[][] distance = new double[tableLength][tableLength];
		// -> Calculate the distances
		for (int i = 0; i < tableLength; i++) {
			distance[i][i] = 0.0;
			double[] iValues = dataTable[i];
			for (int j = i + 1; j < tableLength; j++) {
				double[] jValues = dataTable[j];
				distance[i][j] = GaMath.calEuclideanDistance(iValues, jValues);
				distance[j][i] = distance[i][j];
			} // for
		} // for

		// ->Return the matrix of distances
		return distance;
	} // distanceMatrix

} // Distance

