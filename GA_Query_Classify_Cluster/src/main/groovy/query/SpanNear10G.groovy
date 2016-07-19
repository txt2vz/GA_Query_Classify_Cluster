package query;

import lucene.ImportantWords
import lucene.IndexInfoStaticG

import org.apache.lucene.index.Term
import org.apache.lucene.search.BooleanClause
import org.apache.lucene.search.BooleanQuery
import org.apache.lucene.search.IndexSearcher
import org.apache.lucene.search.spans.SpanNearQuery
import org.apache.lucene.search.spans.SpanQuery
import org.apache.lucene.search.spans.SpanTermQuery

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
 * integer pairs
 *
 * @author Laurie
 */

public class SpanNear10G extends Problem implements SimpleProblemForm, MatchT {

	private String[] wordArray;
	BooleanQuery query;
	private final static int WORD_DISTANCE=10;

	public void setup(final EvolutionState state, final Parameter base) {

		super.setup(state, base);

		println("Total docs for cat  " + IndexInfoStaticG.instance.getCatnumberAsString() + " "
				+ IndexInfoStaticG.instance.totalTrainDocsInCat + " Total test docs for cat "
				+ IndexInfoStaticG.instance.totalTestDocsInCat);

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
				wordInd1 = intVectorIndividual.genome[i + 1];
			}
			String word0 = wordArray[wordInd0];
			String word1 = wordArray[wordInd1];
	
			SpanQuery snw0   = new SpanTermQuery(new Term(IndexInfoStaticG.FIELD_CONTENTS, word0));
			SpanQuery snw1   = new SpanTermQuery(new Term(IndexInfoStaticG.FIELD_CONTENTS, word1));
			
			SpanQuery spanN = new SpanNearQuery([snw0, snw1] as SpanQuery[], WORD_DISTANCE,false)
			
			//new SpanNearQuery(new SpanQuery[] {snw0,snw1}, WORD_DISTANCE, false);
			
			bqb.add(spanN, BooleanClause.Occur.SHOULD);
		}


		query = bqb.build();

		IndexSearcher searcher = IndexInfoStaticG.instance.indexSearcher;
		
		//use methods from trait
		int positiveMatch = getPositiveMatch(searcher, query)
		int negativeMatch = getNegativeMatch(searcher,query)

		def F1train = ClassifyQuery.f1(positiveMatch, negativeMatch, IndexInfoStaticG.instance.totalTrainDocsInCat);

		fitness.setTrainValues(positiveMatch, negativeMatch);
		fitness.setF1Train(F1train);
		fitness.setQuery(query);

		def rawfitness = F1train	
		((SimpleFitness) intVectorIndividual.fitness).setFitness(state, rawfitness, false);

		ind.evaluated = true;
	}
}
