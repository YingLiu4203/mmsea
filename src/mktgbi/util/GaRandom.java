/**
 * 
 */
package mktgbi.util;

import java.util.Random;

/**
 * @author yingliu
 * 
 */
public class GaRandom {
	private static Random sm_randomData = new Random();

	/**
	 * Set the seed of random generator
	 * 
	 * @param seed
	 *            The random seed
	 */
	public static void setSeed(int seed) {
		sm_randomData.setSeed(seed);
	}
	
	/**
	 * Use a new Random with radom seed. 
	 * 
	 */
	public static void setRandom() {
		sm_randomData = new Random();
	}


	/**
	 * This method return a random integer in the range (inclusive). If seed is
	 * not set by reSeed(), it uses the current time as random seed.
	 * 
	 * @param lower
	 *            the lower bound of random number (inclusive)
	 * @param upper
	 *            the upper bound of random number (inclusive)
	 * @return A random integer in the specified inclusive range. If the two
	 *         numbers are equal, the equal value is returned.
	 */
	public static int nextInt(int lower, int upper) {
		if (lower == upper) {
			return lower;
		}

		int range = upper - lower;
		// the nextInt return a random between 0 (inclusive) and
		// its parameter (exclusive) -- we have to add one here to
		// make it inclusive
		int number = sm_randomData.nextInt(range + 1);
		return (lower + number);
	}

	/**
	 * @return a random double number in (0, 1). End point is excluded.
	 */
	public static double nextDouble() {
		return sm_randomData.nextDouble();
	}

}
