package cluster

import org.apache.lucene.index.Term
import org.apache.lucene.search.BooleanClause
import org.apache.lucene.search.BooleanQuery
import org.apache.lucene.search.IndexSearcher
import org.apache.lucene.search.MatchAllDocsQuery
import org.apache.lucene.search.Query
import org.apache.lucene.search.ScoreDoc
import org.apache.lucene.search.TermQuery
import org.apache.lucene.search.TopDocs
import org.apache.lucene.search.TotalHitCountCollector
import org.apache.lucene.search.spans.SpanFirstQuery
import org.apache.lucene.search.spans.SpanTermQuery
import ec.vector.IntegerVectorIndividual
import index.ImportantWords
import index.IndexInfo

class EvalQueryList {

	private IndexSearcher searcher = IndexInfo.instance.indexSearcher

	public void cf (ClusterFit fitness, def bqbArray){

		//assert bqbArray.size == IndexInfo.NUMBER_OF_CLUSTERS
		final int hitsPerPage = IndexInfo.instance.indexReader.maxDoc()
		final int coreClusterSize=20

		fitness.positiveScoreTotal=0
		fitness.negativeScoreTotal=0
		fitness.positiveHits=0
		fitness.negativeHits=0
		fitness.lowSubqHits= 0
		fitness.coreClusterPenalty=0
		fitness.totalHits=0
		fitness.missedDocs =0
		fitness.zeroHitsCount =0
		fitness.duplicateCount = 0

		def qMap = [:]
		def allHits = [] as Set

		//		if (bqbArray.size() != IndexInfo.NUMBER_OF_CLUSTERS) {
		//			println "bqblist size error"
		//			fitness.isDummy = true
		//		}

		bqbArray.eachWithIndex {bqb, index ->

			def q = bqb.build()

			if ( q.toString(IndexInfo.FIELD_CONTENTS).contains("DummyXX") || q==null || q.toString(IndexInfo.FIELD_CONTENTS) == '' ){
				fitness.isDummy = true;
			}

			def otherdocIdSet= [] as Set
			def otherQueries = bqbArray - bqb

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

		if (fitness.queryMap.size() != IndexInfo.NUMBER_OF_CLUSTERS) {
			fitness.isDummy = true
		}
		fitness.scoreOnly = fitness.positiveScoreTotal - fitness.negativeScoreTotal
		fitness.totalHits = allHits.size()
		fitness.fraction = fitness.totalHits / IndexInfo.instance.indexReader.maxDoc()
		fitness.missedDocs = IndexInfo.instance.indexReader.maxDoc() - allHits.size()
		//return fitness
	}
}