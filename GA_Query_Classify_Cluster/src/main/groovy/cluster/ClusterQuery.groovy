package cluster;

import index.ImportantWords
import index.IndexInfo

import org.apache.lucene.search.BooleanClause
import org.apache.lucene.search.BooleanQuery
import org.apache.lucene.search.IndexSearcher
import org.apache.lucene.search.Query
import org.apache.lucene.search.ScoreDoc
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

public class ClusterQuery extends Problem implements CreateQueryTrait, SimpleProblemForm {

	IndexSearcher searcher = IndexInfo.instance.indexSearcher;
	private String[] wordArray;
	private final int coreClusterSize=20

	public void setup(final EvolutionState state, final Parameter base) {

		super.setup(state, base);
		println "Total docs for ClusterQuery.groovy   " + IndexInfo.instance.indexReader.maxDoc()
		ImportantWords iw = new ImportantWords();
		wordArray =	iw.getTFIDFWordList()
	}

	public void evaluate(final EvolutionState state, final Individual ind, final int subpopulation,
			final int threadnum) {

		if (ind.evaluated)
			return;

		ClusterFit fitness = (ClusterFit) ind.fitness;
		IntegerVectorIndividual intVectorIndividual = (IntegerVectorIndividual) ind;

		//list of lucene Boolean Query Builders
		def bqbList =

				//from ClusterQueriesT = trait
			getORQL(wordArray, intVectorIndividual)
	//	getANDQL(wordArray, intVectorIndividual)
		//getORNOTQL(wordArray, intVectorIndividual, IndexInfo.NUMBER_OF_CLUSTERS)

		final int hitsPerPage = 10000;
		def negHitsTotal=0
		def posHitsTotal=0
		def posScore =0
		def negScore =0
		def coreClusterPen = 0
		def emptyPen = 0

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

			//major penalty for query returning nothing
			if (hits.size()<1) emptyPen = emptyPen + 100

			hits.eachWithIndex {d, position ->
				allHits << d.doc

				if (otherdocIdSet.contains(d.doc)){
					negHitsTotal++;
					negScore += d.score
					if (position < coreClusterSize ){
						def reverseRank = coreClusterSize - position
						coreClusterPen +=reverseRank
					}
				}
				else {
					posHitsTotal++
					posScore +=d.score
				}
			}
		}

		def totalScore = posScore - negScore

		//fitness must be positive for ECJ
		def final  minScore = 1000
		def scorePlus = (totalScore < -minScore) ? 0 : totalScore + minScore

		def negIndicators =
				noHitsCount + duplicateCount + emptyPen + coreClusterPen + 1

		def fractionCovered = allHits.size() / IndexInfo.instance.indexReader.maxDoc()
		def missedDocs = IndexInfo.instance.indexReader.maxDoc() - allHits.size()
		///You might want to multiple your fitness function by 1/(number of unclassified documents).
		//(1.1)^{number of words covered by clusters}.

		def baseFitness = scorePlus / negIndicators

//		if (totalScore> 0) {
//			baseFitness = totalScore / negIndicators
//		} else
//			baseFitness =
//					(posScore + 1) / (negScore + coreClusterPen + emptyPen + duplicateCount + 1)

		//may improve recall?
		def rawfitness =	baseFitness * fractionCovered

		//baseFitness * (1/(Math.log(missedDocs)))
		//baseFitness * (1/(Math.pow(1.01,missedDocs)))
		//	baseFitness * (Math.pow(1.01,allHits.size()))

		fitness.baseFitness = baseFitness;
		fitness.missedDocs = missedDocs
		fitness.fraction = fractionCovered
		fitness.totalHits = allHits.size()
		fitness.graphPenalty=graphPen
		fitness.queryMap = qMap.asImmutable()
		fitness.scrPlus = scorePlus
		fitness.negativeScore = negScore
		fitness.positiveScore = posScore
		fitness.negHits = negHitsTotal
		fitness.posHits = posHitsTotal
		fitness.duplicateCount=duplicateCount
		fitness.coreClusterPenalty= coreClusterPen
		fitness.treePenalty=treePen
		fitness.noHitsCount=noHitsCount
		fitness.scoreOnly=totalScore
		fitness.emptyPenalty = emptyPen

		((SimpleFitness) intVectorIndividual.fitness).setFitness(state, rawfitness, false);
		ind.evaluated = true;
	}
}