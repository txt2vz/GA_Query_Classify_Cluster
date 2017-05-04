package cluster

import org.apache.lucene.document.Document
import org.apache.lucene.search.BooleanClause
import org.apache.lucene.search.BooleanQuery
import org.apache.lucene.search.IndexSearcher
import org.apache.lucene.search.Query
import org.apache.lucene.search.ScoreDoc
import org.apache.lucene.search.TopDocs

import index.IndexInfo

@groovy.transform.CompileStatic
@groovy.transform.TypeChecked
class EvalQueryList {

	private static final IndexSearcher searcher = IndexInfo.indexSearcher
	private static final int hitsPerPage = IndexInfo.indexReader.maxDoc()
	private static final int coreClusterSize=20


	public void cf (ClusterFit fitness, List <BooleanQuery.Builder> bqbArray, boolean gp){

		assert bqbArray.size() == IndexInfo.NUMBER_OF_CLUSTERS

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

		bqbArray.eachWithIndex {BooleanQuery.Builder bqb, index ->

			Query q = bqb.build()

			if (gp){
				if ( q.toString(IndexInfo.FIELD_CONTENTS).contains("DummyXX") || q==null || q.toString(IndexInfo.FIELD_CONTENTS) == '' ){
					fitness.isDummy = true;
				}
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
			hitsOthers.each {ScoreDoc otherHit -> otherdocIdSet << otherHit.doc }

			TopDocs docs = searcher.search(q, hitsPerPage)
			ScoreDoc[] hits = docs.scoreDocs;
			qMap.put(q,hits.size())

			if (hits.size()<1)   fitness.zeroHitsCount ++

			hits.eachWithIndex {ScoreDoc d, int position ->
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

		if (gp && fitness.queryMap.size() != IndexInfo.NUMBER_OF_CLUSTERS) {
			fitness.emptyQueries = true
		}
		fitness.scoreOnly = fitness.positiveScoreTotal - fitness.negativeScoreTotal
		fitness.totalHits = allHits.size()
		fitness.fraction = fitness.totalHits / IndexInfo.indexReader.maxDoc()   //IndexInfo.instance.indexReader.maxDoc()
		fitness.missedDocs = IndexInfo.indexReader.maxDoc()  - allHits.size()   //  IndexInfo.instance.indexReader.maxDoc() - allHits.size()
	}
}