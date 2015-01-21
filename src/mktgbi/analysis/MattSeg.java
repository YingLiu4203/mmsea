package mktgbi.analysis;

import java.io.BufferedReader;

import java.io.FileReader;
import java.io.IOException;
import java.util.logging.*;
import java.awt.geom.Point2D; 


/*
 * It uses the raw data file, solution members.txt and SegmentSizes.txt 
 * to calculate the segment position in the segmentation quadrants.
 * The weighted sum method is more stable than weighted product method. 
 * The algorithm output is stored in log file. 
 * 
 * You may need to set two parameters to use it: 
 * 1) number of solutions 
 * 2) number of segments. 
 *  
 */
public class MattSeg {
	
	private static final Logger sm_logger = Logger.getLogger("MattSeg");
	private static String LOG_FILENAME = "output\\MattSeg.log";
	
	private static final String FIELD_DELIMITER_REGEX = "\\s";
	
	private static String m_dataFilename = "data\\MattJointSeg.dat";
	private static int m_numRows = 838;
	private static int m_d1Size = 10;
	private static int m_d2Size = 6;
	private static int m_numColumns = m_d1Size + m_d2Size;
	
	private double[][] m_data1 = null;
	private double[][] m_data2 = null;
	private double[] m_data1Avg = null;
	private double[] m_data1StdDev = null;
	private double[] m_data2Avg = null;
	private double[] m_data2StdDev = null;
	
	
	private static String m_memberFilename = "data\\members.txt";
	private static int m_numSolutions = 300;
	private int[][] m_members = null;
	
	private static String m_segSizeFilename = "data\\SegmentSizes.txt";
	private int[][] m_segSizes = null;
	private static int m_numSegments = 4;
	private Point2D.Double[][] m_segWsm = null;
	private Point2D.Double[][] m_segWpm = null;
	
	/**
	 * @param args
	 * @throws IOException 
	 * @throws SecurityException 
	 */
	public static void main(String[] args) throws SecurityException, IOException {
		
		FileHandler sm_fileHandler = new FileHandler(LOG_FILENAME);
		sm_logger.addHandler(sm_fileHandler);
		
		
		MattSeg mattSeg = new MattSeg();
		mattSeg.run();
		
	}

	private void run() throws SecurityException, IOException {
		
		sm_logger.info("MattSeg Analysis starts.");
		
		readData();
		calData();
		
		// read solution members
		m_members = readIntArrays(m_numSolutions, m_numRows, m_memberFilename);
		
		// read solution segment sizes
		m_segSizes = readIntArrays(m_numSolutions, m_numSegments, m_segSizeFilename);
		
		getSolutionSegments();
		calWsmScores();
		calWpmScores();
		
	}

	
	private void calWsmScores() {
		double maxQDistance = 0;
		int maxQSolutionId = 0;
		int maxQTotal = 0;
		
		for (int solutionIndex = 0; solutionIndex < m_numSolutions; solutionIndex++) {
			double distanceSum = 0;
			// four quadrants are zero at the beginning
			int q1=0, q2=0, q3=0, q4 = 0;
			for (int segIndex = 0; segIndex < m_numSegments; segIndex++) {
				Point2D.Double solutionPoint = m_segWsm[solutionIndex][segIndex];
				distanceSum += solutionPoint.distance(0,0);
				
				if (solutionPoint.x > 0) {
					if (solutionPoint.y > 0) {
						q1 = 1;
					} else {
						q4 = 1;
					}
				} else {
					if (solutionPoint.y > 0) {
						q2 = 1;
					} else {
						q3 = 1;
					}
				}
			}
			
			int qTotal = q1 + q2 + q3 + q4;
			// sometimes we couldn't find solutions in 4 quadrants.
			if (qTotal > maxQTotal) {
				sm_logger.info("Current Best Solution: " + solutionIndex + 
					" WSM value is: " + distanceSum +
					" number of quadrants is: " + qTotal);
				maxQTotal = qTotal;
				maxQDistance = distanceSum;
				maxQSolutionId = solutionIndex;
				
				for (int segIndex = 0; segIndex < m_numSegments; segIndex++) {
					sm_logger.info("Segment " + segIndex + " values: " 
							+ m_segWsm[maxQSolutionId][segIndex]);
				}
				
			} else if (qTotal == maxQTotal) {
				if (distanceSum > maxQDistance) {
					sm_logger.info("Current Best Solution: " + solutionIndex + 
						" WSM value is: " + distanceSum +
						" number of quadrants is: " + qTotal);
					maxQDistance = distanceSum;
					maxQSolutionId = solutionIndex;
				}
			}
		}
		
		sm_logger.info("Best Solution Index: " + maxQSolutionId 
				+ " Distance Sum is: " + maxQDistance);
		
		for (int segIndex = 0; segIndex < m_numSegments; segIndex++) {
			sm_logger.info("Segment " + segIndex + " values: " 
					+ m_segWsm[maxQSolutionId][segIndex]);
		}
		
	}
	
