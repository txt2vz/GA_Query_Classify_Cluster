package clusterGP

import ec.gp.*;
import index.ImportantWords

import org.apache.lucene.search.BooleanQuery
import org.apache.lucene.search.TermQuery

public class QueryData extends GPData {
	public BooleanQuery.Builder [] bqbArray = new BooleanQuery.Builder [3] 
	public BooleanQuery.Builder bqb
	public BooleanQuery bq
	public TermQuery tq;    // return value
	public Boolean dummy

	final TermQuery[] termQueryArray = new ImportantWords().getTFIDFWordList()

	public void copyTo(final GPData gpd)   // copy my stuff to another DoubleData
	{
		((QueryData)gpd).tq = tq;
		((QueryData)gpd).dummy = dummy;
		((QueryData)gpd).bqb = bqb;
		((QueryData)gpd).bq = bq;
		((QueryData)gpd).bqbArray = bqbArray;	
	}
}