package query;

import lucene.ImportantWords
import lucene.IndexInfoStaticG

import org.apache.lucene.index.Term
import org.apache.lucene.search.BooleanClause
import org.apache.lucene.search.BooleanQuery
import org.apache.lucene.search.IndexSearcher
import org.apache.lucene.search.TermQuery

import ec.EvolutionState
import ec.Individual
import ec.Problem
import ec.simple.SimpleFitness
import ec.simple.SimpleProblemForm
import ec.util.Parameter
import ec.vector.IntegerVectorIndividual
import ecj.GAFit;

/**
 * To generate queries to perform binary text classification using GA string of
 * integer pairs which are translated into spanFirst queries
 * 
 * @author Laurie
 */

public class ANDG extends Problem implements SimpleProblemForm, MatchT {

	private String[] wordArray;
	BooleanQuery query;

	public void setup(final EvolutionState state, final Parameter base) {

		super.setup(state, base);

		println("Total docs for cat  " + IndexInfoStaticG.instance.getCatnumberAsString() + " "
				+ IndexInfoStaticG.instance.totalTrainDocsInCat + " Total test docs for cat "
				+ IndexInfoStaticG.instance.totalTestDocsInCat);

		ImportantWords iw = new ImportantWords();
		wordArray = iw.getF1WordList(false, true);
	}

	public void evaluate(final EvolutionState state, final Individual ind,
			final int subpopulation, final int threadnum) {

		if (ind.evaluated)
			return;

		GAFit fitness = (GAFit) ind.fitness;
		BooleanQuery.Builder bqb = new BooleanQuery.Builder();

		IntegerVectorIndividual intVectorIndividual = (IntegerVectorIndividual) ind;

		// create query from Map
		query = new BooleanQuery(true);
		for (int i = 0; i < (intVectorIndividual.genome.length - 1); i = i + 1) {

			// any ints below 0 are ignored
			int wordInd;
			if (intVectorIndividual.genome[i] >= wordArray.length
			|| intVectorIndividual.genome[i] < 0)
				continue;
			else
				wordInd = intVectorIndividual.genome[i];

			final String word = wordArray[wordInd];


			bqb.add(new TermQuery(new Term(IndexInfoStaticG.FIELD_CONTENTS, word)), BooleanClause.Occur.MUST);
		}


		query = bqb.build();

		IndexSearcher searcher = IndexInfoStaticG.instance.indexSearcher;
		int positiveMatch = getPositiveMatch(searcher, query)
		int negativeMatch = getNegativeMatch(searcher,query)

		def F1train = ClassifyQuery.f1(positiveMatch, negativeMatch, IndexInfoStaticG.instance.totalTrainDocsInCat);

		fitness.setTrainValues(positiveMatch, negativeMatch);
		fitness.setF1Train(F1train);
		fitness.setQuery(query);

		float rawfitness = F1train;

		((SimpleFitness) intVectorIndividual.fitness).setFitness(state,
				rawfitness, false);

		ind.evaluated = true;
	}
}
