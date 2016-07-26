package classify;

import org.apache.lucene.search.BooleanClause
import org.apache.lucene.search.BooleanQuery
import org.apache.lucene.search.IndexSearcher
import org.apache.lucene.search.Query
import org.apache.lucene.search.TotalHitCountCollector

import classify.Effectiveness
import ec.EvolutionState
import ec.simple.SimpleStatistics
import index.IndexInfo;

public class ClassifyGAStatistics extends SimpleStatistics {

	public void finalStatistics(final EvolutionState state, final int result) {
		// print out the other statistics
		super.finalStatistics(state, result);
	}

	public void postEvaluationStatistics(EvolutionState state) {
		super.postEvaluationStatistics(state);
		//		Fitness bestFitOfSubp = null, bestFitOfPop = null;
		//		for (int subPop = 0; subPop < state.population.subpops.length; ++subPop) {
		//			bestFitOfSubp = state.population.subpops[subPop].individuals[0].fitness;
		//			for (int i = 1; i < state.population.subpops[subPop].individuals.length; ++i) {
		//				Fitness fit = state.population.subpops[subPop].individuals[i].fitness;
		//				if (fit.betterThan(bestFitOfSubp))
		//					bestFitOfSubp = fit;
		//			}
		//			if (bestFitOfPop == null)
		//				bestFitOfPop = bestFitOfSubp;
		//			else if (bestFitOfSubp.betterThan(bestFitOfPop))
		//				bestFitOfPop = bestFitOfSubp;
		//		}
		//
		//		final GAFit cf = (GAFit) bestFitOfPop;

		GAFit gaFit = (GAFit) state.population.subpops.collect {sbp ->
			sbp.individuals.max() {ind ->
				ind.fitness.fitness()}.fitness
		}.max  {it.fitness()}


		// get test results on best individual
		Query q = gaFit.getQuery()
		IndexSearcher searcher = IndexInfo.instance.indexSearcher;

		TotalHitCountCollector collector = new TotalHitCountCollector()
		BooleanQuery.Builder bqb = new BooleanQuery.Builder()

		bqb.add(q, BooleanClause.Occur.MUST);
		bqb.add(IndexInfo.instance.catTestBQ, BooleanClause.Occur.FILTER);

		searcher.search(bqb.build(), collector);
		final int positiveMatchTest = collector.getTotalHits();

		collector = new TotalHitCountCollector();
		bqb = new BooleanQuery.Builder();
		bqb.add(q, BooleanClause.Occur.MUST);
		bqb.add(IndexInfo.instance.othersTestBQ, BooleanClause.Occur.FILTER);
		searcher.search(bqb.build(), collector);

		final int negativeMatchTest = collector.getTotalHits();

		gaFit.setTestValues(positiveMatchTest, negativeMatchTest);

		gaFit.setF1Test(Effectiveness.f1(positiveMatchTest, negativeMatchTest,
				IndexInfo.instance.totalTestDocsInCat));

		gaFit.setBEPTest(Effectiveness.bep(positiveMatchTest, negativeMatchTest,
				IndexInfo.instance.totalTestDocsInCat));

		println   "Fitness: " + gaFit.fitness() + "F1Test: " + gaFit.getF1Test() +
				" F1Train: " + gaFit.getF1Train() + " positive match test: " + positiveMatchTest +
				" negative match test: " + negativeMatchTest

		//println "Query: " + gaFit.getQuery()
		//println "QueryMinimal: " + gaFit.getQueryMinimal()
		println "QueryString: " + gaFit.getQueryString()
	}
}