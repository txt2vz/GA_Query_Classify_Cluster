package clusterGP

import ec.*;
import ec.gp.*;
import ec.util.*;
import org.apache.lucene.search.BooleanClause
import org.apache.lucene.search.BooleanQuery
import org.apache.lucene.search.TermQuery
import org.apache.lucene.index.Term
import index.IndexInfo

public class PROG2 extends GPNode {
	public String toString() {
		return " PROG2 ";
	}

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
	public int expectedChildren() {
		return 2;
	}

	public void eval(final EvolutionState state,
			final int thread,
			final GPData input,
			final ADFStack stack,
			final GPIndividual individual,
			final Problem problem) {

		QueryData rd = ((QueryData)(input));

		children[0].eval(state,thread,input,stack,individual,problem);
		rd.bqbArray[0] = rd.bqb

		children[1].eval(state,thread,input,stack,individual,problem);
		rd.bqbArray[1] = rd.bqb
	}
}