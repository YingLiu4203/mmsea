/**
 * this is based on jmetal.metaheuristics SPEA2.java
 * 
 */

package mktgbi.moea;

import java.io.IOException;
import java.util.Comparator;
import java.util.logging.Logger;

import mktgbi.algorithm.util.RunMode;
import mktgbi.dataio.Config;
import mktgbi.moea.operator.ICrossover;
import mktgbi.moea.operator.ISelector;
import mktgbi.moea.operator.SolutionComparator;
import mktgbi.moea.problem.Problem;

/**
 * This class representing the SPEA2 algorithm
 */
public class Evolution {
	private static Logger sm_logger = Config.SM_LOGGER; 
	
	private String m_objectivesFilename = null;
	private String m_variablesFilename = null;
	private String m_membersFilename = null;
	private String m_segmentSizeFilename = null;
	private String m_filenameAppendix = null;

	//Stores the problem to solve
	private Problem m_problem;
	
	private int m_minNumSegments = 0;
	private int m_maxNumSegments = 0;
	int m_setSize = 0;
	private SolutionSet[] m_solutionSet = null;
	private SolutionSet[] m_archive = null; 
	private int m_evaluations = 0;
	
	private int m_populationSize, m_archiveSize, m_maxEvaluations;
	private ICrossover m_crossoverOperator = null;
	private ISelector m_selectionOperator = null;
	
	private int m_reportInterval = 0; 
	private static int SM_REPORT_FREQUENCY = 10;
	private static int sm_numOfAdded = 0;
	
	private RunMode m_runMode = null;
	
	/**
	 * Constructor. Create a new SPEA2 instance
	 * 
	 * @param problem
	 *            Problem to solve
	 */
	public Evolution(Problem problem, RunMode runMode) {
		this.m_problem = problem; 
		m_runMode = runMode;
		
		m_objectivesFilename = Config.OBJECTIVES_FILENAME;
		m_variablesFilename = Config.VARIABLES_FILENAME;
		m_membersFilename = Config.MEMBERS_FILENAME;
		m_segmentSizeFilename = Config.SEGMENT_SIZES_FILENAME;
		m_filenameAppendix = Config.FILENAME_APPENDIX;
		
		
		// Read the parameters
		m_populationSize = Config.getPopulationSize();
		m_archiveSize = Config.getArchiveSize();
		m_maxEvaluations = Config.getNumEvaluations();
		
		// Mutation and Crossover for Real codification
		m_crossoverOperator = problem.getCrossoverOperator();
		sm_logger.info("Crossover operator is: " + m_crossoverOperator.getClass().getSimpleName());

		// Selection Operator
		m_selectionOperator = problem.getSelectorOperator();
		sm_logger.info("Selection operator is: " + m_selectionOperator.getClass().getSimpleName());
		
		// Initialize the variables
		m_minNumSegments = Config.getMinNumSegments();
		m_maxNumSegments = Config.getMaxNumSegments();
		m_setSize = m_maxNumSegments - m_minNumSegments + 1;
		m_solutionSet = new SolutionSet[m_setSize];
		m_archive = new SolutionSet[m_setSize];
		for (int setIndex = 0; setIndex < m_setSize; setIndex++) {
			m_solutionSet[setIndex] = new SolutionSet(m_populationSize);
			m_archive[setIndex] = new SolutionSet(m_archiveSize);
		}
	} 

	/**
	 * Runs of the Spea2 algorithm.
	 * 
	 * @return a <code>SolutionSet</code> that is a set of non dominated
	 *         solutions as a result of the algorithm execution
	 * @throws IOException 
	 * @throws JMException
	 */
	public SolutionSet[] execute() throws IOException {
		
		m_evaluations = 0;
		m_reportInterval = m_maxEvaluations / SM_REPORT_FREQUENCY;
		
		if (m_reportInterval == 0){
			m_reportInterval = 1;
		}
		
		if (m_runMode == RunMode.READ_CHROMS || m_runMode == RunMode.READ_DETAIL) {
			
			for (int setIndex = 0; setIndex < m_setSize; setIndex++) {
				int segmentNumber = setIndex + m_minNumSegments;
				String filename = m_variablesFilename + "_" + String.valueOf(segmentNumber) 
					+ "_" + String.valueOf(0) +	m_filenameAppendix;
				m_archive[setIndex] = m_problem.initFromFile(filename);
				evaluateSolutioinSet(m_archive);
			}
		
			sm_logger.info("Read inital archive: " + reportArchive());
			
		} else {
			initilizeSolutionSet();
			if (m_runMode == RunMode.DETAIL) {
				saveResults();
			}
		}
		
		evolve();
		
		return m_archive;
	} // execute
	

	private void initilizeSolutionSet() {
		sm_logger.info("Start solution set initialization");
		// -> Create the initial solutionSet
		m_solutionSet = m_problem.initializeSolutions();
		
		archiveSolutionSet();
		sm_logger.info("Initial archive: " + reportArchive());
		sm_numOfAdded = 0;
		
	}
	
