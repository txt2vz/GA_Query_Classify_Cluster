package index

import java.nio.file.Path
import java.nio.file.Paths

import org.apache.lucene.analysis.Analyzer
import org.apache.lucene.analysis.en.EnglishAnalyzer
import org.apache.lucene.analysis.standard.StandardAnalyzer
import org.apache.lucene.document.Document
import org.apache.lucene.document.Field
import org.apache.lucene.document.StringField
import org.apache.lucene.document.TextField
import org.apache.lucene.index.IndexReader
import org.apache.lucene.index.IndexWriter
import org.apache.lucene.index.IndexWriterConfig
import org.apache.lucene.index.IndexWriterConfig.OpenMode
//import org.apache.lucene.queryparser.classic.QueryParser
import org.apache.lucene.search.IndexSearcher
import org.apache.lucene.search.Query
import org.apache.lucene.search.ScoreDoc
import org.apache.lucene.search.TopScoreDocCollector
import org.apache.lucene.store.Directory
import org.apache.lucene.store.FSDirectory
import org.apache.lucene.codecs.*

class BuildClusterIndexFromCSV {
	// Create Lucene index in this directory
	def indexPath = "indexes/crisis4FireBombFloodShoot"              
	//  "indexes/disaster"

	def filePath =
//	"dataset/crisisData/2012_Colorado_wildfires-tweets_labeled.csv"
//	"dataset/crisisData/2013_Boston_bombings-tweets_labeled.csv"
//	"dataset/crisisData/2013_LA_airport_shootings-tweets_labeled.csv"
	"dataset/crisisData/2013_Queensland_floods-tweets_labeled.csv"
//	"/home/test/dataset/cw.csv"
//	"/home/test/dataset/qsFlood.csv"

	def catName= "qsflood"
//	def catName= "bostonBomb"
//	def catName= "coloradowf"
//	def catName= "LAshooting"

	Path path = Paths.get(indexPath)
	Directory directory = FSDirectory.open(path)
	Analyzer analyzer = //new EnglishAnalyzer();
	                   new StandardAnalyzer();

	static main(args) {
		def i = new BuildClusterIndexFromCSV()
		i.buildIndex()
	}

	def buildIndex() {
		IndexWriterConfig iwc = new IndexWriterConfig(analyzer);

		// Create a new index in the directory, removing any
		// previously indexed documents:
	//	iwc.setOpenMode(OpenMode.CREATE);
		iwc.setOpenMode(OpenMode.APPEND);

		IndexWriter writer = new IndexWriter(directory, iwc);

		Date start = new Date();
		println("Indexing to directory '" + indexPath + "'...");

		def x=0
		new File(filePath).splitEachLine(",") {fields ->
             
			def body = fields[1]
			//def tweetID = fields[0]
			def doc = new Document()
			if (body!="")
				doc.add(new TextField(IndexInfo.FIELD_CONTENTS, body,  Field.Store.YES))

			Field catNameField = new StringField(IndexInfo.FIELD_CATEGORY_NAME, catName, Field.Store.YES);
			doc.add(catNameField)
			
			if (x< 5)
				println "t " +  body
			x++
			writer.addDocument(doc);
		}
		println "Total docs: " + writer.maxDoc()
		writer.close()
		println "done.."

	}
}