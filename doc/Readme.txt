
MMSEA code description
To use server version JVM, copy the JDK bin\server to the corresponding JRE path.  
To process big data file, you may want to set JVM (-vmargs) parameters:
 -server -Xmn200M -Xms1024M  -Xmx1024M
-server  use the server version of JVM

-Xmn<size> set the size of heap for the young generation, it should be smaller than Xms size.
-Xms<size> set initial Java heap size, for Server apps, set it to Xmx size. 
-Xmx<size> set maximum Java heap size
JVM starts with -Xms amount of memory for the heap (storing objects etc.) and can grow to a maximum 
of -Xmx amount of memory.
For a new experiment, set DataFilename, NumberOfRows, NumberOfGenerations, MinNumberOfClusters, and MaxNumberOfClusters
To run Regression, we need to set 
1)	DimensionSize = 1, number of IVs
2)	DistanceType = NONE, EUCLIDEAN
3)	InitMode = CLUSTERWISE
4)	Usually DensityStrategyRate = 0.5
To run Joint segmentation, we need to set 
1)	DimensionSize = size of Dim1, size of Dim2
2)	DistanceType = EUCLIDEAN, EUCLIDEAN
3)	InitMode = KMEANS_MST
4)	Usually DensityStrategyRate = 1

Experimentation with Mmsea.Java
Configure the algorithm and change parameter values in an associated Settings object.
It first creates the problem. The Problem class defines two methods that will be used by the algorithm: solution evaluation and solution initialization. Then it creates the Settings object to create, configure and run the Algorithm object.
Define the optimization problem: Problem.java and JointSeg.java
The Problem class and its sub class define two methods that will be used by the algorithm: solution evaluation and solution initialization.
Setting parameters: SpeaIISettings.java
It has a Problem object and has methods to create Algorithm object and configure its parameters via Properties object.  It returns the Algorithm object to be used in experiment.  User extends this class and set the corresponding parameters for the algorithm. 
The parameters are stored in three Map objects of the created Algorithm object. They include 
•	Input Parameters
o	population size, archive size
o	max number of evaluations
•	Operator Parameters
o	Set Mutation operator and mutation probability
o	Set Crossover operator and crossover probability. 
o	Set Selection operator
•	Output Parameters
The algorithm: MsSpeaII.java
In its execute() method, it reads all parameters in the Settings file: input parameters and operator parameters. It creates the solution set and archive set objects. Then it calls Problem object’s initialSolution() method and evolves. 
In evolve(), it unions solution set and archive set, assigns fitness to the union, run Spea2Fitness’s enviornmentSelection() method to generate the new archive set. 
Then, it calls SelectionOperator to select two offsprings from the archive, runs crossover and mutation on the new offsprings. Finally, the offsrings become the new solution set. 

Entry point: MMSEA.java
The command line argument is run mode. Run modes are defined in runMode class and include:
1) Default: no chroms saved, run from start to finish
2) Save chroms: save clusterwise chroms
3) Read chroms: read chroms from file (don’t generate chroms)
4) Generate chroms: use a new random seed to generate chroms and quit. 
Then in the main method 
1) Initialize configuration: Config.init() to set parameters, set random see.
2) Set the run mode, random seed, check the distance type to determine the segmentation type (joint or regression). Initialize R for regression. 
3) Initialize data source: dataSource.init()
4) Create JointSeg (the Problem), SpeaIISettings(Settings), and calls Settings.configure() to get the Algorithm (MsSpeaII).
5) Save solutions: Solution.saveSolutions() to Write objectives and memberships into result files.

System Configuration: Config.java
The user needs to create directories defined in configuration file (such as the data and output directories) 
 It defines a number of parameters (input/output data, runtime parameters) for the program. 
