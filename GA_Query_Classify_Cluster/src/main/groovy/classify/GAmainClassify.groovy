package classify

import index.IndexInfo
import classify.Effectiveness
import ec.*
import ec.util.*

class GAmainClassify extends Evolve {
 
	private final String parameterFilePath ='src/cfg/classify.params'
	private int totPosMatchedTest = 0, totTest = 0, totNegMatchTest = 0;
	private final static int NUMBER_OF_JOBS = 1
	private double microF1AllRunsTotal = 0, macroF1AllRunsTotal = 0, microBEPAllRunsTotal = 0;		

	public GAmainClassify(){
		println "Start..."
		EvolutionState state;

		Formatter bestResultsOut = new Formatter('results/resultsClassify.csv');
		final String fileHead = "categoryName, categoryNumber job, f1train, f1test, bepTest, totPositiveTest, totNegativeTest, totTestDocsInCat, query" + '\n';
	 
		ParameterDatabase parameters = null;
		final Date startRun = new Date();
		bestResultsOut.format("Start time: %s \n", startRun);
		bestResultsOut.format("%s", fileHead);

		(1..NUMBER_OF_JOBS).each{job ->
			parameters = new ParameterDatabase(new File(parameterFilePath));

			double macroF1 = 0;

			IndexInfo.NUMBER_OF_CATEGORIES.times{ categoryNumber ->

				IndexInfo.instance.setCategoryNumber(String.valueOf(categoryNumber))			
				IndexInfo.instance.setIndex()

				state = initialize(parameters, job);

				state.output.systemMessage("Job: " + job);
				state.job = new Object[1];
				state.job[0] = new Integer(job + categoryNumber);

				if (NUMBER_OF_JOBS >= 1) {
					final String jobFilePrefix = "job." + job + "." + categoryNumber
					state.output.setFilePrefix(jobFilePrefix)
					state.checkpointPrefix = jobFilePrefix 	+ state.checkpointPrefix
				}
				state.run(EvolutionState.C_STARTED_FRESH);

				def popSize=0;
				ClassifyFit cfit = (ClassifyFit) state.population.subpops.collect {sbp ->
					popSize= popSize + sbp.individuals.size()
					sbp.individuals.max() {ind ->
						ind.fitness.fitness()
					}.fitness
				}.max  {it.fitness()}
				println "pop size $popSize"

				//final GAFit cfit = (GAFit) bestFitInAllSubPops;
				def final testF1 = cfit.f1test
				def final trainF1 = cfit.f1train
				def final testBEP = cfit.BEPTest
				macroF1 += testF1;

				totPosMatchedTest += cfit.positiveMatchTest
				totNegMatchTest += cfit.negativeMatchTest
				totTest += IndexInfo.instance.totalTestDocsInCat;

				println "cfit.getQueryMinimal: ${cfit.getQueryMinimal()}"
			 
				bestResultsOut.format(
						"%s, %s, %d, %.3f, %.3f, %.3f, %d, %d, %d, %s \n",
						IndexInfo.instance.getCategoryName(), categoryNumber, job, trainF1, testF1, testBEP,
						cfit.positiveMatchTest,
						cfit.negativeMatchTest,
						IndexInfo.instance.totalTestDocsInCat,
						//cfit.getQuery(),
						//cfit.getQueryMinimal());
						cfit.getQueryString() )
				bestResultsOut.flush();
				println "Test F1 for cat $categoryNumber : $testF1 *******************************"
				cleanup(state);
			}

			final double microF1 = Effectiveness.f1(totPosMatchedTest,
					totNegMatchTest, totTest);
			final double microBEP = Effectiveness.bep(totPosMatchedTest,
					totNegMatchTest, totTest);

			macroF1 = macroF1 / IndexInfo.NUMBER_OF_CATEGORIES;
			println "OVERALL: micro f1:  $microF1  macroF1: $macroF1 microBEP: $microBEP";

			bestResultsOut.format(" \n");
			bestResultsOut.format("Run Number, %d", job );

			bestResultsOut
					.format(", Micro F1: , %.4f,  Macro F1: , %.4f, Micro BEP:, %.4f, Total Positive Matches , %d, Total Negative Matches, %d, Total Docs,  %d \n",
					microF1, macroF1, microBEP, totPosMatchedTest,
					totNegMatchTest, totTest);

			macroF1AllRunsTotal = macroF1AllRunsTotal + macroF1;
			microF1AllRunsTotal = microF1AllRunsTotal + microF1;
			microBEPAllRunsTotal = microBEPAllRunsTotal + microBEP;

			final double microAverageF1AllRuns = microF1AllRunsTotal / (job);
			final double microAverageBEPAllRuns = microBEPAllRunsTotal / (job);
			final double macroAverageF1AllRuns = macroF1AllRunsTotal / (job);

			bestResultsOut
					.format(",, Overall Micro F1 , %.4f,  Overall Macro F1, %.4f, Overall MicroBEP, %.4f",
					microAverageF1AllRuns,
					macroAverageF1AllRuns,
					microAverageBEPAllRuns);

			totPosMatchedTest = 0;
			totNegMatchTest = 0;
			totTest = 0;

			bestResultsOut.format(" \n");
			bestResultsOut.format(" \n");
			bestResultsOut.flush();

			println " ---------------------------------END-----------------------------------------------"
		}

		final Date endRun = new Date();
		def time= endRun.getTime() - startRun.getTime();
		println "Total time taken: $time"
		bestResultsOut.close();
	}

	static main (args){
		new GAmainClassify()
	}
}