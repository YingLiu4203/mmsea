/**
 * 
 */
package mktgbi.util;

/**
 * @author yingliu
 *
 */
public class GaMath {

	/**
	 * 
	 */
	private GaMath() {
		super();
	}
	
	/**
	 * @param d a double type number
	 * @return The square of the number d.
	 */
	public static double square(double d) {
		return d * d;
	}
	
	/**
	 * @param d1
	 * @param d2
	 * @return Euclidean distance of the two data points
	 */
	public static double calEuclideanDistance(double[] d1, double[] d2) {
		double distance = 0;

		double distnaceSquared = calDistanceSquared(d1, d2);
		distance = Math.sqrt(distnaceSquared);
		return distance;
	}
	
	/**
	 * @param d1
	 * @param d2
	 * @return Squared distance of the two data points
	 */
	public static double calDistanceSquared(double[] d1, double[] d2) {
		double distance = 0;

		for (int ii = 0; ii < d1.length; ii++) {
			distance += GaMath.square(d1[ii] - d2[ii]);
		}
		return distance;
	}
	
}
