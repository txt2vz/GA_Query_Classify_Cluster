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

	def totalPositiveScore=0
	def totalNegativeScore=0;
	def totalPosHits=0
	def totalNegHits=0
	def duplicateCount=0;
	def noHitsCount=0;
	
	public String getQueryString(){
		return query.toString(IndexInfo.FIELD_CONTENTS)
	}

	public String getQueryMinimal() {
		return QueryReadable.getQueryMinimal(query);
	}

	public String getQueryJSONForViz(){
		return QueryReadable.getQueryJSONForViz(query);
	}

//
//	public void setNumberOfTerms(int numberOfTerms) {
//		this.numberOfTerms = numberOfTerms;
//	}
//
//	public void setPositiveMatchTest(int positiveMatchTest) {
//		this.positiveMatchTest = positiveMatchTest;
//	}
//
//	public int getPositiveMatchTest() {
//		return positiveMatchTest;
//	}
//
//	public void setNegativeMatchTest(int negativeMatchTest) {
//		this.negativeMatchTest = negativeMatchTest;
//	}
//
//	public int getNegativeMatchTest() {
//		return negativeMatchTest;
//	}
//
//	public int getNumberOfTerms() {
//		return numberOfTerms;
//	}

	public String fitnessToStringForHumans() {
		return  "F1train: $f1train  fitness: " + this.fitness();
	}

	public String toString(int gen) {
		return "Gen: $gen  F1: $f1train  Positive Match: $positiveMatchTrain Negative Match: $negativeMatchTrain "
		+ " Total positive Docs: " + IndexInfo.instance.totalTrainDocsInCat
		+ '\n' + "QueryString: " + query.toString(IndexInfo.FIELD_CONTENTS) + '\n';
	}
}