package cluster;

import index.ImportantWords
import index.IndexInfo
import org.apache.lucene.index.Term
import org.apache.lucene.search.BooleanClause
import org.apache.lucene.search.BooleanQuery
import org.apache.lucene.search.IndexSearcher
import org.apache.lucene.search.Query
import org.apache.lucene.search.ScoreDoc
import org.apache.lucene.search.TermQuery
import org.apache.lucene.search.TopDocs

import ec.EvolutionState
import ec.Individual
import ec.Problem
import ec.simple.SimpleFitness
import ec.simple.SimpleProblemForm
import ec.util.Parameter
import ec.vector.IntegerVectorIndividual

/**
 * To generate sets of queries for clustering
 */

public class ClusterQuery extends Problem implements SimpleProblemForm {

	private IndexSearcher searcher = IndexInfo.instance.indexSearcher;
	private final int coreClusterSize=20
	private QueryListFromChromosome queryListFromChromosome

	enum QueryType {
		OR, ORNOT, AND, ALLNOT, ORNOTEVOLVED
	}
	final QueryType queryType = QueryType.OR

	public void setup(final EvolutionState state, final Parameter base) {

		super.setup(state, base);
		println "Total docs for ClusterQuery.groovy   " + IndexInfo.instance.indexReader.maxDoc()
		queryListFromChromosome = new QueryListFromChromosome()
	}

	public void evaluate(final EvolutionState state, final Individual ind, final int subpopulation,
			final int threadnum) {

		if (ind.evaluated)
			return;

		ClusterFit fitness = (ClusterFit) ind.fitness;
		IntegerVectorIndividual intVectorIndividual = (IntegerVectorIndividual) ind;

		//list of lucene Boolean Query Builders
		def bqbList
		int duplicateCount = 0, lowSubqHits=0
		switch (queryType) {
			case QueryType.OR :
				bqbList = queryListFromChromosome.getORQueryList(intVectorIndividual)
				break;
			case QueryType.AND :
				(bqbList, duplicateCount, lowSubqHits) = queryListFromChromosome.getANDQL(intVectorIndividual)
				break;
			case QueryType.ORNOT :
				(bqbList, duplicateCount) = queryListFromChromosome.getORNOTQL(intVectorIndividual)
				break;
			case QueryType.ALLNOT :
				bqbList = queryListFromChromosome.getALLNOTQL(intVectorIndividual)
				break;
			case QueryType.ORNOTEVOLVED :
				bqbList = queryListFromChromosome.getORNOTfromEvolvedList(intVectorIndividual)
				break;
		}
		assert bqbList.size == IndexInfo.NUMBER_OF_CLUSTERS
		final int hitsPerPage = IndexInfo.instance.indexReader.maxDoc()

		fitness.positiveScoreTotal=0
		fitness.negativeScoreTotal=0
		fitness.positiveHits=0
		fitness.negativeHits=0
		fitness.lowSubqHits=lowSubqHits
		fitness.coreClusterPenalty=0
		fitness.totalHits=0
		fitness.missedDocs =0
		fitness.zeroHitsCount =0
		fitness.duplicateCount = duplicateCount

		def qMap = [:]
		def allHits = [] as Set

		bqbList.eachWithIndex {bqb, index ->

			def q = bqb.build()
			def otherdocIdSet= [] as Set
			def otherQueries = bqbList - bqb

			BooleanQuery.Builder bqbOthers = new BooleanQuery.Builder();
			otherQueries.each {obqb ->
				bqbOthers.add(obqb.build(),  BooleanClause.Occur.SHOULD)
			}
			Query otherBQ = bqbOthers.build()

			TopDocs otherTopDocs = searcher.search(otherBQ, hitsPerPage)
			ScoreDoc[] hitsOthers = otherTopDocs.scoreDocs;
			hitsOthers.each {otherHit -> otherdocIdSet << otherHit.doc }

			TopDocs docs = searcher.search(q, hitsPerPage)
			ScoreDoc[] hits = docs.scoreDocs;
			qMap.put(q,hits.size())

			if (hits.size()<1)   fitness.zeroHitsCount ++

			hits.eachWithIndex {d, position ->
				allHits << d.doc

				if (otherdocIdSet.contains(d.doc)){
					fitness.negativeHits++;
					fitness.negativeScoreTotal += d.score
					if (position < coreClusterSize ){
						//heavy penalty
						//def reverseRank = coreClusterSize - position
						//fitness.coreClusterPenalty +=reverseRank
						fitness.coreClusterPenalty++
					}
				}
				else {
					fitness.positiveHits++
					fitness.positiveScoreTotal +=d.score
				}
			}
		}

		
		fitness.queryMap = qMap.asImmutable()
		fitness.scoreOnly = fitness.positiveScoreTotal - fitness.negativeScoreTotal
		fitness.totalHits = allHits.size()
		fitness.fraction = fitness.totalHits / IndexInfo.instance.indexReader.maxDoc()
		fitness.missedDocs = IndexInfo.instance.indexReader.maxDoc() - allHits.size()

		//fitness must be positive for ECJ - most runs start with large negative score
		def final minScore = 1000
		fitness.scorePlus1000 = (fitness.scoreOnly < -minScore) ? 0 : fitness.scoreOnly + minScore

		def negIndicators =
				//major penalty for query returning nothing or empty query
				(fitness.zeroHitsCount * 100) + fitness.coreClusterPenalty + fitness.duplicateCount + fitness.lowSubqHits + 1;

		fitness.baseFitness = fitness.scorePlus1000 / negIndicators

		//rawfitness used by ECJ for evaluation 
		def rawfitness = fitness.baseFitness

		// to improve improve recall?
		//fitness.baseFitness * fitness.fraction

		//baseFitness * (1/(Math.log(missedDocs)))
		//baseFitness * (1/(Math.pow(1.01,missedDocs)))

		//force positive
		//		if (totalScore> 0) {
		//			baseFitness = scoreOnly / negIndicators
		//		} else
		//			baseFitness =
		//					(posScore + 1) / (negScore + coreClusterPen + duplicateCount + 1)


		((SimpleFitness) intVectorIndividual.fitness).setFitness(state, rawfitness, false)
		ind.evaluated = true;
	}
}