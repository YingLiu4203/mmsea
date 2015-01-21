/**
 * FitnessComparator.java
 * 
 * @author Juan J. Durillo
 * @version 1.0
 */
package mktgbi.moea.operator;

import java.util.Comparator;

import mktgbi.moea.Solution;


/**
 * This class implements a <code>Comparator</code> (a method for comparing
 * <code>Solution</code> objects) based on the fitness value returned by the
 * method <code>getFitness</code>.
 */
public class FitnessComparator implements Comparator<Solution> {
   
 /**
  * Compares two solutions.
  * @param o1 Object representing the first <code>Solution</code>.
  * @param o2 Object representing the second <code>Solution</code>.
  * @return -1, or 0, or 1 if o1 is less than, equal, or greater than o2,
  * respectively.
  */
  public int compare(Solution o1, Solution o2) {
    if (o1==null)
      return 1;
    else if (o2 == null)
      return -1;
    
    double fitness1 = o1.getFitness();
    double fitness2 = o2.getFitness();
    if (fitness1 <  fitness2) {
      return -1;
    }
    
    if (fitness1 >  fitness2) {
      return 1;
    }
    
    return 0;
  } // compare    
} // FitnessComparator
