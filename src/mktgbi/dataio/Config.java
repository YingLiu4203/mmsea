package mktgbi.dataio;

import java.util.logging.Logger;
import java.util.*;
import java.io.*;

import mktgbi.util.*;

/**
 * The Config class initializes/read alogrithm settings.
 * @author yingliu
 */
public class Config {

	public static final Logger SM_LOGGER = Logger.getLogger("Mmsea");
	
	public static final String LOG_FILENAME = "output\\Mmsea.xml";
	public static final String OBJECTIVES_FILENAME = "output\\Objectives";
	public static final String MEMBERS_FILENAME = "output\\Members";
	public static final String VARIABLES_FILENAME = "output\\Variables";
	public static final String SEGMENT_SIZES_FILENAME = "output\\SegmentSizes";
	public static final String FILENAME_APPENDIX = ".txt";
	
	/**
	 * Output file field delimiter
	 */
	public static final String OUTPUT_DELIMITER = "\t";
	public static final char VARIABLE_DELIMITER = '\t';
	public static final String PROPERTY_DELIMITER = ",\\s*";
	
	private static final int INVALID_SIZE = Integer.MIN_VALUE;
	
	private static final String PROBLEM_CLASS_PROPNAME = "ProblemClass";
	private static String m_problemClass = null;
	private static final String DEFAULT_PROBLEM_CLASS = "JointSeg";
	
	private static final String INIT_SOLUTION_SIZE_PROPNAME = "InitSolutionSize";
	private static int m_initSolutionSize = INVALID_SIZE;
	private static final int DEFAULT_INIT_SOLUTION_SIZE = 9;
	
	private static final String POPULATION_SIZE_PROPNAME = "PopulationSize";
	private static int m_populationSize = INVALID_SIZE;
	private static final int DEFAULT_POPULATION_SIZE = 10;
	
	private static final String ARCHIVE_SIZE_PROPNAME = "ArchiveSize";
	private static int m_archiveSize = INVALID_SIZE;
	private static final int DEFAULT_ARCHIVE_SIZE = 100;
	
	// The number of evaluations
	private static final String MAX_EVALUATIONS_PROPNAME = "MaxEvaluations";
	private static int m_maxEvaluations = INVALID_SIZE;
	private static final int DEFAULT_MAX_EVALUATIONS = 100;
	
	private static final String CROSSOVER_PROBABILITY_PROPNAME = "CrossoverProbability";
	private static double m_crossoverProbability = INVALID_SIZE;
	private static final double DEFAULT_CROSSOVER_PROBABILITY = 0.1;
	
	private static final String MIN_OBJECTIVE_DISTANCE_PROPNAME = "MinObjectiveDistance";
	private static double m_minObjectiveDistance = INVALID_SIZE;
	private static final double DEFAULT_MIN_OBJECTIVE_DISTANCE = 0.0001;
	
	
	private static final String CONFIG_FILENAME = "config\\GaMain.Properties";
	
	// The parameters from configuration file
	private static final String DATA_FILENAME_PROPNAME = "DataFilename";
	private static String m_dataFilename = null;

	private static final String IS_LINEAR_PROPNAME = "IsLinear";
	private static boolean m_isLinear = true;
	
	private static final String RANDOM_SEED_PROPNAME = "RandomSeed";
	private static int m_randomSeed = INVALID_SIZE;
	private static final int DEFAULT_RANDOM_SEED = 1997;
	
	// set this number will simplify code
	private static final String NUM_ROWS_PROPNAME = "NumberOfRows";
	private static int m_numRows = INVALID_SIZE;
	private static final int MIN_NUM_ROWS = 100;
	
	private static final String MIN_CLUSTER_SIZE = "MinClusterSize";
	private static int m_minClusterSize = INVALID_SIZE;
	private static final int DEFAULT_MIN_CLUSTER_SIZE = 10; 
	
	
	// size of each dimension and the total number of columns 
	private static final String DIMENSION_SIZES_PROPNAME = "DimensionSizes";
	private static int[] m_dimensionSizes = null;
	private static int m_numColumns = INVALID_SIZE;
	// The number of objectives
	private static int m_numOfObjectives = INVALID_SIZE;
	
	
	//we use this to distinguish regression and joint segmentation. 
	//If the first parameter is NONE, this is a regression. Otherwise, it’s a joint segmentation. 
	private static final String DISTANCE_TYPES_PROPNAME = "DistanceTypes";
	private static DistanceType[] m_distanceTypes = null;
	
	//number of iterations in for clusterwise objective calculation
	private static final String MAX_ITERATION_NUMBER_PROPNAME = "MaxIterationNumber";
	private static int m_maxIterationNumber = INVALID_SIZE;
	private static final int DEFAULT_MAX_ITERATION_NUMBER = 100;
	
	private static final String MIN_NUM_SEGMENTS_PROPNAME = "MinNumberOfSegments";
	private static int m_minNumSegments = INVALID_SIZE;
	private static final int DEFUALT_MIN_NUM_SEGMENTS = 3;
	
