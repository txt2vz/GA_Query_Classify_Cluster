package classify;
public abstract class ClassifyQuery  {


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
