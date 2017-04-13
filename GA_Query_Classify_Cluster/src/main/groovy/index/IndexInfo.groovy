package index

import java.nio.file.Path
import java.nio.file.Paths

import org.apache.lucene.index.DirectoryReader
import org.apache.lucene.index.IndexReader
import org.apache.lucene.index.Term
import org.apache.lucene.search.BooleanClause
import org.apache.lucene.search.BooleanQuery
import org.apache.lucene.search.IndexSearcher
import org.apache.lucene.search.Query
import org.apache.lucene.search.TermQuery
import org.apache.lucene.search.TotalHitCountCollector
import org.apache.lucene.store.Directory
import org.apache.lucene.store.FSDirectory

/**
 * Singleton class to store index information.
 * Set the path to the lucene index here
 */

@Singleton
class IndexInfo {

	// Lucene field names
	public static final String FIELD_CATEGORY_NAME = 'category',
	                           FIELD_CONTENTS = 'contents', 
							   FIELD_PATH = 'path',
							   FIELD_TEST_TRAIN = 'test_train',
							   FIELD_CATEGORY_NUMBER = 'categoryNumber';
	public static final int NUMBER_OF_CLUSTERS =  3 , NUMBER_OF_CATEGORIES = 10

	String 	pathToIndex =	
    'indexes/r10L6' 
	// 'indexes/crisis3FireBombFloodL6'
	// 'indexes/classic4_500L6'
//	 'indexes/20NG5WindowsmiscForsaleHockeySpaceChristianL6'
//	 'indexes/20NG5WindowsmiscForsaleHockeySpaceChristianL6Stem'	 
	// 'indexes/20NG3SpaceHockeyChristianL6Stem'
   //  'indexes/20NG3SpaceHockeyChristianL6'

	IndexReader indexReader
	IndexSearcher indexSearcher
	String categoryNumber='0', categoryName='cru';
	Query catTrainBQ, othersTrainBQ, catTestBQ, othersTestBQ;
	int totalTrainDocsInCat, totalTestDocsInCat, totalOthersTrainDocs, totalTestDocs;

	TermQuery trainQ = new TermQuery(new Term(
	FIELD_TEST_TRAIN, 'train'));
	TermQuery testQ = new TermQuery(new Term(
	FIELD_TEST_TRAIN, 'test'));

	TermQuery catQ 	= new TermQuery(new Term(FIELD_CATEGORY_NUMBER,
	categoryNumber))
	
	//get hits for a particular query using filter (e.g. a particular category)
	public int getQueryHitsWithFilter(IndexSearcher searcher, Query filter, Query q ) {
		
		TotalHitCountCollector collector = new TotalHitCountCollector();
		BooleanQuery.Builder  bqb = new BooleanQuery.Builder();
		bqb.add(q, BooleanClause.Occur.MUST)
		bqb.add(filter, BooleanClause.Occur.FILTER)
		searcher.search(bqb.build(), collector);
		return collector.getTotalHits();
	}
	

	public void setIndex()  {
		catQ = new TermQuery(new Term(FIELD_CATEGORY_NUMBER,
				categoryNumber));
		println "Index info catQ: $catQ"

		Path path = Paths.get(pathToIndex)
		Directory directory = FSDirectory.open(path)
		indexReader = DirectoryReader.open(directory)
		indexSearcher = new IndexSearcher(indexReader);

		BooleanQuery.Builder bqb = new BooleanQuery.Builder()
		bqb.add(catQ, BooleanClause.Occur.MUST)
		bqb.add(trainQ, BooleanClause.Occur.MUST)
		catTrainBQ = bqb.build();

		bqb = new BooleanQuery.Builder()
		bqb.add(catQ, BooleanClause.Occur.MUST)
		bqb.add(testQ, BooleanClause.Occur.MUST)
		catTestBQ = bqb.build();

		bqb = new BooleanQuery.Builder()
		bqb.add(catQ, BooleanClause.Occur.MUST_NOT)
		bqb.add(trainQ, BooleanClause.Occur.MUST)
		othersTrainBQ = bqb.build();

		bqb = new BooleanQuery.Builder()
		bqb.add(catQ, BooleanClause.Occur.MUST_NOT)
		bqb.add(testQ, BooleanClause.Occur.MUST)
		othersTestBQ = bqb.build();

		TotalHitCountCollector collector  = new TotalHitCountCollector();
		indexSearcher.search(catTrainBQ, collector);
		totalTrainDocsInCat = collector.getTotalHits();

		collector  = new TotalHitCountCollector();
		indexSearcher.search(catTestBQ, collector);
		totalTestDocsInCat = collector.getTotalHits();

		collector  = new TotalHitCountCollector();
		indexSearcher.search(othersTrainBQ, collector);
		totalOthersTrainDocs = collector.getTotalHits();

		collector  = new TotalHitCountCollector();
		indexSearcher.search(trainQ, collector);
		int totalTrain = collector.getTotalHits();

		collector  = new TotalHitCountCollector();
		indexSearcher.search(testQ, collector);
		totalTestDocs = collector.getTotalHits();

		println "Total train docs: $totalTrain"
		println "IndexInfo   CategoryNumber: $categoryNumber Total train in cat: $totalTrainDocsInCat  Total others tain: $totalOthersTrainDocs   Total test in cat : $totalTestDocsInCat  "
	}	
}