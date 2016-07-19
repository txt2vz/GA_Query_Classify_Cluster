import org.apache.lucene.analysis.standard.StandardAnalyzer
import org.apache.lucene.document.Document
import org.apache.lucene.document.Field

import org.apache.lucene.analysis.standard.StandardAnalyzer

import org.apache.lucene.document.StringField
import org.apache.lucene.document.TextField
import org.apache.lucene.index.DirectoryReader
import org.apache.lucene.index.IndexReader
import org.apache.lucene.index.IndexWriter
import org.apache.lucene.index.IndexWriterConfig
import org.apache.lucene.index.LiveIndexWriterConfig
import org.apache.lucene.queryparser.classic.QueryParser
import org.apache.lucene.search.IndexSearcher
import org.apache.lucene.search.Query
import org.apache.lucene.search.ScoreDoc
import org.apache.lucene.search.TopScoreDocCollector
import org.apache.lucene.store.Directory
import org.apache.lucene.store.RAMDirectory


//import org.apache.lucene.util.Version;


	// 0. Specify the analyzer for tokenizing text.
	//    The same analyzer should be used for indexing and searching
	StandardAnalyzer analyzer2 = new StandardAnalyzer();

	// 1. create the index
	Directory index2 = new RAMDirectory();	


	IndexWriterConfig config = new LiveIndexWriterConfig( analyzer2);
	/*

	IndexWriter w = new IndexWriter(index, config);
	addDoc(w, "Tintin: the crab with the golden claws", "1941");
	addDoc(w, "Tintin: the black island", "1938");
	addDoc(w, "Tintin: the land of black gold", "1950");
	addDoc(w, "Tintin: destination moon", "1953");
	addDoc(w, "Tintin: explorers on the moon", "1954");
	w.close();

	// 2. query
	String querystr =  "moon";

	// the "title" arg specifies the default field to use
	// when no field is explicitly specified in the query.
	Query q = new QueryParser( "title", analyzer).parse(querystr);

	// 3. search
	int hitsPerPage = 10;
	IndexReader reader = DirectoryReader.open(index);
	IndexSearcher searcher = new IndexSearcher(reader);
	TopScoreDocCollector collector = TopScoreDocCollector.create(hitsPerPage);
	searcher.search(q, collector);
	ScoreDoc[] hits = collector.topDocs().scoreDocs;
	
	// 4. display results
	println "Found " + hits.length + " hits."
	hits.each{
		int docId = it.doc;
		Document d = searcher.doc(docId);
		println(d.get("year") + "\t" + d.get("title"));
	}

	// reader can only be closed when there
	// is no need to access the documents any more.
	reader.close();


  def addDoc(IndexWriter w, String title, String isbn) throws IOException {
	Document doc = new Document();
	doc.add(new TextField("title", title, Field.Store.YES));

	// use a string field for year because we don't want it tokenized
	doc.add(new StringField("year", isbn, Field.Store.YES));
	w.addDocument(doc);
  }
  */

