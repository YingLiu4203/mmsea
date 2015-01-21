package mktgbi.experiment;

import java.util.logging.Logger;

import mktgbi.algorithm.util.*;
import mktgbi.dataio.*;
import mktgbi.util.*;

import java.io.*;
import java.util.*;

/**
 * @author yingliu
 *
 */
public class KmeansRun {

	private static Logger sm_logger = Config.SM_LOGGER;
	private static final int RANDOM_SEED = 1000;
	
	private static String OBJECTIVE_FILENAME_APPENDEX = ".objectives.txt";
	private static String MEMEBER_FILENAME_APPENDEX = ".members.txt";
	
	private static final String CONFIG_FILENAME = 
			"config\\KmeansRun.Properties";
	
	private static final String NUM_RUNS_PROPNAME = "NumberOfRuns";
	private static final String OUTPUT_FILENAME_PROPNAME = "OutputFilename";
	
	private static final int DEFAULT_NUM_RUNS = 100;
	
	private int m_numRuns = 0;
	
	private String m_filename = null;
	
	int m_numDims = 0;
	int m_minNumClusters = 3;
	int m_maxNumClusters = 5;
	
	private ResultSet m_resultSet = null;
	// store results
	
	
	/**
	 * @param config
	 * @param dataSource
	 * @throws IOException 
	 * @throws ConfigurationException
	 */
	public KmeansRun() throws IOException {
		super();
			
		m_numDims = Config.getNumObjectives();
		
		readConfig();
		
		m_resultSet = new ResultSet(); 
	}
	
	private void readConfig() throws IOException {
		Properties properties = new Properties();
		properties.load(new FileInputStream(CONFIG_FILENAME));
		m_numRuns = Config.getIntProperty(properties,NUM_RUNS_PROPNAME, DEFAULT_NUM_RUNS);
		m_filename = properties.getProperty(OUTPUT_FILENAME_PROPNAME);
		
	}
	
	
	/**
	 * @param args
	 * @throws ConfigurationException
	 * @throws IOException
	 */
	public static void main(String[] args) throws IOException {
		
		sm_logger.info("KemansRun starts");
		
		// set random seed so we can repeat the test
		GaRandom.setSeed(RANDOM_SEED);
		
		Config.init();
		DataSource.init();
		
		// run other experiments
		KmeansRun kmeansRun = new KmeansRun();
		kmeansRun.run();
		kmeansRun.save();
		
		sm_logger.info("KemansRun is done.");
	}
	
	/**
	 * 
	 */
	public void run() {
		
		for (int numClusters = m_minNumClusters; 
				numClusters <= m_maxNumClusters; numClusters++ ) {
			
			Result[] results = new Result[m_numDims * 2];
			
			for (int dim = 0; dim < m_numDims; dim++) {
				results[dim] = runDimension(numClusters, dim);
			}
			
			Result[] allResults = runAll(numClusters);
			for (int ii = 0; ii < m_numDims; ii++) {
				results[ii + m_numDims] = allResults[ii];
			}
			
			m_resultSet.add(numClusters, results);
		}
		
	}
	
	private Result runDimension(int numClusters, int dim) {
		
		DataBin dataBin = DataSource.getDataBin(dim);
		Kmeans kmeans = new Kmeans(dataBin);
		
		double minValue = Double.MAX_VALUE;
		double[] minObjectives = null; 
		int[] minAssigned = null;
		for (int runIndex = 0; runIndex < m_numRuns; runIndex++) {
			int[] assigned = kmeans.run(numClusters);
			
			double[] objectives = Cluster.calObjectives(assigned);
			
			double compared = objectives[dim];
			if (minValue > compared) {
				minValue = compared;
				minObjectives = objectives;
				minAssigned = assigned;
			}
		}
		return (new Result(minObjectives, minAssigned));
	}
	
	// best result for each dimension for all attributes
	private Result[] runAll(int numClusters) {
		
		DataBin dataBin = DataSource.getAll();
		Kmeans kmeans = new Kmeans(dataBin);
		
		// we save a best for each dimension
		double[] minValues = new double[m_numDims];
		for (int ii = 0; ii < m_numDims; ii++) {
			minValues[ii] = Double.MAX_VALUE;
		}
		
		// save the minimum objective result
		double[][] minObjectives = new double[m_numDims][];
		int[][] minAssigned = new int[m_numDims][];
		
		for (int runIndex = 0; runIndex < m_numRuns; runIndex++) {
			int[] assigned = kmeans.run(numClusters);
			double[] objectives = Cluster.calObjectives( assigned);
			
			for (int ii = 0; ii < m_numDims; ii++) {
				if (minValues[ii] > objectives[ii]) {
					minValues[ii] = objectives[ii];
					minObjectives[ii] = objectives;
					minAssigned[ii] = assigned;
				}
			}
		}
		
		Result[] results = new Result[m_numDims];
		for (int ii = 0; ii < m_numDims; ii++) {
			results[ii] = new Result(minObjectives[ii], minAssigned[ii]);
		}
		return results;
	}
	
	/**
	 * @throws IOException
	 */
	public void save() throws IOException {
		
		String delimiter = Config.OUTPUT_DELIMITER;
		
		BufferedWriter objectiveWriter = null;
		BufferedWriter memberWriter = null;
		try {
			objectiveWriter = new BufferedWriter(
					new FileWriter(m_filename + OBJECTIVE_FILENAME_APPENDEX));
			memberWriter = new BufferedWriter(
					new FileWriter(m_filename + MEMEBER_FILENAME_APPENDEX));
			
			for (int numClusters = m_minNumClusters; 
					numClusters <= m_maxNumClusters; numClusters++ ) {
				
				Result[] results = m_resultSet.get(numClusters);
				for (Result result: results) {
					StringBuilder builder = new StringBuilder();
					builder.append(numClusters);
					builder.append(delimiter);
					double[] objectives = result.getObjectives();
					builder.append(GaConverter.fromDoubles(objectives, 
							delimiter));
					
					objectiveWriter.write(builder.toString());
					objectiveWriter.newLine();
					
					// writer members
					int[] assigned = result.getAssigned();
					String members = GaConverter.intsToString(assigned);
					memberWriter.write(members);
					memberWriter.newLine();
					
				}
			}
			
		} 
		finally {
			if (objectiveWriter != null) {
				objectiveWriter.close();
			}
			
			if (memberWriter != null) {
				memberWriter.close();
			}
		}
	}
	
	
	private class Result {
		
		double[]  m_objectives = null;
		int[] m_assigned = null;
		
		Result(double[] objectives, int[] assigned) {
			m_objectives = objectives;
			m_assigned = assigned;
		}
		
		double[] getObjectives() {
			return m_objectives;
		}
		
		int[] getAssigned() {
			return m_assigned;
		}
	}
	
	private class ResultSet {
		HashMap<Integer, Result[]> m_results = null;
		
		ResultSet() {
			super();
			int size = m_maxNumClusters - m_minNumClusters + 1;
			m_results = new HashMap<Integer, Result[]>(size);
		}
		
		void add(int numClusters, Result[] results) {
			m_results.put(numClusters, results);
		}
		
		Result[] get(int numClusters) {
			return m_results.get(numClusters);
		}
	}
	
	

}
