package cluster;

import index.IndexInfo

import org.apache.lucene.index.Term
import org.apache.lucene.search.BooleanClause
import org.apache.lucene.search.BooleanQuery
import org.apache.lucene.search.IndexSearcher
import org.apache.lucene.search.TermQuery
import org.apache.lucene.search.TotalHitCountCollector

import ec.vector.IntegerVectorIndividual

trait CreateQueryTrait {

	final int hitsMin = 4
	int treePen, graphPen, duplicateCount, noHitsCount;

	def getORQL(String[] wordArray, IntegerVectorIndividual intVectorIndividual) {

		treePen = 0
		graphPen = 0
		duplicateCount = 0
		noHitsCount = 0

		IndexSearcher searcher = IndexInfo.instance.indexSearcher;
		def genes =[] as Set
		def bqbList = []
		//int clusterNumber=0
		//final int step = intVectorIndividual.genome.size() / IndexInfo.NUMBER_OF_CLUSTERS

		intVectorIndividual.genome.eachWithIndex {gene, index ->
			//GA to set number of clusters in gene 0
			//			if (index==0){cNumber = gene } else	{

			int clusterNumber =  index % IndexInfo.NUMBER_OF_CLUSTERS

			//possible advantage in keeping words for same query together on genome?
			//if (index % step ==0 && index > 0  && clusterNumber < IndexInfo.NUMBER_OF_CLUSTERS ){	clusterNumber++	}
			String word = wordArray[gene]
			bqbList[clusterNumber] = bqbList[clusterNumber] ?: new BooleanQuery.Builder()

			if (gene < wordArray.size() && gene >= 0){

				//++ seems to cause and odd error for groovy properties? !
				if (!genes.add(gene)) duplicateCount = duplicateCount + 1;

				TermQuery tq = new TermQuery(new Term(IndexInfo.FIELD_CONTENTS, word))
				bqbList[clusterNumber].add(tq,BooleanClause.Occur.SHOULD)
			}
		}
		return bqbList
	}

	def getORNOTQL(String[] wordArray, IntegerVectorIndividual intVectorIndividual ) {

		treePen = 0
		graphPen = 0
		duplicateCount = 0
		noHitsCount = 0

		IndexSearcher searcher = IndexInfo.instance.indexSearcher;
		def genes =[] as Set
		def bqbList = []

		intVectorIndividual.genome.eachWithIndex {gene, index ->

			int clusterNumber =  index % IndexInfo.NUMBER_OF_CLUSTERS
			String wrd = wordArray[gene]
			bqbList[clusterNumber] = bqbList[clusterNumber] ?: new BooleanQuery.Builder()

			if (gene < wordArray.size() && gene >= 0){
				if (!genes.add(gene)) duplicateCount = duplicateCount + 1;

				TermQuery tq = new TermQuery(new Term(IndexInfo.FIELD_CONTENTS, wrd))

				if (index >= (intVectorIndividual.genome.size() -  IndexInfo.NUMBER_OF_CLUSTERS )){

					bqbList[clusterNumber].add(tq,BooleanClause.Occur.MUST_NOT)

				} else
				{
					bqbList[clusterNumber].add(tq,BooleanClause.Occur.SHOULD)
				}

				//check that the subquery returns something
				TotalHitCountCollector collector = new TotalHitCountCollector();
				searcher.search(tq, collector);
				if (collector.getTotalHits() < hitsMin)
				{
					noHitsCount = noHitsCount + 1;
				}
			}
		}
		return bqbList
	}

	//query in DNF format - could be used to generate graph
	def getANDQL(String[] wordArray, IntegerVectorIndividual intVectorIndividual) {

		treePen=0
		graphPen=0
		duplicateCount=0
		noHitsCount=0

		IndexSearcher searcher = IndexInfo.instance.indexSearcher;
		def duplicates =[] as Set
		def clusterSets =[]
		def word0=null, word1=null
		int qNumber=0;

		def bqbList = []

		intVectorIndividual.genome.eachWithIndex {gene, index ->
			if (gene >wordArray.size() || gene < 0)
				gene =0;

			if (word0==null){
				word0 = wordArray[gene]
			} else {
				word1 = wordArray[gene]

				int clusterNumber =  qNumber % IndexInfo.NUMBER_OF_CLUSTERS
				qNumber++

				def wrds=[word0, word1] as Set
				if (word0==word1 || !duplicates.add(wrds))
				{
					duplicateCount= duplicateCount + 1;
				}

				//check in graph form for viz
				if (! clusterSets[clusterNumber]){
					clusterSets[clusterNumber]= [] as Set
				}else
				if (!  (clusterSets[clusterNumber].contains(word0) ||clusterSets[clusterNumber].contains(word1) ) )
					graphPen = graphPen + 1

				//check that query will be in tree form (for Viz)
				//	if (clusterSets[clusterNumber].size()>0 )
				//		if (! ( (clusterSets[clusterNumber].contains(word0) && !clusterSets[clusterNumber].contains(word1))
				//		|| (clusterSets[clusterNumber].contains(word1) && !clusterSets[clusterNumber].contains(word0)) )) treePen = treePen + 1;

				clusterSets[clusterNumber].add(word0)
				clusterSets[clusterNumber].add(word1)

				BooleanQuery.Builder subbqb = new BooleanQuery.Builder();
				subbqb.add(new TermQuery(new Term(IndexInfo.FIELD_CONTENTS, word0)), BooleanClause.Occur.MUST);
				subbqb.add(new TermQuery(new Term(IndexInfo.FIELD_CONTENTS, word1)), BooleanClause.Occur.MUST);
				BooleanQuery subq = subbqb.build();

				bqbList[clusterNumber] = bqbList[clusterNumber] ?: new BooleanQuery.Builder()

				//check that the subquery returns something
				TotalHitCountCollector collector = new TotalHitCountCollector();
				searcher.search(subq, collector);
				if (collector.getTotalHits() < hitsMin)
				{
					noHitsCount = noHitsCount + 1;
				}

				bqbList[clusterNumber].add(subq, BooleanClause.Occur.SHOULD);
				word0=null;
			}
		}
		return bqbList
	}
}