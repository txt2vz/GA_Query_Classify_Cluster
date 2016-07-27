package index

import groovy.io.FileType

import java.nio.file.Path
import java.nio.file.Paths

import org.apache.lucene.analysis.Analyzer
import org.apache.lucene.analysis.standard.StandardAnalyzer
import org.apache.lucene.codecs.*
import org.apache.lucene.document.Document
import org.apache.lucene.document.Field
import org.apache.lucene.document.StringField
import org.apache.lucene.document.TextField
import org.apache.lucene.index.DirectoryReader
import org.apache.lucene.index.IndexReader
import org.apache.lucene.index.IndexWriter
import org.apache.lucene.index.IndexWriterConfig
import org.apache.lucene.index.Term
import org.apache.lucene.index.IndexWriterConfig.OpenMode
import org.apache.lucene.search.BooleanQuery
import org.apache.lucene.search.IndexSearcher
import org.apache.lucene.search.Query
import org.apache.lucene.search.ScoreDoc
import org.apache.lucene.search.TermQuery
import org.apache.lucene.search.TopScoreDocCollector
import org.apache.lucene.search.TotalHitCountCollector
import org.apache.lucene.search.BooleanClause.Occur
import org.apache.lucene.store.Directory
import org.apache.lucene.store.FSDirectory

class BuildR10 {
	// Create Lucene index in this directory
	def indexPath = "indexes/r10"

	// Index files in this directory
	def docsPath =  /C:\Users\Laurie\Dataset\reuters-top10/

	Path path = Paths.get(indexPath)
	Directory directory = FSDirectory.open(path)
	Analyzer analyzer = //new EnglishAnalyzer();
	new StandardAnalyzer();
	def catsFreq=  [:]//IndexInfo.instance.categoryDocumentCount
	//def docsSet = [] as Set
	def x =0
	IndexWriter writer

	static main(args) {
		def i = new BuildR10()
		i.buildIndex()
		i.testBool()
	}

	def buildIndex() {
		IndexWriterConfig iwc = new IndexWriterConfig(analyzer);

		// Create a new index in the directory, removing any
		// previously indexed documents:
		iwc.setOpenMode(OpenMode.CREATE);
		writer = new IndexWriter(directory, iwc);

		Date start = new Date();
		println("Indexing to directory '" + indexPath + "'...");
		def catNumber=0;

		new File(docsPath).eachDir {
			//	it.eachDir{
			it.eachFileRecurse(FileType.FILES) { file ->

				indexDocs(writer,file, catNumber)
			}
			//	}
			catNumber++;
		}

		Date end = new Date();
		println (end.getTime() - start.getTime() + " total milliseconds");
		println "Total docs: " + writer.maxDoc()

		IndexSearcher searcher = new IndexSearcher(writer.getReader());

		TotalHitCountCollector thcollector  = new TotalHitCountCollector();
		final TermQuery catQ = new TermQuery(new Term(IndexInfo.FIELD_CATEGORY_NAME,	"gra"))
		searcher.search(catQ, thcollector);
		def categoryTotal = thcollector.getTotalHits();
		println "cateTotoal $categoryTotal"
		println "catsFreq $catsFreq"
	
		writer.close()
		println "End ***************************************************************"
	}

	//index the doc adding fields for path, category, test/train and contents
	def indexDocs(IndexWriter writer, File f, int catNumber)
	{
		def doc = new Document()

		Field pathField = new StringField(IndexInfo.FIELD_PATH, f.getPath(), Field.Store.YES);
		doc.add(pathField);

		def catName = f.getCanonicalPath().drop(41).take(3)

		if (x<4) println "catName $catName"
		x++
		
		//keep count of documents in each category
		def n = catsFreq.get((catName)) ?: 0
		catsFreq.put((catName), n + 1)

		Field catNameField = new StringField(IndexInfo.FIELD_CATEGORY_NAME, catName, Field.Store.YES);
		doc.add(catNameField)

		Field catNumberField = new StringField(IndexInfo.FIELD_CATEGORY_NUMBER, String.valueOf(catNumber), Field.Store.YES);
		doc.add(catNumberField)

		doc.add(new TextField(IndexInfo.FIELD_CONTENTS, f.text,  Field.Store.YES))

		String test_train
		if ( f.canonicalPath.contains("test")) test_train="test" else test_train="train";

		Field ttField = new StringField(IndexInfo.FIELD_TEST_TRAIN, test_train, Field.Store.YES)
		doc.add(ttField)

		writer.addDocument(doc);
	}

	def testBool(){

		println "boolean test"

		IndexReader reader = DirectoryReader.open(FSDirectory.open(Paths.get(indexPath)));

		BooleanQuery.Builder finalQuery = new BooleanQuery.Builder();
		BooleanQuery.Builder q1 = new BooleanQuery.Builder();
		q1.add(new TermQuery(new Term(IndexInfo.FIELD_CONTENTS, "ship")), Occur.MUST);
		q1.add(new TermQuery(new Term(IndexInfo.FIELD_CONTENTS, "wheat")), Occur.MUST);
		//finalQuery.add(q1.build(), Occur.MUST);
		Query q =  q1.build() 
		
		IndexSearcher searcher = new IndexSearcher(reader);
		TopScoreDocCollector collector = TopScoreDocCollector.create(10);
		searcher.search(q, collector);
		ScoreDoc[] hits = collector.topDocs().scoreDocs;

		println " reader max doc " + reader.maxDoc()

		hits.each{
			int docId = it.doc;
			Document d = searcher.doc(docId);
			println(d.get(IndexInfo.FIELD_TEST_TRAIN) + "\t" + d.get("path") + "\t category:" +
					d.get(IndexInfo.FIELD_CATEGORY_NAME) + " catNumber " + d.get(IndexInfo.FIELD_CATEGORY_NUMBER) );
		}

		reader.close();
	}
}