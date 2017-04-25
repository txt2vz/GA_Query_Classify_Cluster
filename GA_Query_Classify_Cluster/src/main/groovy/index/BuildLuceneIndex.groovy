package index

import java.nio.file.Path
import java.nio.file.Paths

import org.apache.lucene.analysis.Analyzer
import org.apache.lucene.analysis.standard.StandardAnalyzer
import org.apache.lucene.codecs.*
import org.apache.lucene.document.Document
import org.apache.lucene.document.Field
import org.apache.lucene.document.StringField
import org.apache.lucene.document.TextField
import org.apache.lucene.index.IndexWriter
import org.apache.lucene.index.IndexWriterConfig
import org.apache.lucene.index.Term
import org.apache.lucene.index.IndexWriterConfig.OpenMode
import org.apache.lucene.search.IndexSearcher
import org.apache.lucene.search.TermQuery
import org.apache.lucene.search.TotalHitCountCollector
import org.apache.lucene.store.Directory
import org.apache.lucene.store.FSDirectory

// Index text files found in this directory
def docsPath =
		/C:\Users\Laurie\Dataset\20bydate/
// /C:\Users\Laurie\Dataset\reuters-top10/

//build index here
def indexPath =
		//       'indexes/R10'
		'indexes/20NG'

//set to true when indexing R10 - different directory structure.
boolean reuters = false

Path path = Paths.get(indexPath)
Directory directory = FSDirectory.open(path)
Analyzer analyzer = //new EnglishAnalyzer();  //with stemming
		new StandardAnalyzer()
IndexWriterConfig iwc = new IndexWriterConfig(analyzer)

//store doc counts for each category
def catsFreq=[:]

// Create a new index in the directory, removing any
// previously indexed documents:
iwc.setOpenMode(OpenMode.CREATE)
IndexWriter writer = new IndexWriter(directory, iwc)
IndexSearcher indexSearcher = new IndexSearcher(writer.getReader())
Date start = new Date();
println("Indexing to directory: $indexPath  from: $docsPath ...")

def categoryNumber=-1
new File(docsPath).eachDir {
	if (reuters) categoryNumber++
	else categoryNumber=-1  //reset for 20NG for test and train directories
	it.eachFileRecurse {file ->
		if (!reuters && file.isDirectory()) categoryNumber++
		if (!file.hidden && file.exists() && file.canRead() && !file.isDirectory()) // && categoryNumber <3)

		{
			def doc = new Document()

			Field catNumberField = new StringField(IndexInfo.FIELD_CATEGORY_NUMBER, String.valueOf(categoryNumber), Field.Store.YES);
			doc.add(catNumberField)

			Field pathField = new StringField(IndexInfo.FIELD_PATH, file.getPath(), Field.Store.YES);
			doc.add(pathField);

			String parent = file.getParent()
			String grandParent = file.getParentFile().getParent()

			def catName
			//reuters dataset has different directory structure
			if (reuters)
				catName =   grandParent.substring(grandParent.lastIndexOf("\\") + 1, grandParent.length())
			else
				catName =   parent.substring(parent.lastIndexOf("\\") + 1, parent.length())

			Field catNameField = new StringField(IndexInfo.FIELD_CATEGORY_NAME, catName, Field.Store.YES);
			doc.add(catNameField)

			String test_train
			if ( file.canonicalPath.contains("test")) test_train="test" else test_train="train"
			Field ttField = new StringField(IndexInfo.FIELD_TEST_TRAIN, test_train, Field.Store.YES)
			doc.add(ttField)

			doc.add(new TextField(IndexInfo.FIELD_CONTENTS, file.text,  Field.Store.YES))

			def n = catsFreq.get((catName)) ?: 0
			catsFreq.put((catName), n + 1)
			writer.addDocument(doc)
		}
	}
}

TotalHitCountCollector trainCollector  = new TotalHitCountCollector();
final TermQuery trainQ = new TermQuery(new Term(IndexInfo.FIELD_TEST_TRAIN,	"train"))

TotalHitCountCollector testCollector  = new TotalHitCountCollector();
final TermQuery testQ = new TermQuery(new Term(IndexInfo.FIELD_TEST_TRAIN,	"test"))

indexSearcher.search(trainQ, trainCollector);
def trainTotal = trainCollector.getTotalHits();

indexSearcher.search(testQ, testCollector);
def testTotal = testCollector.getTotalHits();

Date end = new Date();
println (end.getTime() - start.getTime() + " total milliseconds");
println "testTotal $testTotal trainTotal $trainTotal"
println "catsFreq $catsFreq"
println "Total docs: " + writer.maxDoc()
writer.close()
println "End ***************************************************************"