	private void calWpmScores() {
		double maxQ4Distance = 0;
		int maxQ4SolutionId = 0;
		
		for (int solutionIndex = 0; solutionIndex < m_numSolutions; solutionIndex++) {
			double distanceProd = 1;
						
			// four quadrants are zero at the beginning
			int q1=0, q2=0, q3=0, q4 = 0;
			for (int segIndex = 0; segIndex < m_numSegments; segIndex++) {
				Point2D.Double solutionPoint = m_segWpm[solutionIndex][segIndex];
				double x =solutionPoint.x;
				double y =solutionPoint.y;
				
				
				if (solutionPoint.x > 1) {
					if (solutionPoint.y > 1) {
						q1 = 1;
					} else {
						q4 = 1;
						y = 1/y;
					}
				} else {
					x = 1 / x;
					if (solutionPoint.y > 1) {
						q2 = 1;
					} else {
						q3 = 1;
						y = 1 / y;
					}
				}
				distanceProd *= (x * y);
			}
			
			
			int qTotal = q1 + q2 + q3 + q4;
			if (qTotal == 4) {
				sm_logger.info("Solution Index: " + solutionIndex + " WPM value is: " + distanceProd);
				if (distanceProd > maxQ4Distance) {
					maxQ4Distance = distanceProd;
					maxQ4SolutionId = solutionIndex;
				}
			}
		}
		
		sm_logger.info("Best WPM Solution: " + maxQ4SolutionId + " Distance Product is: " + maxQ4Distance);
		
	}

	// calculate the segment scores for each solution
	private void getSolutionSegments() {
		m_segWsm = initSegValues();
		m_segWpm = initSegValues();
		
		for (int solutionIndex = 0; solutionIndex < m_numSolutions; solutionIndex++) {
			int[] members = m_members[solutionIndex];
			for (int segIndex = 0; segIndex < m_numSegments; segIndex++) {
				
				int segSize = m_segSizes[solutionIndex][segIndex];
				double[][] segData1 = initDoubleArray(segSize, m_d1Size);
				double[][] segData2 = initDoubleArray(segSize, m_d2Size);
				
				// get the segment data
				int segRowIndex = 0;
				for (int memIndex = 0; memIndex < members.length; memIndex++) {
					if (members[memIndex] == segIndex) {
						segData1[segRowIndex] = m_data1[memIndex];
						segData2[segRowIndex] = m_data2[memIndex];
						segRowIndex++;
					}
				}
				
				assert segRowIndex == segSize;
				
				double d1Value = calSegWsmValue(segData1, m_data1Avg, m_data1StdDev);
				double d2Value = calSegWsmValue(segData2, m_data2Avg, m_data2StdDev);
				Point2D.Double solutionPoint = new Point2D.Double(d1Value, d2Value);
				m_segWsm[solutionIndex][segIndex] = solutionPoint;
				
				d1Value = calSegWpmValue(segData1, m_data1Avg);
				d2Value = calSegWpmValue(segData2, m_data2Avg);
				solutionPoint = new Point2D.Double(d1Value, d2Value);
				m_segWpm[solutionIndex][segIndex] = solutionPoint;
				
			}
		}
	}
	
	// weighted sum model 
	private double calSegWsmValue(double[][] segData, double[] dataAvg, double[] dataStdDev) {
		double result = 0;
		int numColumns = dataAvg.length;
		double[] dimSegAvg = calAvg(segData);
		for (int columnIndex = 0; columnIndex < numColumns; columnIndex++) {
			double columnDev = dimSegAvg[columnIndex] - dataAvg[columnIndex];
			double columnValue = columnDev / dataStdDev[columnIndex];
			result += columnValue;
		}
		result = result / numColumns; 
		
		return result;
	}
	
