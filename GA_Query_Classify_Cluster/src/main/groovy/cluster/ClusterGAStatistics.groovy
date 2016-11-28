package cluster;

import ec.EvolutionState
import ec.simple.SimpleStatistics
import index.IndexInfo

public class ClusterGAStatistics extends SimpleStatistics {

	public void finalStatistics(final EvolutionState state, final int result) {
		// print out the other statistics
		super.finalStatistics(state, result);
	}

	public void postEvaluationStatistics(EvolutionState state) {
		super.postEvaluationStatistics(state);
		
		ClusterFit cf = (ClusterFit) state.population.subpops.collect {sbp ->
			sbp.individuals.max() {ind ->
				ind.fitness.fitness()}.fitness
		}.max  {it.fitness()}
		
		println cf.queryShort()
		
		printf "PosHits: %d NegHits: %d PosScr: %.2f NegScr: %.2f ScrOnly: %.2f ScPlus: %.2f coreClstPen: %d noHitsPen: %d fit: %.2f emptyPen: %d  \n",   
		  cf.positiveHits, cf.negativeHits, cf.positiveScoreTotal as float, cf.negativeScoreTotal as float, cf.scoreOnly as float, cf.scorePlus as float, cf.coreClusterPenalty, cf.noHitsCount, cf.fitness(), cf.zeroHitsCount
		println "TotalHits: " + cf.totalHits + " Total Docs: " + IndexInfo.instance.indexReader.maxDoc()  +  " fraction: " + cf.fraction +  
		" baseFit: " + cf.baseFitness + " missedDocs: " + cf.missedDocs + " missedDocs: " + cf.missedDocs //+ " log(misseddocs): " +   Math.log(cf.missedDocs)
	}
}