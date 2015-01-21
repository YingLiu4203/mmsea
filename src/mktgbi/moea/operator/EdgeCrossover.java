package mktgbi.moea.operator;

import mktgbi.dataio.Config;

import mktgbi.moea.EdgeVariables;
import mktgbi.moea.Solution;
import mktgbi.util.GaRandom;
public class EdgeCrossover implements ICrossover {
	
	private double m_probability = 0;  
	
	public EdgeCrossover() {
		m_probability = Config.getCrossoverProbability();
	}

	public Solution[] execute(Solution[] parents) {
		
		int numOfParents = parents.length;
		Solution[] offSprings = new Solution[numOfParents];
		int numOfVars = parents[0].getDecisionVariables().size();
		
		int[][] childVariables = new int[numOfParents][];
		for (int childIndex = 0; childIndex < numOfParents; childIndex++) {
			childVariables[childIndex] = new int[numOfVars];
			childVariables[childIndex] = parents[childIndex].getDecisionVariables()
					.getEdges();
		}
		
		for (int varIndex = 0; varIndex < numOfVars; varIndex++) {
			if (GaRandom.nextDouble() < m_probability) {
				int temp = childVariables[0][varIndex];
				childVariables[0][varIndex] = childVariables[1][varIndex];
				childVariables[1][varIndex] = temp;
			}
		}
		
		for (int childIndex = 0; childIndex < numOfParents; childIndex++) {
			int[] edges = childVariables[childIndex];
			EdgeVariables dv = EdgeVariables.createByEdges(edges);
			if (dv != null) {
				offSprings[childIndex] = new Solution(dv);
			} else {
				offSprings[childIndex] = null;
			}
			
		}
		return offSprings;
	}

	
	

}
