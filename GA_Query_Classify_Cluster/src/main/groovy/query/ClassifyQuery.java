package query;


import lucene.ImportantWords;

import org.apache.lucene.search.Query;

import ec.EvolutionState;
import ec.gp.GPProblem;
import ec.simple.SimpleProblemForm;
import ec.util.Parameter;


public abstract class ClassifyQuery extends GPProblem implements SimpleProblemForm {

	public static final String P_DATA = "data";

	public static final String P_FILE = "file";

	protected ImportantWords importantWords;

	//protected QueryData input;

	protected Query query;

	public void setup(final EvolutionState state, final Parameter base) {

		super.setup(state, base);

//		input = (QueryData) state.parameters.getInstanceForParameterEq(base
//				.push(P_DATA), null, QueryData.class);

//		input.setup(state, base.push(P_DATA));

		// GPs can produce very long queries
		org.apache.lucene.search.BooleanQuery.setMaxClauseCount(2048);

		state.parameters.getFile(base.push(P_FILE), null);
	}

	protected boolean problemQuery(String queryStr) {

		// problem with strong typing in ECJ: need to find better solution but
		// this works	
//		if (queryStr.indexOf(Dummy.DUMMY_STRING) >= 0) {
//			return true;
//		}

		// lucene has a limit of 32 on the number of require/prohibit(+
		// -)functions so need to discourage excessive use
		char c;
		int plusMinusCount = 0;
		for (int x = 0; x < queryStr.length(); x++) {
			c = queryStr.charAt(x);
			if (c == '+' || c == '-') {
				plusMinusCount++;
			}
			if (plusMinusCount > 22)
				return true;
		}

		return false;
	}

	public static float precision(final int positiveMatch, final int negativeMatch) {
		final int totalRetrieved = positiveMatch + negativeMatch;
		if (totalRetrieved > 0)
			return (float) positiveMatch / totalRetrieved;
		else
			return 0;
	}

	public static float recall(final int positiveMatch, final int totalPositive) {

		if (totalPositive > 0)
			return (float) positiveMatch / totalPositive;
		else
			return 0;
	}

	/**
	 * Fitness is based on the F1 measure which combines precision and recall
	 */
	public static float f1(final int positiveMatch, final int negativeMatch,
			final int totalPositive) {

		if (positiveMatch <= 0 || totalPositive <= 0) {
			return 0;
		}

		final float recall = recall(positiveMatch, totalPositive);
		final float precision = precision(positiveMatch, negativeMatch);

		return (2 * precision * recall) / (precision + recall);
	}

	/**
	 * Break even point. Alternative (older) measure of classification accuracy
	 */
	public static float bep(int positiveMatch, int negativeMatch,
			int totalPositive) {

		if (positiveMatch <= 0 || totalPositive <= 0) {
			return 0;
		}
		final float recall = recall(positiveMatch, totalPositive);
		final float precision = precision(positiveMatch, negativeMatch);

		return (precision + recall) / 2;
	}
}
