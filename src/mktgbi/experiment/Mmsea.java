/**
 * Mmsea.java
 * @author Ying Liu
 * Creation Date: 09-07-2009
 * Last Modified: 09-08-2009
 * 
 */
package mktgbi.experiment;

import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.Logger;

import mktgbi.algorithm.util.LinearObjective;
import mktgbi.algorithm.util.RunMode;
import mktgbi.dataio.Config;
import mktgbi.dataio.DataSource;
import mktgbi.moea.Evolution;
import mktgbi.moea.problem.Problem;
import mktgbi.util.GaRandom;

public class Mmsea {
	private static Logger sm_logger = Config.SM_LOGGER; 
	private static FileHandler sm_fileHandler = null;
	

	/**
	 * @param args
	 *            Command line arguments.
	 * @throws JMException
	 * @throws IOException
	 * @throws SecurityException
	 *             Usage: three options - jmetal.experiments.Main algorithmName
	 *             - jmetal.experiments.Main algorithmName problemName -
	 *             jmetal.experiments.Main algorithmName problemName
	 *             paretoFrontFile
	 * @throws IllegalAccessException 
	 * @throws InstantiationException 
	 * @throws ClassNotFoundException 
	 */
	public static void main(String[] args) throws Exception {
		
		sm_fileHandler = new FileHandler(Config.LOG_FILENAME);
		sm_logger.addHandler(sm_fileHandler);
		sm_logger.info("Program starts.");
		
		Mmsea gaMain = new Mmsea();
		RunMode mode = null;
		if (args.length == 0) {
			mode = RunMode.DEFAULT;
		} else {
			mode = Enum.valueOf(RunMode.class, args[0].toUpperCase());
		}
		
		gaMain.run(mode);
		
		LinearObjective.endR();
		
		sm_logger.info("Program ran successfully.");
	
	}
	
	
	@SuppressWarnings("unchecked")
	private void run(RunMode mode) throws IOException, ClassNotFoundException, 
			InstantiationException, IllegalAccessException {
		
		sm_logger.info("Start GaMain run() method with run mode: " + mode);
		
		Config.init();
		int seed = Config.getRandomSeed();
		
		GaRandom.setSeed(seed);
		sm_logger.info("Set random seed to: " + seed);
		
		DataSource.init();
		
		String problemClassName = Config.getProblemClass();
		Class<Problem> problemClass = (Class<Problem>)Class.forName(problemClassName);
		
		Problem problem = problemClass.newInstance();
		Evolution algorithm = new Evolution(problem, mode);
		
		// Execute the Algorithm
		long initTime = System.currentTimeMillis();
		algorithm.execute();
		long estimatedTime = (System.currentTimeMillis() - initTime) / 1000;

		// Result messages
		sm_logger.info("Total execution time: " + estimatedTime + " seconds.");
		
		
	} // run
	
	
} // Mmsea
