 package index

import org.apache.lucene.index.IndexReader
import org.apache.lucene.index.MultiFields
import org.apache.lucene.index.PostingsEnum
import org.apache.lucene.index.Term
import org.apache.lucene.index.Terms
import org.apache.lucene.index.TermsEnum
import org.apache.lucene.search.BooleanClause
import org.apache.lucene.search.BooleanQuery
import org.apache.lucene.search.DocIdSetIterator
import org.apache.lucene.search.IndexSearcher
import org.apache.lucene.search.Query
import org.apache.lucene.search.TermQuery
import org.apache.lucene.search.TotalHitCountCollector
import org.apache.lucene.search.similarities.ClassicSimilarity
import org.apache.lucene.search.similarities.Similarity
import org.apache.lucene.search.similarities.TFIDFSimilarity
//import org.apache.lucene.search.similarities.DefaultSimilarity
import org.apache.lucene.search.spans.SpanFirstQuery
import org.apache.lucene.search.spans.SpanTermQuery
import org.apache.lucene.util.BytesRef

//import query.*
//import classify.Effectiveness

/**
 * GAs return words by selecting form word lists provided by this
 * class. The words should be as far as possible in order of their likely
 * usefulness in classify.query building
 * 
 * @author Laurie 
 */

public class ImportantWords {

	public final static int SPAN_FIRST_MAX_END = 300;
	private final static int MAX_TERMLIST_SIZE = 300;

	private final IndexSearcher indexSearcher
	private final IndexReader indexReader
	private Terms terms
	private TermsEnum termsEnum
	private int maxDoc
	private Set<String> stopSet

	public static void main(String[] args){
		IndexInfo.instance.setCategoryName("cru")
		IndexInfo.instance.categoryNumber = '3'
		IndexInfo.instance.setIndex()
		def iw = new ImportantWords()
		iw.getF1WordList(false, true)
	//	iw.getTFIDFWordList()
	}

	public ImportantWords() throws IOException {
		indexSearcher = IndexInfo.instance.indexSearcher;
		indexReader = indexSearcher.getIndexReader()
		terms = MultiFields.getTerms(indexReader, IndexInfo.FIELD_CONTENTS)
		termsEnum = terms.iterator();
		maxDoc = indexReader.maxDoc();
		stopSet = StopSet.getStopSetFromFile()
	}

	/**
	 * create a set of words based on F1 measure of the word as a classify.query
	 * create new for each category
	 */
	public TermQuery[] getF1WordList(boolean spanFirstQ, boolean positiveList)
	throws IOException{

		println "Important words terms.getDocCount: ${terms.getDocCount()}"
		println "Important words terms.size ${terms.size()}"

		BytesRef termbr
		def wordMap = [:]

		while((termbr = termsEnum.next()) != null) {

			Term t = new Term(IndexInfo.FIELD_CONTENTS, termbr);
			String word = termbr.utf8ToString()
			char firstChar = word.charAt(0)
			int df = indexSearcher.getIndexReader().docFreq(t)

			if (
			  df < 3
			  || stopSet.contains(t.text())
			  || t.text().contains("'")
			  || t.text().length() < 2
			  || !firstChar.isLetter()
			//  || t.text().contains(".")
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

			def F1 = classify.Effectiveness.f1(positiveHits, negativeHits,
					totalDocs);

			if (F1 > 0.02) {
				wordMap += [(t): F1]
			}
		}
		
		//wordMap= wordMap.sort{a, b -> a.value <=> b.value}
		wordMap= wordMap.sort{-it.value}
		println "wordMap $wordMap"
		TermQuery[] termQueryList = wordMap.keySet().toList().take(MAX_TERMLIST_SIZE).collect {new TermQuery(it)}.asImmutable()
		println "f1 map size: ${wordMap.size()}  termQuerylist size: ${termQueryList.size()}  termQuerylist: $termQueryList"
		return termQueryList
	}

	public TermQuery[] getTFIDFWordList(){

		println "Important words terms.getDocCount: ${terms.getDocCount()}"

		def wordMap = [:]
		BytesRef termbr;

		while((termbr = termsEnum.next()) != null) {

			Term t = new Term(IndexInfo.FIELD_CONTENTS, termbr);
			String word = termbr.utf8ToString()
			char firstChar = word.charAt(0)
			int df = indexSearcher.getIndexReader().docFreq(t)

			if (
			  df < 3
			  || stopSet.contains(t.text())
			  || t.text().contains("'")
			  || t.text().length() < 2
			  || !firstChar.isLetter()
			//  || t.text().contains(".")
			)
				continue;

			long indexDf = indexReader.docFreq(t);
			int docCount = indexReader.numDocs()

			//for lucene 5
			//TFIDFSimilarity tfidfSim = new DefaultSimilarity()
			//For lucene 6
			TFIDFSimilarity tfidfSim = new ClassicSimilarity()
			PostingsEnum docsEnum = termsEnum.postings(MultiFields.getTermDocsEnum(indexReader, IndexInfo.FIELD_CONTENTS, termbr ));
			double tfidfTotal=0

			if (docsEnum != null) {
				while (docsEnum.nextDoc() != DocIdSetIterator.NO_MORE_DOCS) {
					double tfidf = tfidfSim.tf(docsEnum.freq()) * tfidfSim.idf(docCount, indexDf);
					tfidfTotal +=tfidf
				}
			}
			wordMap+= [(t) : tfidfTotal]
		}

		wordMap= wordMap.sort{a, b -> a.value <=> b.value}
		TermQuery[] termQueryList = wordMap.keySet().toList().take(MAX_TERMLIST_SIZE).collect {new TermQuery(it)}.asImmutable()	
		println "tfidf map size: ${wordMap.size()}  termQuerylist size: ${termQueryList.size()}  termQuerylist: $termQueryList"
		return termQueryList
	}
}