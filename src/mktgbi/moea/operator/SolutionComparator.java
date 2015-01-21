package mktgbi.moea.operator;

import java.util.Comparator;

import mktgbi.moea.Solution;

public class SolutionComparator implements Comparator<Solution> {
	   
  public int compare(Solution s1, Solution s2) {
	  Double objOne = s1.getObjective(0);
	  Double objTwo = s2.getObjective(0);
	  
	  return objOne.compareTo(objTwo);
  }
		        
} 
	
