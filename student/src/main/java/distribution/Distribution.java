package distribution;

import java.util.Random;

/**
 * Base class of distribution.
 * 
 * @author Allen, 22 Sep 2016
 */
public abstract class Distribution {
	protected static Random m_random = new Random();

	/** set parameters for the distribution */
	public void setParas(Object... paras) {
	};

	/** get the possibility P (between 0.0 and 1.0 inclusively) */
	public abstract double P(Object... paras);

	/** return if an event happens */
	public final boolean hit(Object... paras) {
		return m_random.nextDouble() <= P(paras);
	}

	public static boolean hitUniform(double min, double max, double num) {
		return m_random.nextDouble() <= (num - min) / (max - min);
	}

	public static boolean hitUniform(double num) {
		return hitUniform(0, 1., num);
	}
}