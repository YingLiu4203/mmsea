package mktgbi.analysis;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.Logger;

import mktgbi.algorithm.util.Cluster;
import mktgbi.algorithm.util.LinearObjective;
import mktgbi.dataio.Config;
import mktgbi.dataio.DataSource;
import mktgbi.util.GaFile;


/**
 * 
 * @author Ying Liu
 * this class is created to calculate regression objectives for joint segmentation results. 
 * Join segmentation is more efficient than predictive segmentation. 
 *
 */
public class RegObjs {
	private static final Logger sm_logger = Logger.getLogger("RegObjs");
	private static String LOG_FILENAME = "output\\RegObjs";
	
	private static final String FIELD_DELIMITER_REGEX = "\\s";
	
	private static String m_memberFilename = "output\\Members_5_2000001.txt";
	private static String m_trssFilename = "output\\TrssWsos_5.txt";
	
	private int[][] m_members = null;
	private int m_numSolutions = -1;
	private double[][] m_results = null;
	
	
	/**
	 * @param args
	 * @throws IOException 
	 * @throws SecurityException 
	 */
	public static void main(String[] args) throws SecurityException, IOException {
		
		FileHandler sm_fileHandler = new FileHandler(LOG_FILENAME);
		sm_logger.addHandler(sm_fileHandler);
			
		RegObjs regObjs = new RegObjs();
		regObjs.run();
	}

	private void run() throws SecurityException, IOException {
		
		sm_logger.info("Analysis started.");
		
		Config.init();
		
		DataSource.init();
		
		
		m_numSolutions = Config.getArchiveSize();
		m_results = new double[m_numSolutions][];
		
		// read solution members
		m_members = readIntArrays(m_numSolutions, m_memberFilename);
		
		
		calObjectives();
		
		saveResults();
		
		LinearObjective.endR();
		sm_logger.info("Analysis finished.");
		
	}

	
	// calculate the regression objective for each solution
	private void calObjectives() {
		
		for (int solutionIndex = 0; solutionIndex < m_numSolutions; solutionIndex++) {
			double[] objectives = Cluster.calObjectives2(m_members[solutionIndex], true);
			
			m_results[solutionIndex] = objectives;
		}
		
	}
	
	
	private int[][] readIntArrays(int numRows, String filename) throws IOException {
		
		int numColumns = Config.getNumRows();
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
	
	
	private  int[][] initIntArray(int numRows, int numColumns) {
		int[][] retTable = new int[numRows][];

		for (int row = 0; row < numRows; row++) {
			retTable[row] = new int[numColumns];
		}
		return retTable;
	}
	
	private void saveResults() {
		
		try {
			BufferedWriter bw = GaFile.getFileWriter(m_trssFilename);

			for (int i = 0; i < m_results.length; i++) {
				for (int j = 0; j < m_results[0].length; j++) {
					bw.write(Double.toString(m_results[i][j]));
					bw.write("\t");
				}
				bw.newLine();
			}

			/* Close the file */
			bw.close();
		} catch (IOException e) {
			GaFile.reportError(m_trssFilename, e);
		}
	}
}
