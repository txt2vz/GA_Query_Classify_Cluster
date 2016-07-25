package classify;

//import org.apache.lucene.search.Query
import ec.simple.SimpleFitness

/**
 * Store cluster information
 * 
 * @author Laurie 
 */

public class ClusterFit extends SimpleFitness {
	
	def queryMap = [:]	
	def positiveScore=0
	def negativeScore=0;
	def posHits=0
	def negHits=0
	def duplicateCount=0;
	def noHitsCount=0;
	def coreClusterPenalty=0
	def treePenalty=0;
	def graphPenalty=0

	public String queryShort (){
		def s=""
		queryMap.keySet().eachWithIndex {q, index ->
			if (index>0) s+='\n';			
			s +=  "Cluster " + index + ": " + queryMap.get(q) + " " + q   
		}
		return s
	}
	
	public String queryForCSV (){
		def s=""
		queryMap.keySet().eachWithIndex {q, index ->
			s += " Cluster " + index + ": " + queryMap.get(q) +  q + "##"
		}
		return s
	}

	public String fitnessToStringForHumans() {
		def origFit = this.fitness() -200
		return  "Cluster Fitness: " + this.fitness() + " origFit: $origFit";
	}

	public String toString(int gen) {
		return "Gen: " + gen + " Cluster Fitness: " + this.fitness
		+ " qMap: " + queryMap;
	}
}
