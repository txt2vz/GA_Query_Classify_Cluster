package luc

import java.nio.file.Path
import java.nio.file.Paths

import org.apache.lucene.analysis.Analyzer
import org.apache.lucene.analysis.standard.StandardAnalyzer
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
import org.apache.lucene.queryparser.classic.QueryParser
import org.apache.lucene.search.BooleanQuery
import org.apache.lucene.search.IndexSearcher
import org.apache.lucene.search.Query
import org.apache.lucene.search.ScoreDoc
import org.apache.lucene.search.TermQuery
import org.apache.lucene.search.TopScoreDocCollector
import org.apache.lucene.search.BooleanClause.Occur
import org.apache.lucene.store.Directory
import org.apache.lucene.store.FSDirectory

class R10bz {
	
	def indexPath =   "indexes/r10g"

	def docsPath =  /C:\Users\Laurie\Dataset\reuters-top10/   // Index files in this directory
	def docsCatMap=[:]
	
	Path path = Paths.get(indexPath)
	Directory directory = FSDirectory.open(path)
	Analyzer analyzer = new StandardAnalyzer();	

	static main(args) {
		def r10v = new R10bz()
		r10v.buildIndex()
		r10v.testIndex()
	//	r10.testBool()
		//r10.testIndex()
	
	}

	def buildIndex() {

		Date start = new Date();
		println("Indexing to directory '" + indexPath + "'...");
		IndexWriterConfig iwc = new IndexWriterConfig(analyzer);
		
		iwc.setOpenMode(OpenMode.CREATE);
		iwc.setRAMBufferSizeMB(512.0);
		IndexWriter writer = new IndexWriter(directory, iwc);

		def catNumber=0;
		new File(docsPath).eachDir {
			it.eachFileRecurse {
				if (!it.hidden && it.exists() && it.canRead() && !it.directory && it.name.endsWith('.txt'))  {
					docsCatMap[(it.name)] = docsCatMap.get(it.name, []) << catNumber.toString()
				}
			}
			catNumber++;
		}
		
	 catNumber=0;
		def Set<String> set=new HashSet();
		new File(docsPath).eachDir {

			it.eachFileRecurse {
				if (!it.hidden && it.exists() && it.canRead() && !it.directory && it.name.endsWith('.txt'))  {
					if (set.add(it.name))
						indexDocs(writer,it, catNumber)
				}
			}
			println "catNumber $catNumber"
			catNumber++;
		}

		Date end = new Date();
		println(end.getTime() - start.getTime() + " total milliseconds");
		
		writer.close()
		println "***************************************************************"
		
		//reader.close();
		writer.close();
	}
	
	def testBool(){
		
		println "boolean test"
		int hitsPerPage = 5;
		//writer.open
		IndexReader reader =  writer.getReader();
		
		BooleanQuery.Builder finalQuery = new BooleanQuery.Builder();
		BooleanQuery.Builder q1 = new BooleanQuery.Builder();
		q1.add(new TermQuery(new Term(IndexInfo.FIELD_CONTENTS, "ship")), Occur.MUST);
		q1.add(new TermQuery(new Term(IndexInfo.FIELD_CONTENTS, "wheat")), Occur.MUST);
		//finalQuery.add(q1.build(), Occur.MUST);
		Query q =  q1.build()  //finalQuery.build();
		
		IndexSearcher searcher = new IndexSearcher(reader);
		TopScoreDocCollector collector = TopScoreDocCollector.create(10);
		searcher.search(q, collector);
		ScoreDoc[] hits = collector.topDocs().scoreDocs;
		
		println " reader max doc " + reader.maxDoc()

		hits.each{
			int docId = it.doc;
			Document d = searcher.doc(docId);
			println(d.get(IndexInfo.FIELD_TEST_TRAIN) + "\t" + d.get("path") + "\t category:" +
					d.get(IndexInfo.FIELD_CATEGORY) );
		}
		
		//reader.close();
	//	writer.close();
		
	}
	
	def testIndex(){
		IndexReader reader = DirectoryReader.open(FSDirectory.open(Paths.get(indexPath)));
		String querystr =  "oil";

		Query q = new QueryParser(IndexInfo.FIELD_CONTENTS, analyzer).parse(querystr);

		int hitsPerPage = 5;
	

		println " reader max doc " + reader.maxDoc()
		IndexSearcher searcher = new IndexSearcher(reader);
		TopScoreDocCollector collector = TopScoreDocCollector.create(10);
		searcher.search(q, collector);
		ScoreDoc[] hits = collector.topDocs().scoreDocs;

		// 4. display results
		println "Searching for: $querystr Found ${hits.length} hits:"
		hits.each{
			int docId = it.doc;
			Document d = searcher.doc(docId);
			println(d.get(IndexInfo.FIELD_TEST_TRAIN) + "\t" + d.get("path") + "\t category:" +
					d.get(IndexInfo.FIELD_CATEGORY) );
		}

		println "end"

		reader.close();
		//writer.close();
	}
	/**
	 * Indexes the given file using the given writer, or if a directory is given,
	 * recurses over files and directories found under the given directory.
	 */
	def indexDocs(IndexWriter writer, File f, categoryNumber)
	throws IOException {

				//	println "Indexing ${f.canonicalPath} categorynumber: $categoryNumber"
		//	println " parent ${f.getParent()}"
		//	println " parent parent " + f.getParentFile().getParentFile().name;

		def doc = new Document()
		FileInputStream fis=new FileInputStream(f);

		// Construct a Field that is tokenized and indexed, but is not stored in the index verbatim.
		//	doc.add(Field.Text("contents", fis))

		// Add the path of the file as a field named "path".  Use a
		// field that is indexed (i.e. searchable), but don't tokenize
		// the field into separate words and don't index term frequency
		// or positional information:
		Field pathField = new StringField(IndexInfo.FIELD_PATH, f.getPath(), Field.Store.YES);
		doc.add(pathField);

		doc.add(new TextField(IndexInfo.FIELD_CONTENTS, new BufferedReader(new InputStreamReader(fis, "UTF-8"))) );

		def categoryList = docsCatMap.get(f.name)

		//if (categoryList.size()>1)
		//	println "l is  $categoryList ********************************************************************************************************************"

		categoryList.each {
			Field categoryField = new StringField(IndexInfo.FIELD_CATEGORY, it, Field.Store.YES);
			doc.add(categoryField)
		}

		String test_train
		if ( f.canonicalPath.contains("test")) test_train="test" else test_train="train";
		Field ttField = new StringField(IndexInfo.FIELD_TEST_TRAIN, test_train, Field.Store.YES)
		doc.add(ttField)

		writer.addDocument(doc);
	}
}
