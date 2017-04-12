package cluster;

import ec.gp.koza.KozaFitness
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
 * Store cluster fitness information
 * 
 * @author Laurie 
 */

public class ClusterFit extends SimpleFitness {

	def queryMap = [:]
	def positiveScoreTotal=0 as float
	def negativeScoreTotal=0 as float
	def positiveHits=0
	def negativeHits=0
	def duplicateCount
	def lowSubqHits=0
	def coreClusterPenalty=0
	def scoreOnly=0 as float
	def totalHits=0
	def fraction = 0 as float
	def baseFitness = 0 as float
	def scorePlus1000 = 0 as float
	def missedDocs =0
	def zeroHitsCount =0
	boolean isDummy = false
	boolean emptyQueries = false
	
	def treePenalty=0;
	def graphPenalty=0
	
	Formatter bestResultsOut
	IndexSearcher searcher = IndexInfo.instance.indexSearcher;
	final int hitsPerPage=IndexInfo.instance.indexReader.maxDoc()

	String queryShort (){
		def s="queryMap.size ${queryMap.size()} \n"
		queryMap.keySet().eachWithIndex {q, index ->
			if (index>0) s+='\n';
			s +=  "ClusterQuery: $index :  ${queryMap.get(q)}  ${q.toString(IndexInfo.FIELD_CONTENTS)}"
		}
		return s
	}

	public void queryStats (int job, int gen, int popSize){
		def messageOut=""
		FileWriter resultsOut = new FileWriter("results/clusterResultsF1.txt", true)
		resultsOut <<"  ***** Job: $job Gen: $gen PopSize: $popSize Noclusters: ${IndexInfo.NUMBER_OF_CLUSTERS}  pathToIndex: ${IndexInfo.instance.pathToIndex}  *********** ${new Date()} ***************************************************** \n"

		def f1list = [], precisionList =[], recallList =[]
		queryMap.keySet().eachWithIndex {q, index ->

			TopScoreDocCollector collector = TopScoreDocCollector.create(hitsPerPage);
			searcher.search(q, collector);
			ScoreDoc[] hits = collector.topDocs().scoreDocs;
			def qString = q.toString(IndexInfo.FIELD_CONTENTS)

			println "***********************************************************************************"
			messageOut = "ClusterQuery: $index hits: ${hits.length} Query:  $qString \n"
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

//view top 5 results
//				if (i <5){
//					messageOut = "$i path ${d.get(IndexInfo.FIELD_PATH)} cat name: $catName "
//					println messageOut
//					resultsOut << messageOut + '\n'
//				}
			}
			println "Gen: $gen ClusterQuery: $index catsFreq: $catsFreq for query: $qString "

			//find the category with maximimum returned docs for this query
			def catMax = catsFreq?.max{it?.value} ?:0

			println "catsFreq: $catsFreq cats max: $catMax "

			//purity measure - check this is correct?
            //def purity = (hits.size()==0) ? 0 : (1 / hits.size())  * catMax.value
	        //println "purity:  $purity"

			if (catMax !=0){
				TotalHitCountCollector totalHitCollector  = new TotalHitCountCollector();
				TermQuery catQ = new TermQuery(new Term(IndexInfo.FIELD_CATEGORY_NAME,
						catMax.key));
				searcher.search(catQ, totalHitCollector);
				def categoryTotal = totalHitCollector.getTotalHits();
				messageOut = "categoryTotal: $categoryTotal for category: $catQ \n"
				println messageOut
				resultsOut << messageOut

				def recall = catMax.value / categoryTotal;
				def precision = catMax.value / hits.size()
				def f1 = (2 * precision * recall) / (precision + recall);
				
				f1list << f1
				precisionList << precision
				recallList << recall
				messageOut = "f1: $f1 recall: $recall precision: $precision"
				println messageOut
				resultsOut << messageOut + "\n"
				//resultsOut << "Purity: $purity Job: $job \n"
			}
		}
	    
		def averageF1 = (f1list) ? f1list.sum()/ IndexInfo.NUMBER_OF_CLUSTERS : 0
		def averageRecall = (recallList) ? recallList.sum()/ IndexInfo.NUMBER_OF_CLUSTERS : 0
		def averagePrecision =(precisionList) ? precisionList.sum()/ IndexInfo.NUMBER_OF_CLUSTERS :0
		messageOut ="***  TOTALS:   *****   f1list: $f1list averagef1: :$averageF1  ** average precision: $averagePrecision average recall: $averageRecall"
		println messageOut
		
		resultsOut << "TotalHits: $totalHits Total Docs:  ${IndexInfo.instance.indexReader.maxDoc()} \n"
		resultsOut << "PosHits: $positiveHits NegHits: $negativeHits PosScore: $positiveScoreTotal NegScore: $negativeScoreTotal Fitness: ${fitness()} \n"
		resultsOut << messageOut + "\n"
		resultsOut << "************************************************ \n \n"

		resultsOut.flush()
		resultsOut.close()

		boolean appnd =  true//job!=0
		FileWriter fcsv = new FileWriter("results/resultsCluster.csv", appnd)
		//Formatter csvOut = new Formatter(fcsv);
		if (!appnd){
			final String fileHead = "gen, job, popSize, fitness, averageF1, averagePrecision, averageRecall, query" + '\n';
		//	csvOut.format("%s", fileHead)			
		}
//		csvOut.format(
//				"%s, %s, %s, %.3f, %.3f, %.3f, %.3f, %s",
//				gen,
//				job,
//				popSize,
//				fitness(),
//				averageF1,
//				averagePrecision,
//				averageRecall)//,
//			//	queryForCSV(job) );

		//csvOut.flush();
		//csvOut.close()
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