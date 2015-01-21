package mktgbi.algorithm.util;

import mktgbi.dataio.Config;
import mktgbi.dataio.DataBin;
import java.util.logging.Logger;

import mktgbi.util.IllegalConfigException;

import org.rosuda.JRI.*;


/**
 * The configuration to call R from Java
 * 
 * 1) In R, run install.packages("rJava")
 * 
 * 2) Set Environment variable R_HOME to the R installation root
 * 
 * 3) The directory containing R.dll (in %R_HOME%\bin) and JRI.dll (in
 * %R_HOME%\library\rJava\jri) must be in your PATH. This is to fulfill the
 * requriment of java.library.path used in calling native code.
 * 
 * 4)Include jri.jar in your CLASSPATH -- Eclipse doesn't use CLASSPATH of the OS. 
 * 
 */

public class LinearObjective {


	private static Logger sm_logger = Config.SM_LOGGER;


	private static String R_EXPRESSION_SET_SEED1 = "set.seed(";
	private static String R_EXPRESSION_SET_SEED2 = ")";

	private static String R_NAME_PREFIX = "Variable";
	private static String R_DATASET_NAME = "RegressionData";
	private static String R_DATA_FRAME1 = R_DATASET_NAME  + " = data.frame(cbind(" 
		+ R_NAME_PREFIX + "0";
	
	private static String R_DATA_FRAME2 = ", ";
	private static String R_DOUBLE_BRACKET = "))";

	// example: deviance(lm(LnMargin ~ ., data=tData))
	private static String R_LM_PART1 = "deviance(lm(" + R_NAME_PREFIX + "0";
	private static String R_LM_PART2 = " ~ ., data = " + R_DATASET_NAME + "))"; 
	private static String R_LM_Eval_String = R_LM_PART1 + R_LM_PART2;
	


	private static String R_EXPRESSION_RM = "rm(list=ls())";

	// use R -- help to find out options
	private static String ENGINE_ARGS[] = { "--vanilla", "--slave" };


	// example: RegressionData = data.frame(cbind(Variable0, Variable1, Variable2))
	private static String[] sm_cached_Names = null;
	private static String sm_cached_DF_String = null;
	

	private static Rengine sm_rEngine = null;
	public static boolean sm_Init_Flag = false;

	public static void initR(int randomSeed) {
		sm_logger.info("About to initialize R Engine");
		
		if (!Rengine.versionCheck()) {
			throw (new IllegalConfigException("Rengine Version is wrong"));
		}

		// 1) we pass the arguments from the command line
		// 2) we won't use the main loop -- flase parameter
		// 3) the callbacks console is null
		sm_rEngine = new Rengine(ENGINE_ARGS, false, null);

		if (!sm_rEngine.waitForR()) {
			throw (new IllegalConfigException("Cannot load R engine"));
		}

		sm_rEngine.eval(R_EXPRESSION_SET_SEED1 + randomSeed
				+ R_EXPRESSION_SET_SEED2);
		
		sm_logger.info("R Engine was initialized with seed: " + randomSeed);
	}

  
	// set to public to suppress warning
	public static void endR() {
		if (sm_Init_Flag) {
			sm_rEngine.end();
			sm_logger.info("R Engine is closed");
		}
	}


	static double calClusterObj(DataBin dataBin, Cluster cluster) {

		if (!sm_Init_Flag) {
			initR(1997);
			buildDataFrameNames(dataBin.getNumColumns());
			sm_Init_Flag = true;
		}			
		
		// create data frame
		Integer[] rows = cluster.get_members();
		int numColumns = dataBin.getNumColumns();
		for (int columnIndex = 0; columnIndex < numColumns; columnIndex++) {
			double[] column = dataBin.getColumnByRows(columnIndex, rows);
			sm_rEngine.assign(sm_cached_Names[columnIndex], column);
		}
		sm_rEngine.eval(sm_cached_DF_String);

		REXP rResult = sm_rEngine.eval(R_LM_Eval_String);
		double retValue = rResult.asDouble();

		// remove all dataset from R Enivornment
		sm_rEngine.eval(R_EXPRESSION_RM);

		return retValue;
	}

	private static void buildDataFrameNames(int numColumns) {
		
		sm_cached_Names = new String[numColumns];
		sm_cached_Names[0] = R_NAME_PREFIX + "0";
		String evalNameStr = "";
		for (int columnIndex = 1; columnIndex < numColumns; columnIndex++) {
			sm_cached_Names[columnIndex] = R_NAME_PREFIX + columnIndex;
			evalNameStr += R_DATA_FRAME2 + sm_cached_Names[columnIndex];
		}
		sm_cached_DF_String = R_DATA_FRAME1 + evalNameStr + R_DOUBLE_BRACKET;
	}


}