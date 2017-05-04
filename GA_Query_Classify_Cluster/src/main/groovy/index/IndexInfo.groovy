package index

import java.nio.file.Path
import java.nio.file.Paths
import org.apache.lucene.document.Document
import org.apache.lucene.index.DirectoryReader
import org.apache.lucene.index.IndexReader
import org.apache.lucene.index.Term
import org.apache.lucene.search.BooleanClause
import org.apache.lucene.search.BooleanQuery
import org.apache.lucene.search.IndexSearcher
import org.apache.lucene.search.Query
import org.apache.lucene.search.ScoreDoc
import org.apache.lucene.search.TermQuery
import org.apache.lucene.search.TopScoreDocCollector
import org.apache.lucene.search.TotalHitCountCollector
import org.apache.lucene.store.Directory
import org.apache.lucene.store.FSDirectory

/**
 * Singleton class to store index information.
 * Set the path to the lucene index here
 */

@groovy.transform.TypeChecked
@Singleton
class IndexInfo {

	// Lucene field names
	public static final String FIELD_CATEGORY_NAME = 'category',
	FIELD_CONTENTS = 'contents',
	FIELD_PATH = 'path',
	FIELD_TEST_TRAIN = 'test_train',
	FIELD_CATEGORY_NUMBER = 'categoryNumber';
	
	static final int NUMBER_OF_CLUSTERS =  3 , NUMBER_OF_CATEGORIES = 10
	static IndexReader indexReader
	static IndexSearcher indexSearcher

	final String pathToIndex =
	    'indexes/R10'
	 //     'indexes/NG20'
	//	 'indexes/crisis3FireBombFloodL6'
	// 'indexes/classic4_500L6'
	//	 'indexes/20NG5WindowsmiscForsaleHockeySpaceChristianL6'
	//'indexes/20NG3SpaceHockeyChristianL6'

	String categoryNumber='0', categoryName=''
	Query catTrainBQ, othersTrainBQ, catTestBQ, othersTestBQ;
	int totalTrainDocsInCat, totalTestDocsInCat, totalOthersTrainDocs, totalTestDocs;

	TermQuery trainQ = new TermQuery(new Term(
	FIELD_TEST_TRAIN, 'train'));
	TermQuery testQ = new TermQuery(new Term(
	FIELD_TEST_TRAIN, 'test'));

	TermQuery catQ 	= new TermQuery(new Term(FIELD_CATEGORY_NUMBER,
	categoryNumber))

	//get hits for a particular query using filter (e.g. a particular category)
	@groovy.transform.CompileStatic
	public static int getQueryHitsWithFilter(IndexSearcher searcher, Query filter, Query q ) {
		TotalHitCountCollector collector = new TotalHitCountCollector();
		BooleanQuery.Builder  bqb = new BooleanQuery.Builder();
		bqb.add(q, BooleanClause.Occur.MUST)
		bqb.add(filter, BooleanClause.Occur.FILTER)
		searcher.search(bqb.build(), collector);
		return collector.getTotalHits();
	}
 
	public String getCategoryName (){
		TopScoreDocCollector collector = TopScoreDocCollector.create(1)
		indexSearcher.search(catQ, collector);
		ScoreDoc[] hits = collector.topDocs().scoreDocs
		
		hits.each {ScoreDoc h ->
			Document d = indexSearcher.doc(h.doc)
			categoryName = d.get(FIELD_CATEGORY_NAME)
		}
		return categoryName		
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
		println "IndexInfo CategoryNumber: $categoryNumber Total train in cat: $totalTrainDocsInCat  Total others tain: $totalOthersTrainDocs   Total test in cat : $totalTestDocsInCat  "
	}
}