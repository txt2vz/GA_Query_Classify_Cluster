package query;

import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.spans.SpanQuery;

import ec.gp.GPData;

/**
 * @author Laurie Hirsch
 */

public class QueryData extends GPData {
   
    public BooleanQuery query;
    
    public SpanQuery sq;
    
    public Query q[];
	
    public int index, clusterNumber;
  
    public void copyTo(final GPData gpd) {
      
        ((QueryData)gpd).index=index;
        ((QueryData)gpd).query = query;
        ((QueryData)gpd).q = q;

    }
}