	private static final String MAX_NUM_SEGMENTS_PROPNAME = "MaxNumberOfSegments";
	private static int m_maxNumSegments = INVALID_SIZE;
	private static final int DEFUALT_MAX_NUM_SEGMENTS = 8;
	
	
	/**
	 * set to private so nobody can create an object of this class
	 */
	private Config() {
		super();
	}

	/**
	 * @throws ConfigurationException 
	 * 
	 */
	public static void init() throws IOException {
		
		SM_LOGGER.fine("About to read configuration file: " + CONFIG_FILENAME);
		
		Properties properties = new Properties();
		
		properties.load(new FileInputStream(CONFIG_FILENAME));
				
		readFileConfig(properties);
	
		readEvolutionConfig(properties);
		
		readSegmenationConfig(properties);
		
		SM_LOGGER.info("Reading configuration file is done. ");
	}
	
	
	private static void readFileConfig(Properties properties) {
		
		m_dataFilename =properties.getProperty(DATA_FILENAME_PROPNAME);
		SM_LOGGER.info("Data filename: " + m_dataFilename); 
		
		
		m_randomSeed = getIntProperty(properties, RANDOM_SEED_PROPNAME, DEFAULT_RANDOM_SEED);
		SM_LOGGER.info("Random seed is: " + m_randomSeed); 
		
		m_numRows = getIntProperty(properties, NUM_ROWS_PROPNAME);			
		if (m_numRows < MIN_NUM_ROWS) {
			reportIllegalConfig("nubmer of rows is too small: " 
					+ m_numRows + ". Minimum is " + MIN_NUM_ROWS);
		}
		SM_LOGGER.info("Number of rows: "+ m_numRows); 
		
		String dimensionProperty = properties.getProperty(DIMENSION_SIZES_PROPNAME);
		if (dimensionProperty == null) {
			reportIllegalConfig("Dimension sizes msut be specified in property file");
		} else {
			String[] dimensionSizes = dimensionProperty.split(PROPERTY_DELIMITER);
			m_numOfObjectives = dimensionSizes.length;
			
			if (m_numOfObjectives < 1) {
				reportIllegalConfig("the number of objectives is wrong: " + m_numOfObjectives);
			} else {
				SM_LOGGER.info("Number of objectives: "+ m_numOfObjectives);
			}
			
			m_dimensionSizes = new int[m_numOfObjectives];
			m_numColumns = 0;
			for (int ii = 0; ii < m_numOfObjectives; ii++) {
				m_dimensionSizes[ii] = Integer.parseInt(dimensionSizes[ii]);
				SM_LOGGER.info("Dimension " + ii + " column size: " + m_dimensionSizes[ii]);
				if (m_dimensionSizes[ii] < 1) {
					reportIllegalConfig("Column in dimension " + ii
							+ " is too small: "	+ m_dimensionSizes[ii]);
				}
				m_numColumns += m_dimensionSizes[ii];
			} 
		}
		
		SM_LOGGER.info("Number of columns is " +  m_numColumns);
	}
	
