package mktgbi.moea.operator;

import mktgbi.moea.Solution;
import mktgbi.moea.SolutionSet;
import mktgbi.util.GaRandom;

public class MixSetSelector extends BinaryTournament {

	@Override
	protected Solution[] selectFour(SolutionSet[] sets) {
		// we should select solutions with the same number of segments
		Solution[] retValues = new Solution[4];
		int lastSetIndex = sets.length -1;

		for (int index = 0; index < retValues.length; index++) {
			int setIndex = GaRandom.nextInt(0, lastSetIndex);
			SolutionSet set = sets[setIndex];
			int lastSolutionIndex = set.size() - 1;
			int selectIndex = GaRandom.nextInt(0, lastSolutionIndex);
			retValues[index] = set.get(selectIndex);
		}
		return retValues;
	}

}

