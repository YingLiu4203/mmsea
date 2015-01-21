package mktgbi.moea;

public interface IDecisionVariables {

	
	public abstract int size(); // size

	/* (non-Javadoc)
	 * @see mktgbi.moea.IDecisionVariables#getVariable(int)
	 */
	public abstract double getSegmentCenter(int index);


	/* (non-Javadoc)
	 * @see mktgbi.moea.IDecisionVariables#getVariables()
	 */
	public abstract double[] getSegmentCenters();

	
	public abstract int[] getEdges();


	public abstract int[] getAssigned();

	/* (non-Javadoc)
	 * @see mktgbi.moea.IDecisionVariables#toString()
	 */
	public abstract String toString();
	
	public abstract int getNumSegments();

}