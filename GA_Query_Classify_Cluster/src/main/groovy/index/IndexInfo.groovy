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

import query.*

/**
 * Singleton class to store index information.
 * Set the path to the lucene index here
 */
@Singleton
class IndexInfo {

	// Lucene field names
	public static final String FIELD_CATEGORY_NAME = "categoryName";
	public static final String FIELD_CONTENTS = "contents";
	public static final String FIELD_PATH = "path";
	public static final String FIELD_TEST_TRAIN = "test_train";
	public static final String FIELD_CATEGORY_NUMBER = "categoryNumber";

	int NUMBER_OF_CLUSTERS =  3

	TermQuery trainQ = new TermQuery(new Term(
	FIELD_TEST_TRAIN, "train"));

	TermQuery testQ = new TermQuery(new Term(
	FIELD_TEST_TRAIN, "test"));

	IndexReader indexReader
	IndexSearcher indexSearcher

	String categoryNumber="0", categoryName="cru";

	TermQuery catQ 	= new TermQuery(new Term(FIELD_CATEGORY_NUMBER,
	categoryNumber))

	Query catTrainBQ, othersTrainBQ, catTestBQ, othersTestBQ;

	int totalTrainDocsInCat, totalTestDocsInCat, totalOthersTrainDocs, totalTestDocs;

	public void setCatNumber(final int cn) {
		categoryNumber = String.valueOf(cn);
		println "Category number set: $categoryNumber"
	}
	public void setCatName(final String cn) {
		categoryName = cn;
	}

	public  String getCatnumberAsString() {
		return String.valueOf(categoryNumber);
	}

	public void setFilters()  {
		catQ = new TermQuery(new Term(FIELD_CATEGORY_NUMBER,
				categoryNumber));
		println "index infor catQ $catQ"

		String pathToIndex = //"indexes/r10"
				"indexes/20NG3SpaceHockeyChristian"

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
		println "CategoryNumber $categoryNumber Total train in cat: $totalTrainDocsInCat  Total others tain: $totalOthersTrainDocs   Total test in cat : $totalTestDocsInCat  "
	}
}