Configuration file is defined as CONFIG_FILENAME constant (default is "config\\GaMain.Properties")
Read configurations using PropertiesConfiguration methods. 
1)	DataFilename: input data filename. The first line contains variable names and will be skipped. 
2)	OutputFilename: output file name (without appendix). Default is “output\\ms_out”.
3)	RadomSeed: the initial seed used in random generation. Default is 1997.
4)	NumberOfGenerations: number of generations of GA. The default is 10 – good for testing purpose.
5)	NumberOfRows: number of records in data file. The minimum number of rows is 100.
6)	MinClusterSize: the minimum cluster (segment) size. Default is 30.
7)	DimensionSizes: size (number of columns) of each dimension.
8)	InitMode: the mode of initial solution set generation. 
a.	Use cluster wise to generate chromosome: CLUSTERWISE
b.	 Use k-means: KMEANS
c.	Use random cluster assignment
9)	 randomly generate chromosome: RANDOM
10)	DistanceTypes: we use this to distinguish regression and joint segmentation. If the first parameter is NONE, this is a regression. Otherwise, it’s a joint segmentation.
11)	InitChromEdgesFilename: the file used to save initialized chromosome edge file. The regression initialization is very slow, thus use this option to save the initialization result.
12)	MaxClusterwiseIteration: number of iterations in for clusterwise objective calculation.  
Input data: DataSource.java
Input Data format
1)	Each line is one row while the first line contains the variable names that will be skipped during data reading.
2)	Columns are separated by white space character [ \f\n\r\t].
3)	All data are standardized Z value of double type.
Read input data: readData() stores read-in data into DataBin. Each data bin for one dimension and there is a data bin for all attributes. 
Dimension data: DataBin.java
Each dimension data is stored in a DataBin object. DataBin stores original dimension data, distances between any two rows, and sorted neighbors. 
1)	If the distance is NONE, it’s a response variable. No distance values are calculated.
2)	The memory usage for distance is N*N*8 (bytes for double) /2 while for neighbors is N*N*4Byte for integer. The memory space for a 5,000 rows, 10 columns data are : the distance matrix: 8 * 5000 * 5000 / 2  = 100 MB. The ordered neighbor list: 4 (int type) * 5000 * 5000 = 100 MB. The total is 200MB/dimension. So the space required is about 8 * n * n for each dimension where n is the number of rows. For 10,000 rows, the space is 800 MB for each dimension. 
DataBin has some help functions to calculate distance between two rows, calculate centers for the specified set of rows, and generate minimum spanning tree. 
SPEA algorithm description
Selection first adds offspring to population. Then it calculates fitness for all individuals, calculates distance matrix, performs environmental selection (truncate all to size alpha), then it fills mating pool by binary tournament. 
Calculate fitness: 

The main algorithm: GeneticEvolution.java
In constructor, initialize the static context of Solution class. Set IP, EP, generations and crossover parameters. 
In run() method, first generate the set of initial solutions, then archive and evolve. 
In archive() method, merge IP into EP (add all non-dominated solutions), calculate solution box number (location in solutions space),  update solution box density. If the generated EP size is bigger than configured size, remove extra EP solutions from the most crowded solution boxes. 
In evolve() method, for the specified number of generations, reproduce solutions and archive. 
In reproduce() method, clean IP, then generate the specified number of IP solutions: select two parents (each one is selected by binary tournament with a winner from less crowded solution box), crossover the two to generate two children, then mutate two children. The two generated children are added to IP. 

Solution:  Solution.java

In static initStaticContext() method, context variables such as number of objectives, number of objective grids, number of min/max clusters etc, are initialized. Static chromosome is also initialized. 

The constructor of Solution is private to make sure every instance is evaluated. The member variables are chromosome, objectives, box number, and number of clusters. 

initSolutions() method calls Chromosome.initChroms() and creates solution instances from chromosomes by evaluating each chromosome. 

Chromosome: Chromosome.java

In initStaticContext(): numRows, mutation rate, min clusters, max clusters, and minClusterSize.

initChroms() calls initByDataBin() to create chromosome for each dimension and all attributes. 

In initByDataBin(), half is generated by MST and the rest is by k-means.

In initByMST(), the first is to generate original MST. Then re-link the initial MST edges to generate new edges, decode the edges to create new chromosome. (!!!! The relink always starts with first set of edgeDegree – not useful). 


In initByKmeans(), run k-means algorithm to generate assigned array, 

MinSpanTree (MST): 

MST uses edges and sorted neighbors to build the MST for all data rows. First, put node 0 in put MST, then find the nearest node that is not in MST, add to MST. Repeat till no node left. Consequently, the root node is the initial one, 2nd is the nearest node to the root, the 3rd is the node that have minimum distance to 1st and 2nd, and so on so forth. 

MST maintains an interesting link member that is a set of pairs. Each pair is the minimum distance rank between two nodes. It’s sorted in descending order. 