	private void evolve() {
		sm_logger.info("Start evolving solution set.");
		
		int reportNumber = m_reportInterval;
		while (m_evaluations < m_maxEvaluations) {
			executeOperations();
			archiveSolutionSet();
			
			if (m_evaluations > reportNumber) {
				sm_logger.info("Generation number is " + m_evaluations +
						". Number of added solutions: " + sm_numOfAdded +
						". Archive status: " + reportArchive());
				reportNumber += m_reportInterval;
				sm_numOfAdded = 0;
				
				if (m_runMode == RunMode.DETAIL || m_runMode == RunMode.READ_DETAIL) {
					saveResults();
				}
			}
		} // while evaluations
		
		saveResults();
		sm_logger.info("Evolution is done with number of evaluations: " + m_evaluations);
	}
	
	private String reportArchive() {
		StringBuilder retValue = new StringBuilder();
		for (int setIndex = 0; setIndex < m_setSize; setIndex++) {
			int segmentNumber = setIndex + m_minNumSegments;
			retValue.append(" [Segment ");
			retValue.append(segmentNumber);
			retValue.append(" Size:");
			retValue.append(m_archive[setIndex].size());
			retValue.append( "] ");
		}
		return retValue.toString();
		
	}
	
	private void archiveSolutionSet() {
		
		for (int setIndex = 0; setIndex < m_setSize; setIndex++) {
			SolutionSet solutionSet = m_solutionSet[setIndex]; 
			SolutionSet archiveSet = m_archive[setIndex];
		
			int oldSize = archiveSet.size();
			SolutionSet union = SolutionSet.union(solutionSet, archiveSet);
			sm_numOfAdded += (union.size() - oldSize);
			
			Selection selection = new Selection(union);
			m_archive[setIndex] = selection.environmentalSelection(m_archiveSize);
		}
	}
	
	private void executeOperations() {
		
		SolutionSet[] offSpringSets = new SolutionSet[m_setSize];
		for (int setIndex = 0; setIndex < m_setSize; setIndex++) {
			offSpringSets[setIndex] = new SolutionSet();
		}
		
		int offSpringSize = 0; 
		while (offSpringSize < m_populationSize && m_evaluations < m_maxEvaluations) {
			
			Solution[] parents = m_selectionOperator.execute(m_archive);
			Solution[] offsprings =  m_crossoverOperator.execute(parents);
			
			for (int childIndex = 0; childIndex < offsprings.length; childIndex ++) {
				Solution offspring = offsprings[childIndex];
				if (offspring != null) {
					
					int numSegments = offspring.getDecisionVariables().getNumSegments();
					if ((numSegments >= m_minNumSegments) && (numSegments <= m_maxNumSegments)) {
						m_problem.evaluate(offspring);
						m_evaluations++;
						int setIndex = numSegments - m_minNumSegments;
						offSpringSets[setIndex].add(offspring);
						offSpringSize++;
					}
					
				} // else not a valid solution
			}		
		}
		m_solutionSet = offSpringSets;
	}
	
	private void saveResults() {
		// sort the final solution set
		Comparator<Solution> comparator = new SolutionComparator();
		
		for (int setIndex = 0; setIndex < m_setSize; setIndex++) {
			int segmentNumber = setIndex + m_minNumSegments;
			String objectivesFilename = m_objectivesFilename + "_" + String.valueOf(segmentNumber) 
					+ "_" + String.valueOf(m_evaluations) + 	m_filenameAppendix;
			String variablesFilename = m_variablesFilename + "_" + String.valueOf(segmentNumber) 
					+ "_" + String.valueOf(m_evaluations) +	m_filenameAppendix;
			String membersFilename = m_membersFilename + "_" + String.valueOf(segmentNumber) 
			+ "_" + String.valueOf(m_evaluations) +	m_filenameAppendix;
			String segmentSizeFilename = m_segmentSizeFilename + "_" + String.valueOf(segmentNumber) 
					+ "_" + String.valueOf(m_evaluations) +	m_filenameAppendix;
			SolutionSet archive = m_archive[setIndex];
			archive.sort(comparator);
			archive.printObjectivesToFile(objectivesFilename);
			archive.printVariablesToFile(variablesFilename);
			archive.printMembersToFile(membersFilename);
			archive.printSegmentSizesToFile(segmentSizeFilename);
		}
	}
	
	private void evaluateSolutioinSet(SolutionSet[] solutionSets) {
		int size = solutionSets.length;
		for (int setIndex = 0; setIndex < size; setIndex++) {
			SolutionSet solutionSet = solutionSets[setIndex];
			int solutionSize = solutionSet.size();
			for (int index = 0; index < solutionSize; index++) {
				Solution solution = solutionSet.get(index);
				m_problem.evaluate(solution);
			}
		}
	}
} 
