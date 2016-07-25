package query

import lucene.ImportantWords
import lucene.IndexInfo

import org.apache.lucene.index.Term
import org.apache.lucene.search.BooleanClause
import org.apache.lucene.search.BooleanQuery
import org.apache.lucene.search.IndexSearcher
import org.apache.lucene.search.Query
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

public class ORG extends Problem implements SimpleProblemForm, MatchT {

	private String[] wordArray;
	Query query;

	public void setup(final EvolutionState state, final Parameter base) {

		super.setup(state, base);

		println("Total train docs in ORG for cat:  " + IndexInfo.instance.getCatnumberAsString() + " "
				+ IndexInfo.instance.totalTrainDocsInCat + " Total test docs for cat "
				+ IndexInfo.instance.totalTestDocsInCat);

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
		
		def genes =[] as Set
		int duplicateCount=0;

		intVectorIndividual.genome.each {gene ->
			if (!genes.add(gene)) duplicateCount = duplicateCount + 1;

			if (gene < wordArray.size() && gene >= 0){
				String wrd = wordArray[gene]

				bqb.add(new TermQuery(new Term(IndexInfo.FIELD_CONTENTS, wrd)), BooleanClause.Occur.SHOULD);
			}

			query = bqb.build();

			IndexSearcher searcher = IndexInfo.instance.indexSearcher;
			int positiveMatch = getPositiveMatch(searcher, query)
			int negativeMatch = getNegativeMatch(searcher, query)		

			def F1train = ClassifyQuery.f1(positiveMatch, negativeMatch, IndexInfo.instance.totalTrainDocsInCat);

			fitness.setTrainValues(positiveMatch, negativeMatch);
			fitness.setF1Train(F1train);
			fitness.setQuery(query);

			float rawfitness = F1train / (duplicateCount +1);

			((SimpleFitness) intVectorIndividual.fitness).setFitness(state,
					rawfitness, false);

			ind.evaluated = true;
		}
	}
}