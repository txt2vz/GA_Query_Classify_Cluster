package index

import org.apache.lucene.index.IndexReader
import org.apache.lucene.index.MultiFields
import org.apache.lucene.index.PostingsEnum
import org.apache.lucene.index.Term
import org.apache.lucene.index.Terms
import org.apache.lucene.index.TermsEnum
import org.apache.lucene.search.DocIdSetIterator
import org.apache.lucene.search.IndexSearcher
import org.apache.lucene.search.Query
import org.apache.lucene.search.TermQuery
import org.apache.lucene.search.similarities.ClassicSimilarity
import org.apache.lucene.search.similarities.TFIDFSimilarity
import org.apache.lucene.util.BytesRef

/**
 * Return a list of termqueries likely to be useful for building queries for classification or clustering
 * Terms should be in order of their likely usefulness query building 
 * @author Laurie 
 */

public class ImportantTerms  {

	public final static int SPAN_FIRST_MAX_END = 300;
	private final static int MAX_TERMQUERYLIST_SIZE = 300;

	private final IndexSearcher indexSearcher
	private final IndexReader indexReader
	private Terms terms
	private TermsEnum termsEnum
	private int maxDoc
	private Set<String> stopSet

	public static void main(String[] args){
		IndexInfo.instance.setCategoryName("cru")
		IndexInfo.instance.categoryNumber = '2'
		IndexInfo.instance.setIndex()
		def iw = new ImportantTerms()
		iw.getF1TermQueryList()
		//iw.getTFIDFTermQueryList()
	}

	public ImportantTerms() {
		indexSearcher = IndexInfo.instance.indexSearcher;
		indexReader = indexSearcher.getIndexReader()
		terms = MultiFields.getTerms(indexReader, IndexInfo.FIELD_CONTENTS)
		termsEnum = terms.iterator();
		maxDoc = indexReader.maxDoc();
		stopSet = StopSet.getStopSetFromFile()
	}

	//screen terms likely to be ineffective
	private boolean isEffectiveTerm(Term t) {
		int df = indexSearcher.getIndexReader().docFreq(t)
		def word = t.text()

		return (
				df > 2
				&& !stopSet.contains(word)
				&& !word.contains("'")
				&& word.length() > 1
				&& word.charAt(0).isLetter()
				//  && !word.contains(".")
				)
	}

	/**
	 * create a set of words based on F1 measure of the term when used to classify current category
	 */
	public TermQuery[] getF1TermQueryList(){

		println "Important words terms.getDocCount: ${terms.getDocCount()}"
		println "Important words terms.size ${terms.size()}"

		BytesRef termbr
		def termQueryMap = [:]

		while((termbr = termsEnum.next()) != null) {

			Term t = new Term(IndexInfo.FIELD_CONTENTS, termbr);
			if ( isEffectiveTerm(t) ){

				Query tq = new TermQuery(t)
				final int positiveHits = IndexInfo.instance.getQueryHitsWithFilter(indexSearcher,IndexInfo.instance.catTrainBQ, tq)
				final int negativeHits = IndexInfo.instance.getQueryHitsWithFilter(indexSearcher,IndexInfo.instance.othersTrainBQ, tq)

				def F1 = classify.Effectiveness.f1(positiveHits, negativeHits,
						IndexInfo.instance.totalTrainDocsInCat)

				if (F1 > 0.02) {
					termQueryMap += [(tq): F1]
				}
			}
		}

		termQueryMap= termQueryMap.sort{-it.value}
		println "termQueryMap: $termQueryMap"
		TermQuery[] termQueryList = termQueryMap.keySet().take(MAX_TERMQUERYLIST_SIZE).asImmutable()
		println "f1 map size: ${termQueryMap.size()}  termQuerylist size: ${termQueryList.size()}  termQuerylist: $termQueryList"
		return termQueryList
	}

	public TermQuery[] getTFIDFTermQueryList(){

		println "Important words terms.getDocCount: ${terms.getDocCount()}"
		def termMap = [:]
		BytesRef termbr;

		while((termbr = termsEnum.next()) != null) {

			Term t = new Term(IndexInfo.FIELD_CONTENTS, termbr);
			if (isEffectiveTerm(t)){

				long indexDf = indexReader.docFreq(t);
				int docCount = indexReader.numDocs()

				//for lucene 5 : TFIDFSimilarity tfidfSim = new DefaultSimilarity()
				TFIDFSimilarity tfidfSim = new ClassicSimilarity()
				PostingsEnum docsEnum = termsEnum.postings(MultiFields.getTermDocsEnum(indexReader, IndexInfo.FIELD_CONTENTS, termbr ))
				double tfidfTotal=0

				if (docsEnum != null) {
					while (docsEnum.nextDoc() != DocIdSetIterator.NO_MORE_DOCS) {
						double tfidf = tfidfSim.tf(docsEnum.freq()) * tfidfSim.idf(docCount, indexDf)
						tfidfTotal +=tfidf
					}
				}
				termMap+= [(t) : tfidfTotal]
			}
		}

		termMap= termMap.sort{a, b -> a.value <=> b.value}
		TermQuery[] termQueryList = termMap.keySet().take(MAX_TERMQUERYLIST_SIZE).collect {new TermQuery(it)}.asImmutable()
		println "tfidf map size: ${termMap.size()}  termQuerylist size: ${termQueryList.size()}  termQuerylist: $termQueryList"
		return termQueryList
	}
}