package classify.query;

import org.apache.lucene.index.Term
import org.apache.lucene.search.BooleanClause
import org.apache.lucene.search.BooleanQuery
import org.apache.lucene.search.IndexSearcher
import org.apache.lucene.search.TermQuery
import org.apache.lucene.search.spans.SpanFirstQuery;
import org.apache.lucene.search.spans.SpanTermQuery;

import ec.EvolutionState
import ec.Individual
import ec.Problem
import ec.simple.SimpleFitness
import ec.simple.SimpleProblemForm
import ec.util.Parameter
import ec.vector.IntegerVectorIndividual
import classify.Effectiveness;
import classify.GAFit;
import groovy.transform.CompileStatic
import index.ImportantWords
import index.IndexInfo;;

/**
 * To generate queries to perform binary text classification using GA string of
 * integer pairs
 *
 * @author Laurie
 */

public class SpanFirstG extends Problem implements SimpleProblemForm, HitCounts {

	private String[] wordArray;
	BooleanQuery query;

	public void setup(final EvolutionState state, final Parameter base) {

		super.setup(state, base);

		println("Total docs for cat  " + IndexInfo.instance.getCatnumberAsString() + " "
				+ IndexInfo.instance.totalTrainDocsInCat + " Total test docs for cat "
				+ IndexInfo.instance.totalTestDocsInCat);

		ImportantWords iw = new ImportantWords();
		wordArray = iw.getF1WordList(false, true);
	}


	public void evaluate(final EvolutionState state, final Individual ind, final int subpopulation,
			final int threadnum) {

		if (ind.evaluated)
			return;

		BooleanQuery.Builder bqb = new BooleanQuery.Builder();

		GAFit fitness = (GAFit) ind.fitness;

		List words=[]
		int tree=0

		IntegerVectorIndividual intVectorIndividual = (IntegerVectorIndividual) ind;

		int wordInd0, wordInd1;

		for (int i = 0; i < (intVectorIndividual.genome.length - 1); i = i + 2) {

			if (intVectorIndividual.genome[i] >= wordArray.length || intVectorIndividual.genome[i] < 0
			|| intVectorIndividual.genome[i + 1] >= wordArray.length || intVectorIndividual.genome[i + 1] < 0
			|| intVectorIndividual.genome[i] == intVectorIndividual.genome[i + 1])
				continue;
			else {
				wordInd0 = intVectorIndividual.genome[i];				
			}
			
			String word = wordArray[wordInd0];

			SpanFirstQuery sfq = new SpanFirstQuery(new SpanTermQuery(new Term(
					IndexInfo.FIELD_CONTENTS, word)),
					intVectorIndividual.genome[i + 1]);

			bqb.add(sfq, BooleanClause.Occur.SHOULD);
		}


		query = bqb.build();

		IndexSearcher searcher = IndexInfo.instance.indexSearcher;
		int positiveMatch = getPositiveMatch(searcher, query)
		int negativeMatch = getNegativeMatch(searcher,query)

		def F1train = Effectiveness.f1(positiveMatch, negativeMatch, IndexInfo.instance.totalTrainDocsInCat);

		fitness.setTrainValues(positiveMatch, negativeMatch);
		fitness.setF1Train(F1train);
		fitness.setQuery(query);

		def rawfitness = F1train	
		((SimpleFitness) intVectorIndividual.fitness).setFitness(state, rawfitness, false);

		ind.evaluated = true;
	}
}
