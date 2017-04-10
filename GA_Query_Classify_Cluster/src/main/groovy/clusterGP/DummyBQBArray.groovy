package clusterGP

import ec.*;
import ec.gp.*;
import ec.util.*;
import org.apache.lucene.search.BooleanClause
import org.apache.lucene.search.BooleanQuery
import org.apache.lucene.search.TermQuery
import org.apache.lucene.index.Term
import index.IndexInfo

public class DummyBQBArray extends GPNode
	{
	public String toString() { return " Dummy "; }

/*
  public void checkConstraints(final EvolutionState state,
  final int tree,
  final GPIndividual typicalIndividual,
  final Parameter individualBase)
  {
  super.checkConstraints(state,tree,typicalIndividual,individualBase);
  if (children.length!=2)
  state.output.error("Incorrect number of children for node " +
  toStringForError() + " at " +
  individualBase);
  }
*/
	public int expectedChildren() { return 0; }

	public void eval(final EvolutionState state,
		final int thread,
		final GPData input,
		final ADFStack stack,
		final GPIndividual individual,
		final Problem problem)
		{
	
		QueryData rd = ((QueryData)(input));
		rd.dummy = true
		
		def bqb = new BooleanQuery.Builder()
		bqb.add(new TermQuery(new Term(IndexInfo.FIELD_CONTENTS, "DummyXX")), BooleanClause.Occur.SHOULD)
//		
		rd.bqbArray[0] = bqb	
		rd.bqbArray[1] = bqb		
//		rd.bqbArray[2] = bqb
		}
	}