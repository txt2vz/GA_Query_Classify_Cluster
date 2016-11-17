package cluster;

import ec.simple.SimpleFitness
import index.IndexInfo

import java.io.FileWriter
import java.util.Formatter;

import org.apache.lucene.document.Document
import org.apache.lucene.index.Term
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.ScoreDoc
import org.apache.lucene.search.TermQuery
import org.apache.lucene.search.TopDocs
import org.apache.lucene.search.TopScoreDocCollector
import org.apache.lucene.search.TotalHitCountCollector

/**
 * Store cluster information
 * 
 * @author Laurie 
 */

public class ClusterFit extends SimpleFitness {

	def queryMap = [:]
	def positiveScore=0 as float
	def negativeScore=0 as float
	def posHits=0
	def negHits=0
	def duplicateCount=0;
	def noHitsCount=0;
	def coreClusterPenalty=0
	def treePenalty=0;
	def graphPenalty=0
	def scoreOnly=0 as float
	def scoreOrig =0 as float
	def totalHits=0
	def fraction = 0 as float
	def baseFitness = 0 as float
	def scrPlus = 0 as float
	def missedDocs =0
	def emptyPen =0
	Formatter bestResultsOut
	def averageF1
	IndexSearcher searcher = IndexInfo.instance.indexSearcher;
	final int hitsPerPage=10000

	private int getTotalHits(){

		def docsReturned =	queryMap.keySet().inject([] as Set) {docSet, q ->

			TopDocs topDocs = searcher.search(q, hitsPerPage)
			ScoreDoc[] hits = topDocs.scoreDocs;
			hits.each {h -> docSet << h.doc				 }
			docSet
		}
		return docsReturned.size()
	}

	String queryShort (){
		def s=""
		queryMap.keySet().eachWithIndex {q, index ->
			if (index>0) s+='\n';
			s +=  "ClusterQuery: $index :  ${queryMap.get(q)}  ${q.toString(IndexInfo.FIELD_CONTENTS)}"
		}
		return s
	}

	public void queryStats (int job, int gen, int popSize){
		def messageOut=""
		FileWriter resultsOut = new FileWriter("results/clusterResultsF1.txt", true)
		resultsOut <<"  ***** Job: $job Gen: $gen PopSize: $popSize Noclusters: ${IndexInfo.instance.NUMBER_OF_CLUSTERS}  pathToIndex: ${IndexInfo.instance.pathToIndex}  **************************************************************** \n "

		def f1list = []
		queryMap.keySet().eachWithIndex {q, index ->

			TopScoreDocCollector collector = TopScoreDocCollector.create(hitsPerPage);
			searcher.search(q, collector);
			ScoreDoc[] hits = collector.topDocs().scoreDocs;
			def qString = q.toString(IndexInfo.FIELD_CONTENTS)

			println "***********************************************************************************"
			messageOut = "ClusterQuery $index searching for:  $qString  Found ${hits.length} hits: \n"
			println messageOut
			resultsOut << messageOut

			//map of categories (ground truth) and their frequencies
			def catsFreq=[:]
			hits.eachWithIndex{ h, i ->
				int docId = h.doc;
				def scr = h.score
				Document d = searcher.doc(docId);
				def catName = d.get(IndexInfo.FIELD_CATEGORY_NAME)
				def n = catsFreq.get((catName)) ?: 0
				catsFreq.put((catName), n + 1)

				if (i <5){
					messageOut = "$i path ${d.get(IndexInfo.FIELD_PATH)} cat name: $catName "
					println messageOut
					resultsOut << messageOut + '\n'
				}
			}
			println "Gen: $gen ClusterQuery: $index catsFreq: $catsFreq for query: $qString "

			//find the category with maximimum returned docs for this query
			def catMax = catsFreq?.max{it?.value} ?:0

			println "catsFreq: $catsFreq cats max: $catMax "

			//purity measure - check this is correct?
			def purity = (hits.size()==0) ? 0 : (1 / hits.size())  * catMax.value
			println "purity:  $purity"

			if (catMax !=0){
				TotalHitCountCollector totalHitCollector  = new TotalHitCountCollector();
				TermQuery catQ = new TermQuery(new Term(IndexInfo.FIELD_CATEGORY_NAME,
						catMax.key));
				searcher.search(catQ, totalHitCollector);
				def categoryTotal = totalHitCollector.getTotalHits();
				messageOut = "categoryTotal: $categoryTotal for catQ: $catQ \n"
				println messageOut
				resultsOut << messageOut

				def recall = catMax.value / categoryTotal;
				def precision = catMax.value / hits.size()
				def f1 = (2 * precision * recall) / (precision + recall);
				f1list << f1
				messageOut = "f1: $f1 recall: $recall precision: $precision"
				println messageOut
				resultsOut << messageOut + "\n"
				resultsOut << "Purity: $purity Job: $job \n"
			}
		}
		//averageF1 = f1list.sum()/f1list.size()
		averageF1 = f1list.sum()/ IndexInfo.NUMBER_OF_CLUSTERS
		messageOut = "f1list: $f1list averagef1: :$averageF1"
		println messageOut
		resultsOut << messageOut + "\n"

		resultsOut << "PosHits: $posHits NegHits: $negHits PosScore: $positiveScore NegScore: $negativeScore Fitness: ${fitness()} \n"
		resultsOut << "TotalHits: ${getTotalHits()} Total Docs:  ${IndexInfo.instance.indexReader.maxDoc()} \n"
		resultsOut << "************************************************ \n \n"

		resultsOut.flush()
		resultsOut.close()

		boolean appnd = true //job!=1
		FileWriter f = new FileWriter("results/resultsCluster.csv", appnd)
		Formatter csvOut = new Formatter(f);
		if (!appnd){
			final String fileHead = "gen, job, popSize, fitness, averageF1, query" + '\n';
			csvOut.format("%s", fileHead)
		}
		csvOut.format(
				"%s, %s, %s, %.3f, %.3f, %s",
				gen,
				job,
				popSize,
				fitness(),
				averageF1,
				queryForCSV(job) );

		csvOut.flush();
		csvOut.close()
	}

	private String queryForCSV (int job){
		def s="Job: $job "
		queryMap.keySet().eachWithIndex {q, index ->
			s += "ClusterQuery " + index + ": " + queryMap.get(q) + " " + q.toString(IndexInfo.FIELD_CONTENTS) + " ## "
		}
		return s + '\n'
	}

	public String fitnessToStringForHumans() {
		return  "ClusterQuery Fitness: ${this.fitness()} "
	}

	public String toString(int gen) {
		return "Gen: $gen ClusterQuery Fitness: ${this.fitness} qMap: $queryMap}"
	}
}