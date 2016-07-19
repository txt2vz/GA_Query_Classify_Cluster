package query;

import lucene.ImportantWords
import lucene.IndexInfoStaticG

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
import ecj.ClusterFit

/**
 * To generate AND queries for clustering
 *
 * @author Laurie
 */

public class Cluster extends Problem implements CreateQueriesT, SimpleProblemForm {

	private String[] wordArray;	
	final int NUMBER_OF_CLUSTERS =  IndexInfoStaticG.NUMBER_OF_CLUSTERS
	final int coreClusterSize=20

	public void setup(final EvolutionState state, final Parameter base) {

		super.setup(state, base);
		println "Total docs for ClusterAND   " + IndexInfoStaticG.instance.reader.maxDoc()
		ImportantWords iw = new ImportantWords();
		wordArray =	iw.getTFIDFWordList()
	}

	public void evaluate(final EvolutionState state, final Individual ind, final int subpopulation,
			final int threadnum) {

		if (ind.evaluated)
			return;

		IndexSearcher searcher = IndexInfoStaticG.instance.indexSearcher;
		ClusterFit fitness = (ClusterFit) ind.fitness;
		IntegerVectorIndividual intVectorIndividual = (IntegerVectorIndividual) ind;	
			
		def bqbList = getORQL(wordArray, intVectorIndividual, NUMBER_OF_CLUSTERS)
		           // getANDQL(wordArray, intVectorIndividual, NUMBER_OF_CLUSTERS)

		final int hitsPerPage = 3000;
		def negHitsTotal=0
		def posHitsTotal=0
		def posScore =0
		def negScore =0
		def coreClusterPen = 0

		def qMap = [:]

		bqbList.eachWithIndex {bqb, index ->

			def q = bqb.build()
			def  otherdocIdSet=[] as Set
			def otherQueries = bqbList - bqb

			BooleanQuery.Builder bqbOthers = new BooleanQuery.Builder();
			otherQueries.each {obqb ->
				bqbOthers.add(obqb.build(),  BooleanClause.Occur.SHOULD)
			}
			Query otherBQ = bqbOthers.build()

			TopDocs otherTopDocs = searcher.search(otherBQ, hitsPerPage)
			ScoreDoc[] hitsOthers = otherTopDocs.scoreDocs;

			//store docIds of other hits
			hitsOthers.each {otherHit -> otherdocIdSet << otherHit.doc }

			TopDocs docs = searcher.search(q, hitsPerPage);

			ScoreDoc[] hits = docs.scoreDocs;
			qMap.put(q.toString(IndexInfoStaticG.FIELD_CONTENTS),hits.size())

			hits.eachWithIndex {d, position ->

				if (otherdocIdSet.contains(d.doc)){
					negHitsTotal++;
					if (position < coreClusterSize ){
						def reverseRank = coreClusterSize - position
						coreClusterPen +=reverseRank
					}
					negScore += d.score * 2
				}
				else {
					posHitsTotal++
					posScore +=d.score
				}
			}
		}

		def negIndicators =  (graphPen+1) *
				// (treePen+1) *
				(noHitsCount+1) * (noHitsCount+1) * (duplicateCount+1) * (coreClusterPen +1) * (coreClusterPen +1)
		def  rawfitness = (posScore - ((negScore+1) * (negIndicators ))) / (negIndicators)
		
		if (rawfitness < -200) rawfitness=0 else rawfitness =rawfitness +200 

		fitness.graphPenalty=graphPen
		fitness.queryMap = qMap.asImmutable()
		fitness.negativeScore = negScore
		fitness.positiveScore = posScore
		fitness.negHits = negHitsTotal
		fitness.posHits = posHitsTotal
		fitness.duplicateCount=duplicateCount
		fitness.coreClusterPenalty= coreClusterPen
		fitness.treePenalty=treePen
		fitness.noHitsCount=noHitsCount

		((SimpleFitness) intVectorIndividual.fitness).setFitness(state, rawfitness, false);

		ind.evaluated = true;
	}
}