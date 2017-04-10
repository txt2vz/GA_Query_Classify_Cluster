package clusterGP;

import ec.EvolutionState
import ec.gp.koza.KozaShortStatistics
import ec.simple.SimpleStatistics
import index.IndexInfo


public class ClusterGPStatistics extends KozaShortStatistics {

	public void finalStatistics(final EvolutionState state, final int result) {
		// print out the other statistics
		super.finalStatistics(state, result);
	}

	public void postEvaluationStatistics(EvolutionState state) {
	
	super.postEvaluationStatistics(state);
		
	
		GPfit cf = (GPfit) state.population.subpops.collect {sbp ->
			sbp.individuals.max() {ind ->
				ind.fitness.fitness()}.fitness
		}.max  {it.fitness()}
		
		println "stats1"
	//	println "fiittt " + cf.gpf
		
		println "cf.totostring " + cf.toString(state.generation)
	//	println cf.queryShort()
		
//		printf "PosHits: %d NegHits: %d PosScr: %.2f NegScr: %.2f ScrOnly: %.2f ScPlus1000: %.2f coreClstPen: %d lowSubqHits(AND): %d fit: %.2f zeoHitsCount: %d  \n",
//		  cf.positiveHits, cf.negativeHits, cf.positiveScoreTotal as float, cf.negativeScoreTotal as float, cf.scoreOnly as float, cf.scorePlus1000 as float, cf.coreClusterPenalty, cf.lowSubqHits, cf.fitness(), cf.zeroHitsCount
	//	println "TotalHits: " + cf.totalHits + " Total Docs: " + IndexInfo.instance.indexReader.maxDoc()  +  " fraction: " + cf.fraction +
		//" baseFit: " + cf.baseFitness + " missedDocs: " + cf.missedDocs + " duplicate count " + cf.duplicateCount//+ " log(misseddocs): " +   Math.log(cf.missedDocs)
	}
}