	private static void readEvolutionConfig(Properties properties) {
		
		m_problemClass = properties.getProperty(PROBLEM_CLASS_PROPNAME, DEFAULT_PROBLEM_CLASS);
		
		String isLinearStr = properties.getProperty(IS_LINEAR_PROPNAME);
		if (isLinearStr != null) {
			m_isLinear = Boolean.parseBoolean(isLinearStr);
		}
		
		m_initSolutionSize = getIntProperty(properties, INIT_SOLUTION_SIZE_PROPNAME, 
				DEFAULT_INIT_SOLUTION_SIZE);
		SM_LOGGER.info("Initial solution size is " + m_initSolutionSize);
		
		m_populationSize = getIntProperty(properties, POPULATION_SIZE_PROPNAME,
				DEFAULT_POPULATION_SIZE);
		SM_LOGGER.info("Population size is: " + m_populationSize);
		
		m_archiveSize = getIntProperty(properties, ARCHIVE_SIZE_PROPNAME,
				DEFAULT_ARCHIVE_SIZE);
		SM_LOGGER.info("Archive size is: " + m_archiveSize);
		
		m_maxEvaluations = getIntProperty(properties, MAX_EVALUATIONS_PROPNAME, 
				DEFAULT_MAX_EVALUATIONS);
		SM_LOGGER.info("Number of Generations: " + m_maxEvaluations);
		
		m_crossoverProbability = getDoubleProperty(properties, CROSSOVER_PROBABILITY_PROPNAME, 
				DEFAULT_CROSSOVER_PROBABILITY);
		SM_LOGGER.info("Crossover probability : " + m_crossoverProbability);
		
		
		m_minObjectiveDistance = getDoubleProperty(properties, MIN_OBJECTIVE_DISTANCE_PROPNAME, 
				DEFAULT_MIN_OBJECTIVE_DISTANCE);
		SM_LOGGER.info("Minimum objective distance is : " + m_minObjectiveDistance);
		
		
		m_minClusterSize = getIntProperty(properties, MIN_CLUSTER_SIZE, DEFAULT_MIN_CLUSTER_SIZE);	
		SM_LOGGER.info("Minimum cluster size: " + m_minClusterSize);
		
		
		m_minNumSegments = getIntProperty(properties, MIN_NUM_SEGMENTS_PROPNAME, 
				DEFUALT_MIN_NUM_SEGMENTS);
		SM_LOGGER.info("Minumum number of clusters: " + m_minNumSegments);
		
		m_maxNumSegments = getIntProperty(properties, MAX_NUM_SEGMENTS_PROPNAME, 
				DEFUALT_MAX_NUM_SEGMENTS);
		SM_LOGGER.info("Maximum number of clusters: " + m_maxNumSegments);
	
	}
	
	
	private static void readSegmenationConfig(Properties properties) {
		
		m_maxIterationNumber = getIntProperty(properties, MAX_ITERATION_NUMBER_PROPNAME, 
				DEFAULT_MAX_ITERATION_NUMBER);
		SM_LOGGER.info("Max number of clusterwise iteration: " + m_maxIterationNumber);
			
		String distanceProperty = properties.getProperty(DISTANCE_TYPES_PROPNAME);
		if (distanceProperty == null) {
			reportIllegalConfig("Distance types must be specified in property file");
		} else {
			String[] distanceTypes = distanceProperty.split(PROPERTY_DELIMITER);
			int columnCount = distanceTypes.length; 
			if (columnCount != m_numOfObjectives) {
				reportIllegalConfig("the number of distance types is wrong: " + columnCount);
			}
			
			m_distanceTypes = new DistanceType[columnCount];
			for (int ii = 0; ii < distanceTypes.length; ii++) {
				m_distanceTypes[ii] = Enum.valueOf(DistanceType.class, distanceTypes[ii]);
				SM_LOGGER.info("Dimension " + ii + " distance type: " + distanceTypes[ii]);
			}
		}
	}
	
	public static int getIntProperty(Properties properties, String propertyName) {
		int retVal = INVALID_SIZE;
		
		String value = properties.getProperty(propertyName);
		
		if (value == null) {
			reportIllegalConfig("Property " + propertyName + " must be specified.");
		} else {
			retVal = Integer.parseInt(value);
		}
		return retVal;
	}
	
	public static int getIntProperty(Properties properties, String propertyName, int defaultValue) {
		int retVal = defaultValue;
		String value = properties.getProperty(propertyName);
		if (value != null) {
			retVal = Integer.parseInt(value);
		}
		return retVal;
	}
	
	public static double getDoubleProperty(Properties properties, String propertyName, 
			double defaultValue) {
		double retVal = defaultValue;
		
		String value = properties.getProperty(propertyName);
		if (value != null) {
			retVal = Double.parseDouble(value); 
		}
		return retVal;
	}


	public static String getDataFilename() {
		
		return m_dataFilename;
	}
	
	public static boolean getIsLinear() {
		return m_isLinear;
	}

	public static int getRandomSeed() {
		return m_randomSeed;
	}
	
	
	public static int getNumRows() {
		
		return m_numRows;
	}
	
	
	public static int getNumColumns() {
		
		return m_numColumns;
	}
	
	
	public static int[] getDimSizes() {
		
		return m_dimensionSizes;
	}
	
	
	public static DistanceType[] getDistanceTypes() {
		
		return m_distanceTypes;
	}
	

	public static int getMinNumSegments() {
			
		return m_minNumSegments;
	}

	public static int getMaxNumSegments() {
		
		return m_maxNumSegments;
	}

	/**
	 * Get the minimum cluster size.
	 * @return The minimum cluster size 
	 */
	public static int getMinClusterSize() {
		
		return m_minClusterSize;
	}
	
	/**
	 * Get the number of objectives.
	 * @return The number of objectives 
	 */
	public static int getNumObjectives() {
		return m_numOfObjectives;
	}

	public static int getMaxIterationNumber() {
		return m_maxIterationNumber;
	}
	
	static private int reportIllegalConfig(String errorMsg) {
		SM_LOGGER.severe(errorMsg);
		throw (new IllegalConfigException(errorMsg));
	}

	public static String getProblemClass() {
		return m_problemClass;
	}
	
	public static int getInitSolutionSize() {
		return m_initSolutionSize;
	}

	public static int getPopulationSize() {
		return m_populationSize;
	}
	
	public static int getArchiveSize() {
		return m_archiveSize;
	}
	
	public static int getNumEvaluations() {
		return m_maxEvaluations;
	}

	public static double getCrossoverProbability() {
		return m_crossoverProbability;
	}
	
	public static double getMinObjectiveDistance() {
		return m_minObjectiveDistance;
	}
	
}

