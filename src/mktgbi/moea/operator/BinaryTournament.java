/**
 * BinaryTournament.java
 * @author Juan J. Durillo
 * @version 1.0
 */
package mktgbi.moea.operator;

import java.util.Comparator;

import mktgbi.moea.Solution;
import mktgbi.moea.SolutionSet;
import mktgbi.util.GaRandom;

/**
 * This class implements an opertor for binary selections
 */
public abstract class BinaryTournament implements ISelector {

	/**
	 * Stores the <code>Comparator</code> used to compare two solutions
	 */
	private Comparator<Solution> comparator_;

	/**
	 * Constructor Creates a new Binary tournament operator using a
	 * BinaryTournamentComparator
	 */
	public BinaryTournament() {
		comparator_ = new FitnessComparator();
	} // BinaryTournament

	/**
	 * Constructor Creates a new Binary tournament with a specific
	 * <code>Comparator</code>
	 * 
	 * @param comparator
	 *            The comparator
	 */
	public BinaryTournament(Comparator<Solution> comparator) {
		comparator_ = comparator;
	} // Constructor

	/* (non-Javadoc)
	 * @see mktgbi.moea.operator.ISelector#execute(mktgbi.moea.SolutionSet[])
	 */
	public Solution[] execute(SolutionSet[] sets) {
		Solution[] retValues = new Solution[2];
		Solution[] solutions = selectFour(sets);
		retValues[0] = selectOne(solutions[0], solutions[1]);
		retValues[1] = selectOne(solutions[2], solutions[3]);
		
		return retValues;
	} // execute
	
	private Solution selectOne(Solution solution1, Solution solution2) {
		int flag = comparator_.compare(solution1, solution2);
		if (flag == -1)
			return solution1;
		else if (flag == 1)
			return solution2;
		else if (GaRandom.nextDouble() < 0.5)
			return solution1;
		else
			return solution2;
	}
	
	protected abstract Solution[] selectFour(SolutionSet[] sets);
} // BinaryTournament
