package mktgbi.moea.operator;

import mktgbi.moea.Solution;

public interface ICrossover {

	public abstract Solution[] execute(Solution[] parents);

}