package mktgbi.util;

import mktgbi.dataio.Config;


/**
 * @author yingliu
 *
 */
public class GaConverter {
	
	public static String intsToString(int[] array) {
		char delimiter = Config.VARIABLE_DELIMITER;
		StringBuilder builder = new StringBuilder(array.length);
		builder.append(array[0]);
		for (int i = 1; i < array.length; i++) {
			builder.append(delimiter);
			builder.append(array[i]);
		}
		return builder.toString();
	}
	
	public static String doublesToString(double[] array) {
		char delimiter = Config.VARIABLE_DELIMITER;
		StringBuilder builder = new StringBuilder(array.length);
		builder.append(array[0]);
		for (int i = 1; i < array.length; i++) {
			builder.append(delimiter);
			builder.append(array[i]);
		}
		return builder.toString();
	}

	/**
	 * @param array
	 * @param delimiter
	 * @return Convert a dobule array into a string separated by delimiter.
	 */
	public static String fromDoubles(double[] array, String delimiter) {
		StringBuilder builder = new StringBuilder();
		for (double element: array) {
			builder.append(element);
			builder.append(delimiter);
		}
		return builder.toString();
	}
	

	/**
	 * @param line
	 * @param delimiter
	 * @return Convert a string separated by delimiter to an int array
	 */
	public static int[] toInts(String line) {
		
		String delimiter = Character.toString(Config.VARIABLE_DELIMITER);
		String[] values = line.split(delimiter);
		int length = values.length;
		int[] retValue = new int[length];
		
		for(int index = 0; index < length; index++) {
			retValue[index] = Integer.parseInt(values[index]);
		}
		
		return retValue;
		
	}
}
