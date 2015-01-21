package mktgbi.moea.operator;

import mktgbi.moea.Solution;
import mktgbi.moea.SolutionSet;

public interface ISelector {

	/**
	 * Performs the operation
	 * 
	 * @param object
	 *            Object representing a SolutionSet
	 * @return the selected solution
	 */
	public abstract Solution[] execute(SolutionSet[] sets); // execute

}