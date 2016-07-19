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
import groovy.transform.CompileStatic

/**
 * To generate queries to perform binary text classification using GA string of
 * integer pairs
 *
 * @author Laurie
 */

public class AND_OR_Viz extends Problem implements SimpleProblemForm, MatchT {

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

			if (i>0) {
				if (!((words.contains(word0) && !words.contains(word1)) || (words.contains(word1) && !words.contains(word0)))) tree++;
			}

			words.add(word0)
			words.add(word1)

			BooleanQuery.Builder subbqb = new BooleanQuery.Builder();
			subbqb.add(new TermQuery(new Term(IndexInfoStaticG.FIELD_CONTENTS, word0)), BooleanClause.Occur.MUST);
			subbqb.add(new TermQuery(new Term(IndexInfoStaticG.FIELD_CONTENTS, word1)), BooleanClause.Occur.MUST);

			BooleanQuery subq = subbqb.build();
			bqb.add(subq, BooleanClause.Occur.SHOULD);
		}

		query = bqb.build();
		
		IndexSearcher searcher = IndexInfoStaticG.instance.indexSearcher;
		int positiveMatch = getPositiveMatch(searcher, query)
		int negativeMatch = getNegativeMatch(searcher,query)

		def F1train = ClassifyQuery.f1(positiveMatch, negativeMatch, IndexInfoStaticG.instance.totalTrainDocsInCat);

		fitness.setTrainValues(positiveMatch, negativeMatch);
		fitness.setF1Train(F1train);
		fitness.setQuery(query);


		//def rawfitness;
		//if (tree > 0) rawfitness = Integer.MAX_VALUE
		//else
		//rawfitness =  F1train * words.size()
		def l = intVectorIndividual.genome.length

		def rawfitness  =F1train / (tree +1)
		// (F1train * (words.size()/l)) / (tree +1) //* (tree +1) * (tree +1)) ;
		// +length
		// +duplicates
		// +tree

		((SimpleFitness) intVectorIndividual.fitness).setFitness(state, rawfitness, false);

		ind.evaluated = true;
	}
}