	private double calSegWpmValue(double[][] segData, double[] dataAvg) {
		double result = 1;
		
		double[] dimSegAvg = calAvg(segData);
		for (int columnIndex = 0; columnIndex < dimSegAvg.length; columnIndex++) {
			double columnValue = dimSegAvg[columnIndex] / dataAvg[columnIndex];
			result *= columnValue;
		}
		
		return result;
	}

	private  Point2D.Double[][] initSegValues() {
		
		Point2D.Double[][] retValue = new Point2D.Double[m_numSolutions][];
		for (int solutionIndex = 0; solutionIndex < m_numSolutions; solutionIndex++) {
			retValue[solutionIndex] = new Point2D.Double[m_numSegments];
		}
		return retValue;
	}



	private void readData() throws IOException {
		m_data1 = initDoubleArray(m_numRows, m_d1Size);
		m_data2 = initDoubleArray(m_numRows, m_d2Size);
		
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new FileReader(m_dataFilename));
			
			// skip row header
			reader.readLine();
			
			for (int row = 0; row < m_numRows; row++) {

				String line = reader.readLine();
				
				String[] fields = line.split(FIELD_DELIMITER_REGEX);

				for (int column = 0; column < m_d1Size; column++) {
					// the row and column are reversed in our data array
					double value = Double.parseDouble(fields[column]);
					m_data1[row][column] = value;
				}
				
				for (int column = m_d1Size; column < m_numColumns; column++) {
					// the row and column are reversed in our data array
					double value = Double.parseDouble(fields[column]);
					m_data2[row][column - m_d1Size] = value;
				}
			}

			sm_logger.info("Reading data file is done. ");
			

		} finally {
			if (reader != null) {
				reader.close();
			}
		}
	}
	
	private void calData() {
		 
		m_data1Avg = calAvg(m_data1);
		m_data1StdDev = calStdDev(m_data1, m_data1Avg);
		
		m_data2Avg = calAvg(m_data2);
		m_data2StdDev = calStdDev(m_data2, m_data2Avg);
	}



	private  double[] calAvg(double[][] data) {
		double[] avg = new double[data[0].length];
		int numRows = data.length;
		for (int columnIndex = 0; columnIndex < avg.length; columnIndex++){
			double columnSum = 0;
			for (int rowIndex = 0; rowIndex < numRows; rowIndex++) {
				columnSum += data[rowIndex][columnIndex];
			}
			avg[columnIndex] = columnSum / numRows;
		}
		return avg;
	}
	
	private double[] calStdDev(double[][] data, double[] avg) {
		double[] stdDev = new double[data[0].length];
		int numRows = data.length;
		
		for (int columnIndex = 0; columnIndex < avg.length; columnIndex++){
			double columnSumDev = 0;
			for (int rowIndex = 0; rowIndex < numRows; rowIndex++) {
				double dev = data[rowIndex][columnIndex] - avg[columnIndex];
				double devSquare = dev * dev;
				columnSumDev += devSquare;
			}
			stdDev[columnIndex] = Math.sqrt(columnSumDev / (numRows - 1));
		}
		return stdDev;
	}
	
	private int[][] readIntArrays(int numRows, int numColumns, String filename) throws IOException {
		int[][] intArray = initIntArray(numRows, numColumns);
		
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new FileReader(filename));
			
			
			for (int row = 0; row < numRows; row++) {

				String line = reader.readLine();
				
				String[] fields = line.split(FIELD_DELIMITER_REGEX);

				for (int column = 0; column < numColumns; column++) {
					// the row and column are reversed in our data array
					int value = Integer.parseInt(fields[column]);
					intArray[row][column] = value;
				}
			}

			sm_logger.info("Reading file is done for file: " + filename);

		} finally {
			if (reader != null) {
				reader.close();
			}
		}
		return intArray;
		
	}
	


	private double[][] initDoubleArray(int numRows, int numColumns) {
		double[][] retTable = new double[numRows][];

		for (int row = 0; row < numRows; row++) {
			retTable[row] = new double[numColumns];
		}
		return retTable;
	}
	
	private  int[][] initIntArray(int numRows, int numColumns) {
		int[][] retTable = new int[numRows][];

		for (int row = 0; row < numRows; row++) {
			retTable[row] = new int[numColumns];
		}
		return retTable;
	}
}
