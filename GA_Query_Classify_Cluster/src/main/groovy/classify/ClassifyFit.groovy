package classify;

import org.apache.lucene.search.Query

import ec.simple.SimpleFitness
import index.IndexInfo;

/**
 * Store information about classification classify.query and test/train values
 * 
 * @author Laurie
 * 
 */

public class ClassifyFit extends SimpleFitness {

	private double f1train, f1test, BEPTest, tree;
	private int positiveMatchTrain, negativeMatchTrain;
	private int positiveMatchTest, negativeMatchTest, numberOfTerms = 0;
	private Query query;

	int totalPositiveScore=0
	int totalNegativeScore=0;
	int totalPosHits=0
	int totalNegHits=0
	int duplicateCount=0;
	int noHitsCount=0;
	
	public String getQueryString(){
		return query.toString(IndexInfo.FIELD_CONTENTS)
	}

	public String getQueryMinimal() {
		return QueryReadable.getQueryMinimal(query);
	}

	public String getQueryJSONForViz(){
		return QueryReadable.getQueryJSONForViz(query);
	}

	public String fitnessToStringForHumans() {
		return  "F1train: $f1train  fitness: " + this.fitness();
	}

	public String toString(int gen) {
		return "Gen: $gen  F1: $f1train  Positive Match: $positiveMatchTrain Negative Match: $negativeMatchTrain "
		+ " Total positive Docs: " + IndexInfo.instance.totalTrainDocsInCat
		+ '\n' + "QueryString: " + query.toString(IndexInfo.FIELD_CONTENTS) + '\n';
	}
}