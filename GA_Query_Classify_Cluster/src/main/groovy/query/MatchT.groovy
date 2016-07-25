package query;

import lucene.IndexInfo

import org.apache.lucene.search.BooleanClause
import org.apache.lucene.search.BooleanQuery
import org.apache.lucene.search.IndexSearcher
import org.apache.lucene.search.Query
import org.apache.lucene.search.TotalHitCountCollector

trait MatchT {

	public double getPositiveMatch(IndexSearcher searcher, Query q) {
		
		TotalHitCountCollector collector = new TotalHitCountCollector();
		BooleanQuery.Builder  bqb = new BooleanQuery.Builder();	
		bqb = new BooleanQuery.Builder()
		bqb.add(q, BooleanClause.Occur.MUST)
		bqb.add(IndexInfo.instance.catTrainBQ, BooleanClause.Occur.FILTER)
		searcher.search(bqb.build(), collector);
		return collector.getTotalHits();
	}

	public double getNegativeMatch(IndexSearcher searcher, Query q) {
		
		TotalHitCountCollector collector = new TotalHitCountCollector();
		BooleanQuery.Builder bqb = new BooleanQuery.Builder()
		bqb.add(q, BooleanClause.Occur.MUST)
		bqb.add(IndexInfo.instance.othersTrainBQ, BooleanClause.Occur.FILTER)
		searcher.search(bqb.build(), collector);
		return collector.getTotalHits();	
	}
}