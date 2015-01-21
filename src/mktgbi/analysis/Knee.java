/**
 * 
 */
package mktgbi.analysis;

import java.awt.geom.Line2D;
import java.io.*;
import java.util.ArrayList;
import java.util.logging.Logger;

import mktgbi.dataio.*;


/**
 * @author yingliu
 *
 */
public class Knee {	
	
	private static Logger sm_logger = Config.SM_LOGGER;
	
	private ArrayList<double[]> m_results = null;
	
	private String m_filename = "output\\Objectives_4_6000000.txt";
	
	
	public static void main(String[] args) throws IOException {
		
		Knee knee = new Knee();
		knee.run();
	}

	/**
	 * @param filename 
	 * @param results 
	 * @throws IOException 
	 * 
	 */
	public Knee() throws IOException {
		super();
		Config.init();
	}
	
		
	/**
	 * @throws IOException 
	 * 
	 */
	public void run() throws IOException {
		
		m_results = readResults();
		sm_logger.info("file read is done. ");
		
		int groupSize = m_results.size();
		double[] distances = new double[groupSize];
		double[] firstPoint = m_results.get(0);
		double[] lastPoint = m_results.get(groupSize - 1);
		
		double maxDistance = 0;
		int maxSolutionIndex = 0;
		Line2D.Double line = new Line2D.Double(firstPoint[0], firstPoint[1],
				lastPoint[0], lastPoint[1]);
		for (int ii = 0; ii < groupSize; ii++) {
			double[] point = m_results.get(ii);
			
			// positive means below the line
			int direction = line.relativeCCW(point[0], point[1]);
			distances[ii] = direction * line.ptLineDist(point[0], point[1]);
			if (distances[ii] >= maxDistance) {
				maxDistance = distances[ii];
				maxSolutionIndex = ii;
			}
		}
		
		sm_logger.info("The maximum distance is: " + maxDistance + 
				"   solution id:" + maxSolutionIndex);		
	}
	
	
	
	/**
	 * @return solutions results
	 * @throws IOException 
	 * @throws FileNotFoundException 
	 */
	public ArrayList<double[]> readResults() 
			throws IOException {
		
		ArrayList<double[]> results = new ArrayList<double[]> ();
		
		BufferedReader reader = null; 
		
		try {
			
			reader = new BufferedReader(new FileReader(m_filename));
			String line;
			while ( (line = reader.readLine()) != null) {
				String[] values = line.split(Config.OUTPUT_DELIMITER);
				
				int objectiveSize = values.length;
				double[] objectives = new double[objectiveSize];
				for (int ii = 0; ii < objectiveSize; ii++) {
					objectives[ii] = Double.parseDouble(values[ii]);
				}
				
				results.add(objectives);				
			}
		} 
		finally {
			if (reader != null) {
				reader.close();
			}
		}
		return results;
	}

}
