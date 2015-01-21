package mktgbi.dataio;

import java.io.*;
import java.util.logging.*;

/**
 * @author yingliu
 *
 */
public class DataSource {

	private static Logger sm_logger = Config.SM_LOGGER;

	// we use whitespace [ \f\n\r\t] as field delimiter
	private static final String FIELD_DELIMITER_REGEX = "\\s";

	private static int m_numRows = 0;

	private static int m_numColumns = 0;


	private static DataBin[] m_dataBins = null;

	private static DataBin m_allData = null;


	/**
	 * @throws IOException
	 */
	public static void init() throws IOException {

		sm_logger.info("About to read data file and initilize data bins. ");
		
		m_numRows = Config.getNumRows();
		m_numColumns = Config.getNumColumns();

		m_dataBins = new DataBin[Config.getNumObjectives()];

		double[][] dataTable = readData();

		initDataBins(dataTable);
		
		sm_logger.info("Data source is initialized.");
	}

	private static void initDataBins(double[][] dataTable) {
		
		int[] dimensionSizes = Config.getDimSizes();
		DistanceType[] distanceTypes = Config.getDistanceTypes();
		
		int startColumn = 0;
		
		for (int binIndex = 0; binIndex < m_dataBins.length; binIndex++) {
			sm_logger.info("Initialize data bin: " + binIndex + " with start column: "
					+ startColumn);

			int numColumns = dimensionSizes[binIndex];
			double[][] binDataTable = initializeTable(m_numRows, numColumns);

			for (int row = 0; row < m_numRows; row++) {
				for (int column = 0; column < numColumns; column++) {
					binDataTable[row][column] = dataTable[row][startColumn + column];
				}
			}

			m_dataBins[binIndex] = new DataBin(binDataTable, distanceTypes[binIndex]);

			startColumn += dimensionSizes[binIndex];
		}

		sm_logger.info("Initialize data bin for all data.");
		m_allData = new DataBin(dataTable, DistanceType.EUCLIDEAN);
	}

	private static double[][] readData() throws IOException {

		String dataFilename = Config.getDataFilename();
		if (sm_logger.isLoggable(Level.FINE)) {
			sm_logger.fine("About to read data file: " + dataFilename
					+ " Rows: " + m_numRows + " Columns: " + m_numColumns);
		}

		double[][] dataTable = initializeTable(m_numRows, m_numColumns);

		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new FileReader(dataFilename));
			
			// skip row header
			reader.readLine();
			
			for (int row = 0; row < m_numRows; row++) {

				String line = reader.readLine();
				
				String[] fields = line.split(FIELD_DELIMITER_REGEX);

				for (int column = 0; column < m_numColumns; column++) {
					// the row and column are reversed in our data array
					double value = Double.parseDouble(fields[column]);
					dataTable[row][column] = value;
				}
			}

			sm_logger.info("Reading data file is done. ");

		} finally {
			if (reader != null) {
				reader.close();
			}
		}

		return dataTable;
	}


	/**
	 * @param dimIndex
	 * @return The DataBin instance of specified index
	 */
	public static DataBin getDataBin(int dimIndex) {
		return m_dataBins[dimIndex];
	}

	/**
	 * @return The DataBin instance of all data
	 */
	public static DataBin getAll() {

		return m_allData;
	}

	private static double[][] initializeTable(int numRows, int numColumns) {
		double[][] retTable = new double[numRows][];

		for (int row = 0; row < m_numRows; row++) {
			retTable[row] = new double[numColumns];
		}
		return retTable;
	}

}
