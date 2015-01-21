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
 * 4)Include jri.jar in your CLASSPATH
 * 
 */

public class LogisticObjective {


	private static Logger sm_logger = Config.SM_LOGGER;

	// used to initialize binary (int) column from double value
	private static final double BINARY_CUTOFF_VALUE = 0.5;

	private static String R_EXPRESSION_SET_SEED1 = "set.seed(";

	private static String R_EXPRESSION_SET_SEED2 = ")";

	private static String R_NAME_PREFIX = "Variable";

	private static String R_DATASET_NAME = "RegressionData";

	private static String R_DATA_FRAME1 = R_DATASET_NAME
			+ " = data.frame(cbind(";

	private static String R_DATA_FRAME2 = ", ";

	private static String R_DOUBLE_BRACKET = "))";

	private static String R_FORMULA_SIGN = " ~ ";

	private static char R_EXPRESSION_PLUS = '+';

	// example: deviance( glm(churn ~ Zrecchrge + Zmonths,
	// data = cellData, family = binomial (link=logit))
	private static String R_LOGIT_EXPRESSION_PART1 = "deviance(glm(";

	private static String R_LOGIT_EXPRESSION_PART2 = ", data = "
			+ R_DATASET_NAME + ", family = binomial " + " (link=logit), "
			+ "control=glm.control(maxit=5000)))"; // set high max iteration

	// example: deviance( lm(Zmargin ~ Zrecchrge + Zmonths,
	// data = cellData, family = binomial (link=logit))


	private static String R_EXPRESSION_RM = "rm(list=ls())";

	// use R -- help to find out options
	private static String ENGINE_ARGS[] = { "--vanilla", "--slave" };

	// used to improve performance of constructing R experession
	// only construct expressions when necessary (databin changed)
	private static DataBin sm_cached_DataBin = null;
	private static String[] sm_cached_Names = null;
	private static String sm_cached_DF_String = null;
	private static String sm_cached_Eval_String = null;

	private static Rengine sm_rEngine = null;
	public static boolean sm_Init_Flag = false;

	public static void initR(int randomSeed) {
		sm_logger.info("About to initialize R Engine");
		
		if (!Rengine.versionCheck()) {
			throw (new IllegalConfigException("Rengine Version is wrong"));
		}

		// 1) we pass the arguments from the command line
		// 2) we won't use the main loop -- flase parameter
		// 3) the callbacks are implemented by the TextConsole class below
		// m_rEngine = new Rengine(ENGINE_ARGS, false, new TextConsole());
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
		sm_rEngine.end();
		sm_logger.info("R Engine is closed");
	}


	static double calClusterObj(DataBin dataBin, Cluster cluster) {

		if (!sm_Init_Flag) {
			initR(1997);
			sm_Init_Flag = true;
		}
		
		if (dataBin != sm_cached_DataBin) {
			sm_cached_DataBin = dataBin;
			buildNames(dataBin.getNumColumns());
			buildDFString();
			
			buildLogitEvalString();
		}
		
		double retValue = 0;

		Integer[] rows = cluster.get_members();
		int numColumns = dataBin.getNumColumns();

		double[] firstColumn = dataBin.getColumnByRows(0, rows);
		
		
		int[] response = initBinaryColumns(firstColumn);
		sm_rEngine.assign(sm_cached_Names[0], response);
		

		for (int columnIndex = 1; columnIndex < numColumns; columnIndex++) {
			double[] column = dataBin.getColumnByRows(columnIndex, rows);
			sm_rEngine.assign(sm_cached_Names[columnIndex], column);
		}

		sm_rEngine.eval(sm_cached_DF_String);

		REXP rResult = sm_rEngine.eval(sm_cached_Eval_String);
		retValue = rResult.asDouble();

		// remove all dataset from R Enivornment
		sm_rEngine.eval(R_EXPRESSION_RM);

		return retValue;
	}

	private static void buildNames(int numColumns) {
		sm_cached_Names = new String[numColumns];
		for (int columnIndex = 0; columnIndex < numColumns; columnIndex++) {
			sm_cached_Names[columnIndex] = R_NAME_PREFIX + columnIndex;
		}

	}

	private static int[] initBinaryColumns(double[] column) {
		int numRows = column.length;
		int[] retValue = new int[numRows];

		// type cast results in truncation of decimal numbers
		for (int ii = 0; ii < numRows; ii++) {
			retValue[ii] = (int) (column[ii] + BINARY_CUTOFF_VALUE);
		}

		return retValue;
	}

	private static void buildDFString() {

		String evalNameStr = sm_cached_Names[0];
		for (int ii = 1; ii < sm_cached_Names.length; ii++) {
			evalNameStr += R_DATA_FRAME2 + sm_cached_Names[ii];
		}
		sm_cached_DF_String = R_DATA_FRAME1 + evalNameStr + R_DOUBLE_BRACKET;

	}

	private static void buildLogitEvalString() {

		String evalStr = R_LOGIT_EXPRESSION_PART1 + sm_cached_Names[0]
				+ R_FORMULA_SIGN;

		String evalNameStr = sm_cached_Names[1];
		for (int ii = 2; ii < sm_cached_Names.length; ii++) {
			evalNameStr += R_EXPRESSION_PLUS + sm_cached_Names[ii];
		}
		evalStr += evalNameStr + R_LOGIT_EXPRESSION_PART2;
		sm_cached_Eval_String = evalStr;
	}


}