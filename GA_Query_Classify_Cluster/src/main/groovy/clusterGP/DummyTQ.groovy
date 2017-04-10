package clusterGP

import ec.*;
import ec.gp.*;
import ec.util.*;
import org.apache.lucene.index.Term
import org.apache.lucene.search.BooleanClause
import org.apache.lucene.search.BooleanQuery
import org.apache.lucene.search.TermQuery
import index.IndexInfo

public class DummyTQ extends GPNode
	{
	public String toString() { return " DummyTQ "; }

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
	public int expectedChildren() { return 1; }

	public void eval(final EvolutionState state,
		final int thread,
		final GPData input,
		final ADFStack stack,
		final GPIndividual individual,
		final Problem problem)
		{
	
		QueryData rd = ((QueryData)(input));
		rd.tq = new TermQuery(new Term(IndexInfo.FIELD_CONTENTS, "DummyXX"))
		rd.dummy = true		
		}
	}