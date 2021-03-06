package clusterGP.terminals

import clusterGP.QueryData
import ec.*;
import ec.gp.*;
import ec.util.*;

public class TQ8 extends GPNode {
	public String toString() {
		return "tq8 "
	}

	public int expectedChildren() {
		return 0;
	}

	public void eval(final EvolutionState state,
			final int thread,
			final GPData input,
			final ADFStack stack,
			final GPIndividual individual,
			final Problem problem) {
			
		QueryData rd = ((QueryData)(input));
		rd.tq = rd.termQueryArray[8]		
	}
}