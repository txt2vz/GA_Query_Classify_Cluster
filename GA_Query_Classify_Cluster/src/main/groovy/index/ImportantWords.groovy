package index

import org.apache.lucene.index.IndexReader
import org.apache.lucene.index.MultiFields
import org.apache.lucene.index.Term
import org.apache.lucene.index.Terms
import org.apache.lucene.index.TermsEnum
import org.apache.lucene.search.BooleanClause
import org.apache.lucene.search.BooleanQuery
import org.apache.lucene.search.IndexSearcher
import org.apache.lucene.search.Query
import org.apache.lucene.search.TermQuery
import org.apache.lucene.search.TotalHitCountCollector
import org.apache.lucene.search.spans.SpanFirstQuery
import org.apache.lucene.search.spans.SpanTermQuery
import org.apache.lucene.util.BytesRef

import query.*
import classify.ClassifyQuery

/**
 * GAs return words by selecting form word lists provided by this
 * class. The words should be as far as possible in order of their likely
 * usefulness in classify.query building
 * 
 * @author Laurie 
 */

public class ImportantWords {

	public final static int SPAN_FIRST_MAX_END = 300;
	private final static int MAX_WORDLIST_SIZE = 300;

	private final IndexSearcher indexSearcher = IndexInfo.instance.indexSearcher;
	private final IndexReader indexReader = indexSearcher.getIndexReader()
	private Terms terms = MultiFields.getTerms(indexReader, IndexInfo.FIELD_CONTENTS)
	private TermsEnum termsEnum = terms.iterator();
	private int maxDoc = indexReader.maxDoc();
	private Set<String> stopSet = StopLists.getStopSet()

	public static void main(String[] args){
		IndexInfo.instance.setCategoryName("gra")
		IndexInfo.instance.setFilters()
		def iw = new ImportantWords()
		iw.getF1WordList(false, true)
	}

	public ImportantWords() throws IOException {
	}

	/**
	 * create a set of words based on F1 measure of the word as a classify.query
	 */
	public String[] getF1WordList(boolean spanFirstQ, boolean positiveList)
	throws IOException{

		println "Important words terms.getDocCount: ${terms.getDocCount()}"
		println "Important words terms.size ${terms.size()}"

		BytesRef text;
		termsEnum = terms.iterator();

		def wordMap = [:]

		while((text = termsEnum.next()) != null) {

			def word = text.utf8ToString()

			final Term t = new Term(IndexInfo.FIELD_CONTENTS, word);

			if (word=="") continue

				char c = word.charAt(0)

			if (indexSearcher.getIndexReader().docFreq(t) < 3
			//|| StopSet.stopSet.contains(t.text())
			|| stopSet.contains(t.text())
			//  ||t.text().contains("'")
			//  ||t.text().contains(".")
			//||!c.isLetter())
			//|| stopSet.contains(t.text()))
			)
				continue;

			Query q;
			if (spanFirstQ){
				q = new SpanFirstQuery(new SpanTermQuery(t),
						SPAN_FIRST_MAX_END);
			}
			else
			{
				q = new TermQuery(t);
			}

			//Filter filter0, filter1;
			BooleanQuery filter0, filter1;
			int totalDocs;

			if (positiveList) {
				filter0 = IndexInfo.instance.catTrainBQ;
				filter1 = IndexInfo.instance.othersTrainBQ;
				totalDocs = IndexInfo.instance.totalTrainDocsInCat;
			} else {
				filter0 = IndexInfo.instance.othersTrainBQ;
				filter1 = IndexInfo.instance.catTrainBQ;
				totalDocs = IndexInfo.instance.totalOthersTrainDocs;
			}

			TotalHitCountCollector collector  = new TotalHitCountCollector();
			BooleanQuery.Builder bqb = new BooleanQuery.Builder()
			bqb.add(q, BooleanClause.Occur.MUST)
			bqb.add(filter0, BooleanClause.Occur.FILTER)
			indexSearcher.search(bqb.build(), collector);
			final int positiveHits = collector.getTotalHits();

			collector  = new TotalHitCountCollector();
			bqb = new BooleanQuery.Builder()
			bqb.add(q, BooleanClause.Occur.MUST)
			bqb.add(filter1, BooleanClause.Occur.FILTER)
			indexSearcher.search(bqb.build(), collector);
			final int negativeHits = collector.getTotalHits();

			def F1 = ClassifyQuery.f1(positiveHits, negativeHits,
					totalDocs);

			if (F1 > 0.02) {
				wordMap += [(word): F1]
			}
		}

		wordMap= wordMap.sort{a, b -> b.value <=> a.value}

		List wordList = wordMap.keySet().toList().take(MAX_WORDLIST_SIZE)
		println "map size: ${wordMap.size()}  List size is ${wordList.size()}  list is $wordList"

		return wordList.toArray();
	}
}