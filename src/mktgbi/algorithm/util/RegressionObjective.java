package mktgbi.algorithm.util;
import mktgbi.dataio.Config;
import mktgbi.dataio.DataBin;
import java.util.logging.Logger;

import flanagan.analysis.*;


public class RegressionObjective {

	private static Logger sm_logger = Config.SM_LOGGER;
	private static boolean sm_isLinear = Config.getIsLinear();

	// the dataBin has all data whose first colume is the DV
	public static double calResponseObj(DataBin dataBin, Cluster[] clusters) {
		double retValue = 0;

		for (Cluster cluster : clusters) {
			
			double clusterObj = 0;
			
			if (sm_isLinear) {
				// for linear regression
			
				//clusterObj = calClusterObj(dataBin, cluster);
				clusterObj = LinearObjective.calClusterObj(dataBin, cluster);
				
			} else {
				// for logistic regression
				clusterObj = LogisticObjective.calClusterObj(dataBin, cluster);
			}
			retValue += clusterObj;
		}
		
		sm_logger.fine("Regression evaluation result: " + retValue);

		return retValue;
	}

	
	static double calClusterObj(DataBin dataBin, Cluster cluster) {

		double retValue = 0;
		
		Integer[] rows = cluster.get_members();
		int numColumns = dataBin.getNumColumns();
		double[][] xValues = new double[numColumns - 1][];
		
		double[] yValues = dataBin.getColumnByRows(0, rows);
		
		for (int columnIndex = 1; columnIndex < numColumns; columnIndex++) {
			double[] column = dataBin.getColumnByRows(columnIndex, rows);
			xValues[columnIndex - 1] = column;
		}
		
		Regression reg = new Regression(xValues, yValues);
		reg.linear();
		// this is the residual sum of squares
		retValue = reg.getSumOfSquares();
		
		
		return retValue;
	}



}
