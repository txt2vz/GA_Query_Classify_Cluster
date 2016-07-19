package query;

import java.io.IOException;
import java.util.Map;
import java.util.TreeMap;

import lucene.ImportantWords;
import lucene.IndexInfoStaticG;

import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TotalHitCountCollector;
import org.apache.lucene.search.spans.SpanFirstQuery;
import org.apache.lucene.search.spans.SpanTermQuery;

IndexSearcher searcher = IndexInfoStaticG.getIndexSearcher();
Formatter bestResultsOut = new Formatter("resultsOR.csv");
final String fileHead = "category, f1train, f1test, cat" + '\n';
bestResultsOut.format("%s", fileHead);

def micro=0
def totalpos=0
def totalneg=0
def totaltest=0

for (cat in 0..19){
	IndexInfoStaticG.setCatNumber(cat)

	float F1train = 0;
	String[] wordArray;
	BooleanQuery query = new BooleanQuery(true);

	ImportantWords importantWords = new ImportantWords();

	System.out.println("Total docs for cat  "
			+ IndexInfoStaticG.getCatnumberAsString() + " "
			+ IndexInfoStaticG.totalTrainDocsInCat
			+ " Total test docs for cat "
			+ IndexInfoStaticG.totalTestDocsInCat);

	wordArray = importantWords.getF1WordList(true, true);

	println "word is ${wordArray[0]}"

	(0..3).each{

		query.add(new TermQuery(
				new Term(IndexInfoStaticG.FIELD_CONTENTS,  wordArray[it])),
				BooleanClause.Occur.SHOULD);
	}

	println " category $cat query is $query"

	TotalHitCountCollector collector = new TotalHitCountCollector();
	searcher.search(query, IndexInfoStaticG.catTrainF, collector);
	int positiveMatch = collector.getTotalHits();

	collector = new TotalHitCountCollector();
	searcher.search(query, IndexInfoStaticG.othersTrainF, collector);
	int negativeMatch = collector.getTotalHits();

	F1train = ClassifyQuery.f1(positiveMatch, negativeMatch,
			IndexInfoStaticG.totalTrainDocsInCat);

	collector = new TotalHitCountCollector();
	searcher.search(query, IndexInfoStaticG.catTestF, collector);
	positiveMatch = collector.getTotalHits();
	
	collector = new TotalHitCountCollector();
	searcher.search(query, IndexInfoStaticG.othersTestF, collector);
	negativeMatch = collector.getTotalHits();
	
	totaltest += IndexInfoStaticG.totalTestDocsInCat;

	F1test = ClassifyQuery.f1(positiveMatch, negativeMatch,
		IndexInfoStaticG.totalTestDocsInCat);
		
		totalpos +=positiveMatch
		totalneg +=negativeMatch

	println "Category: $cat F1test: $F1test  F1trina: $F1train query: $query"
	println " -----------------------------------------------------------------"
	
	bestResultsOut.format(
		"%s, %.3f, %.3f, %s \n",
		cat, F1train, F1test,query);
}
bestResultsOut.flush();

println "toatl pos  $totalpos totalneg $totalneg"
micro =  ClassifyQuery.f1(totalpos, totalneg, totaltest);
			//IndexInfoStaticG.totalTestDocs);  

println "TotalTest todcs = $IndexInfoStaticG.totalTestDocs   totalmeasured: $totaltest micro=$micro";

