package ecj;

import lucene.IndexInfoStaticG

import org.apache.lucene.search.BooleanClause
import org.apache.lucene.search.BooleanQuery
import org.apache.lucene.search.IndexSearcher
import org.apache.lucene.search.Query
import org.apache.lucene.search.TotalHitCountCollector

import query.ClassifyQuery
import ec.EvolutionState
import ec.Fitness
import ec.simple.SimpleStatistics

public class ClassifyGAStatistics extends SimpleStatistics {

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

		final GAFit cf = (GAFit) bestFitOfPop;

		// get test results on best individual
		try {

			Query q = cf.getQuery()
			IndexSearcher searcher = IndexInfoStaticG.instance.indexSearcher;

			TotalHitCountCollector collector = new TotalHitCountCollector();
			BooleanQuery.Builder bqb = new BooleanQuery.Builder();
			println "query in ga statistics is : " + q

			bqb.add(q, BooleanClause.Occur.MUST);

			bqb.add(IndexInfoStaticG.instance.catTestBQ, BooleanClause.Occur.FILTER);

			searcher.search(bqb.build(), collector);
			final int positiveMatchTest = collector.getTotalHits();
			// indexSearcher.search(q, filter0, collector

			// searcher.search(cf.getQuery(),
			// IndexInfoStaticG.instance.catTestF,
			// collector);

			collector = new TotalHitCountCollector();
			bqb = new BooleanQuery.Builder();
			bqb.add(q, BooleanClause.Occur.MUST);
			bqb.add(IndexInfoStaticG.instance.othersTestBQ, BooleanClause.Occur.FILTER);
			searcher.search(bqb.build(), collector);

			//	searcher.search(cf.getQuery(), IndexInfoStaticG.instance.othersTestF, collector);
			final int negativeMatchTest = collector.getTotalHits();

			cf.setTestValues(positiveMatchTest, negativeMatchTest);

			cf.setF1Test(ClassifyQuery.f1(positiveMatchTest, negativeMatchTest,
					IndexInfoStaticG.instance.totalTestDocsInCat));

			cf.setBEPTest(ClassifyQuery.bep(positiveMatchTest, negativeMatchTest,
					IndexInfoStaticG.instance.totalTestDocsInCat));

			//BooleanQuery q = (BooleanQuery) cf.getQuery();

			println  ( "Fitness: " + cf.fitness() + " FitnessToString: " + cf.fitnessToString() + "F1Test: "
			//		+ cf.getF1Test() + " F1Train: "
					// + " bepTest " + cf.getBEPTest()
				//	+ cf.getF1Train() + " positive match test: " + positiveMatchTest + " negative match test: "
			//		+ negativeMatchTest + " Total test: " + IndexInfoStaticG.instance.totalTestDocsInCat
			//		+ " Total terms in query: " + cf.getNumberOfTerms() + " min should match "
					//	+ q.getMinimumNumberShouldMatch()
					// + " neutralHit " + cf.getNeutralHit() + '\n' +
					+ '\n' + "cluster query sets map " + cf.getQMap()
					+ '\n' + "cluster query list " + cf.qList

					+ '\n' + "cluster totalPositiveScore " + cf.totalPositiveScore + " totalNegScore " + cf.totalNegativeScore
					+ " totalPosHits " + cf.totalPosHits + " totalNegHits " + cf.totalNegHits
					+ " duplicateCount "+ cf.duplicateCount
					+ " treeCount "+ cf.tree
					+ " noHitsCount "+ cf.noHitsCount
                    + " coreClusterPenalty " + cf.coreClusterP)
			//	+ " Query " + cf.getQuery().toString(IndexInfoStaticG.FIELD_CONTENTS) + '\n' + "Query min sorted: "
			//			+ cf.getQueryMinimal()); // + '\n'
			// + cf.getQueryJSONForViz() ) ;

		} catch (IOException e) {

			e.printStackTrace();
		}
	}
}
