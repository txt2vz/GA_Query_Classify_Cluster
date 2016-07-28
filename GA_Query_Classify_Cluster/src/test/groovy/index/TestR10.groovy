package index
import index.IndexInfo

import java.nio.file.Path
import java.nio.file.Paths

import org.apache.lucene.analysis.Analyzer
import org.apache.lucene.analysis.standard.StandardAnalyzer
import org.apache.lucene.index.IndexWriter
import org.apache.lucene.index.IndexWriterConfig
import org.apache.lucene.index.Term
import org.apache.lucene.search.IndexSearcher
import org.apache.lucene.search.TermQuery
import org.apache.lucene.search.TotalHitCountCollector
import org.apache.lucene.store.Directory
import org.apache.lucene.store.FSDirectory

import spock.lang.*

class TestR10 extends spock.lang.Specification {

	def "total r10 docs for grain category"() {
		given:
		Path path = Paths.get("indexes/r10")
		Directory directory = FSDirectory.open(path)
		Analyzer analyzer =	new StandardAnalyzer();
		IndexWriterConfig iwc = new IndexWriterConfig(analyzer);
		def writer = new IndexWriter(directory, iwc);

		when:
		IndexSearcher searcher = new IndexSearcher(writer.getReader());
		TotalHitCountCollector thcollector  = new TotalHitCountCollector();
		final TermQuery catQ = new TermQuery(new Term(IndexInfo.FIELD_CATEGORY_NAME,	"gra"))
		searcher.search(catQ, thcollector);
		def categoryTotal = thcollector.getTotalHits();
		writer.close()
		
		then:
		categoryTotal == 582
	}
}