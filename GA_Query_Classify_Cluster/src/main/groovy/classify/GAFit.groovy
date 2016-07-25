package classify;

import org.apache.lucene.search.Query

import ec.simple.SimpleFitness
import index.IndexInfo;

/**
 * Store information about classification query and test/train values
 * 
 * @author Laurie
 * 
 */

public class GAFit extends SimpleFitness {

	private double f1train, f1Test, BEPTest, tree;
	private int positiveMatchTrain, negativeMatchTrain;
	private int positiveMatchTest, negativeMatchTest, numberOfTerms = 0;
	private Query query;

	def qMap = [:]
	def qList

	private int neutralHit = -1;

	public void setBqbList (def bqbList){
		qMap.clear()
		bqbList.eachWithIndex  {q, index ->
			qMap.put(index, q.build())
			//qSet << q.build()
		}
	}

	public String getQMap(){
		return qMap.toString()
	}

	def totalPositiveScore=0
	def totalNegativeScore=0;
	def totalPosHits=0
	def totalNegHits=0
	def duplicateCount=0;
	def noHitsCount=0;

	public void setQuery(Query q) {

		query = q;
	}

	public Query getQuery() {
		return query;
	}

	public String getQueryString(){
		return query.toString(IndexInfo.FIELD_CONTENTS)
	}

	public String getQueryMinimal() {
		return QueryReadable.getQueryMinimal(query);
	}

	public String getQueryJSONForViz(){
		return QueryReadable.getQueryJSONForViz(query);
	}

	public void setTrainValues(int posMatchTrain, int negMatchTrain) {
		positiveMatchTrain = posMatchTrain;
		negativeMatchTrain = negMatchTrain;
	}

	public void setTestValues(int posMatchTest, int negMatchTest) {

		setPositiveMatchTest(posMatchTest);
		setNegativeMatchTest(negMatchTest);
	}

	public void setF1Train(final float f1) {
		f1train = f1;
	}

	public double getF1Train() {

		return f1train;
	}

	public void setF1Test(final float f1) {
		f1Test = f1;
	}

	public double getF1Test() {
		return f1Test;
	}

	public void setBEPTest(final float bep) {
		BEPTest = bep;
	}

	public double getBEPTest() {
		return BEPTest;
	}

	public void setNumberOfTerms(int numberOfTerms) {
		this.numberOfTerms = numberOfTerms;
	}

	public void setPositiveMatchTest(int positiveMatchTest) {
		this.positiveMatchTest = positiveMatchTest;
	}

	public int getPositiveMatchTest() {
		return positiveMatchTest;
	}

	public void setNegativeMatchTest(int negativeMatchTest) {
		this.negativeMatchTest = negativeMatchTest;
	}

	public int getNegativeMatchTest() {
		return negativeMatchTest;
	}

	public int getNumberOfTerms() {
		return numberOfTerms;
	}

	public String fitnessToStringForHumans() {
		return  "F1train " + this.f1train +  " fitness: " + this.fitness();
	}

	public String toString(int gen) {
		return "Gen: " + gen +
				" F1: " + f1train + " Positive Match: "
		+ positiveMatchTrain + " Negative Match: " + negativeMatchTrain
		+ " Total positive Docs: "
		+ IndexInfo.instance.totalTrainDocsInCat
		+ '\n' + "QueryString: "
		+ query.toString(IndexInfo.FIELD_CONTENTS) + '\n';
	}
}
