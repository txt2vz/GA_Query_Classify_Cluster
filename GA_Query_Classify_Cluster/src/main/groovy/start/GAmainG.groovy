package start
import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.Formatter;

import lucene.IndexInfoStaticG;
import query.ClassifyQuery
import ec.EvolutionState;
import ec.Evolve;
import ec.Fitness;
import ec.util.ParameterDatabase
import ecj.GAFit;
import ec.Fitness;

class GAmainG extends Evolve {

	private final String parameterFilePath =

	"src//cfg//OR.params"
	//	"src//cfg//SpanFirst.params"
	//"src//cfg//SpanNear10.params"
	//"src//cfg//SpanNear10Viz.params"
	//"src//cfg//ORMinShould.params"
	//"src//cfg//OR_NOT.params"
	//"src//cfg//AND.params"
	//"src//cfg//AND_ORG.params"
	//"src//cfg//Cluster_Viz.params"
	//"src//cfg//Cluster_Score.params"
	// "src//cfg//Cluster_Ranking.params"
	// "src//cfg//ClusterAND.params"
	//"src//cfg//AND_OR_Viz.params"

	private int totPosMatchedTest = 0, totTest = 0, totNegMatchTest = 0;

	private final static int NUMBER_OF_CATEGORIES = 2 , NUMBER_OF_JOBS = 2;

	private double microF1AllRunsTotal = 0, macroF1AllRunsTotal = 0,
	microBEPAllRunsTotal = 0;

	FileWriter queryFileWriter = new FileWriter('results//queries.txt')

	public GAmainG(){
		EvolutionState state;
		boolean viz=false
		//parameterFilePath.contains("Viz")

		Formatter bestResultsOut = new Formatter("results//results.csv");

		final String fileHead = "category, job, f1train, f1test, bepTest, totPositiveTest, totNegativeTest, totTestDocsInCat, query" + '\n';

		bestResultsOut.format("%s", fileHead);

		ParameterDatabase parameters = null;

		final Date startRun = new Date();

		NUMBER_OF_JOBS.times{job ->
			parameters = new ParameterDatabase(new File(parameterFilePath));

			double macroF1 = 0;

			for (cat in 0..<NUMBER_OF_CATEGORIES){

				IndexInfoStaticG.instance.setCatNumber(cat)
				IndexInfoStaticG.instance.setFilters()

				state = initialize(parameters, job);

				state.output.systemMessage("Job: " + job);
				state.job = new Object[1];
				state.job[0] = new Integer(job + cat);

				if (NUMBER_OF_JOBS >= 1) {
					final String jobFilePrefix = "job." + job + "." + cat;
					state.output.setFilePrefix(jobFilePrefix);

					state.checkpointPrefix = jobFilePrefix 	+ state.checkpointPrefix;
				}
				state.run(EvolutionState.C_STARTED_FRESH);
				def bestFitInAllSubPops=null;

				/*	state.population.subpops.each { subp ->
				 Fitness bestFitInSubPop = subp.individuals[0].fitness;
				 subp.individuals.each {indi ->
				 Fitness fit = indi.fitness
				 if (fit.betterThan(bestFitInSubPop))
				 bestFitInSubPop=fit;
				 }
				 if (bestFitInAllSubPops==null)
				 bestFitInAllSubPops=bestFitInSubPop
				 else if (bestFitInAllSubPops.betterThan(bestFitInSubPop))
				 bestFitInAllSubPops=bestFitInSubPop;
				 }
				 */
				for (int subPop = 0; subPop < state.population.subpops.length; ++subPop) {
					Fitness bestFitOfSubPop = state.population.subpops[subPop].individuals[0].fitness;

					for (int i = 1; i < state.population.subpops[subPop].individuals.length; ++i) {
						Fitness fit = state.population.subpops[subPop].individuals[i].fitness;
						if (fit.betterThan(bestFitOfSubPop)) {
							bestFitOfSubPop = fit;
						}
					}

					if (bestFitInAllSubPops == null) {
						bestFitInAllSubPops = bestFitOfSubPop;
					} else if (bestFitOfSubPop.betterThan(bestFitInAllSubPops)) {
						bestFitInAllSubPops = bestFitOfSubPop;
					}
				}

				final GAFit cfit = (GAFit) bestFitInAllSubPops;
				final float testF1 = cfit.getF1Test();
				final float trainF1 = cfit.getF1Train();
				final float testBEP = cfit.getBEPTest();
				macroF1 += testF1;

				totPosMatchedTest += cfit.getPositiveMatchTest();
				totNegMatchTest += cfit.getNegativeMatchTest();
				totTest += IndexInfoStaticG.instance.totalTestDocsInCat;

				println "cfit.getQueryMinimal: ${cfit.getQueryMinimal()}"

				if (viz)	{
					queryFileWriter = new FileWriter("results//query${cat}.txt")
					println "cfit.getQueryJSONforViz: ${cfit.getQueryJSONForViz()}"
					queryFileWriter << cfit.getQueryJSONForViz()
					queryFileWriter.flush()
				}

				bestResultsOut.format(
						"%s, %d, %.3f, %.3f, %.3f, %d, %d, %d, %s \n",
						cat, job, trainF1, testF1, testBEP,
						cfit.getPositiveMatchTest(),
						cfit.getNegativeMatchTest(),
						IndexInfoStaticG.instance.totalTestDocsInCat,
						//cfit.getQuery(),
						cfit.getQueryMinimal());

				bestResultsOut.flush();
				println "Test F1 for cat $cat : $testF1 *******************************"
				cleanup(state);
			}

			final double microF1 = ClassifyQuery.f1(totPosMatchedTest,
					totNegMatchTest, totTest);
			final double microBEP = ClassifyQuery.bep(totPosMatchedTest,
					totNegMatchTest, totTest);

			macroF1 = macroF1 / NUMBER_OF_CATEGORIES;
			println "OVERALL: micro f1:  $microF1  macroF1: $macroF1 microBEP: $microBEP";

			bestResultsOut.format(" \n");
			bestResultsOut.format("Run Number, %d", job);
			//	bestResultsOut
			//			.format(", Micro F1: , %.4f, Micro bep: , %.4f, Macro F1: , %.4f,  Total Positive Matches , %d, Total Negative Matches, %d, Total Docs,  %d \n",
			//		microF1, microBEP, macroF1, totPosMatchedTest,
			//		totNegMatchTest, totTest);
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
			//	bestResultsOut
			//			.format(",, Overall Micro F1 , %.4f, Overall Micro BEP , %.4f, Overall Macro F1, %.4f",
			//			microAverageF1AllRuns, microAverageBEPAllRuns,
			//		macroAverageF1AllRuns);

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
		queryFileWriter.flush()
		queryFileWriter.close()
	}

	static main (args){
		new GAmainG()
	}
}
