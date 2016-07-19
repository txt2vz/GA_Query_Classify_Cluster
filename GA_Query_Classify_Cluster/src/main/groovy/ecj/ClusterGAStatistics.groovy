package ecj;

import java.io.IOException;

import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query
import org.apache.lucene.search.TotalHitCountCollector;

import ec.EvolutionState;
import ec.Fitness;
import ec.simple.SimpleStatistics;
import lucene.IndexInfoStaticG
import query.ClassifyQuery;;

public class ClusterGAStatistics extends SimpleStatistics {

	public void finalStatistics(final EvolutionState state, final int result) {
		// print out the other statistics
		super.finalStatistics(state, result);
	}

	public void postEvaluationStatistics(EvolutionState state) {
		super.postEvaluationStatistics(state);
		Fitness bestFitOfSubp = null, bestFitOfPop = null;
		for (int subPop = 0; subPop < state.population.subpops.length; ++subPop) {
			bestFitOfSubp = state.population.subpops[subPop].individuals[0].fitness;
			for (int i = 1; i < state.population.subpops[subPop].individuals.length; ++i) {
				Fitness fit = state.population.subpops[subPop].individuals[i].fitness;
				if (fit.betterThan(bestFitOfSubp))
					bestFitOfSubp = fit;
			}
			if (bestFitOfPop == null)
				bestFitOfPop = bestFitOfSubp;
			else if (bestFitOfSubp.betterThan(bestFitOfPop))
				bestFitOfPop = bestFitOfSubp;
		}

		final ClusterFit cf = (ClusterFit) bestFitOfPop;

		println  ( 		
				cf.queryShort() +   '\n' 
				+ "PosHits " + cf.posHits + "  NegHits " + cf.negHits
				+ " PosScore " + cf.positiveScore + " NegScore " + cf.negativeScore				
				+ " duplicatePen "+ cf.duplicateCount
				+ " treePen "+ cf.treePenalty
				+ " graphPen "+ cf.graphPenalty
				+ " noHitsPen "+ cf.noHitsCount
				+ " coreClusterPen " + cf.coreClusterPenalty 
				+ " fit: " + cf.fitness() + '\n'			
				);
	}
}
