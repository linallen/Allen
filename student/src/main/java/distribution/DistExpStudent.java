package distribution;

/**
 * Exponential distribution.<br>
 * P(x) = {[exp(x)-exp(minX)] / [exp(maxX)-exp(minX)]} + norm(mean, sd)
 * 
 * @author Allen, 22 Sep 2016
 */
public class DistExpStudent extends Distribution {
	/** min(x), max(x) */
	protected double m_minX, m_maxX;
	/** perturbation Guassian random number generator */
	protected DistNorm m_distNorm = new DistNorm();

	public DistExpStudent(Object... paras) {
		setParas(paras);
	}

	/**
	 * paras[0]: perturbation mean<br>
	 * paras[1]: perturbation standard deviation<br>
	 * paras[2]: minX<br>
	 * paras[3]: maxX<br>
	 */
	@Override
	public void setParas(Object... paras) {
		if (paras.length >= 4) {
			m_distNorm.setParas(paras);
			m_minX = (Double) paras[2];
			m_maxX = (Double) paras[3];
		}
	}

	private static double exp(double x) {
		return Math.exp(x);
	}

	/** paras[0]: x */
	@Override
	public double P(Object... paras) {
		assert (paras.length >= 1);
		double x = (Double) paras[0];
		assert ((x >= m_minX) && (x <= m_maxX));
		double normedExp = (exp(x) - exp(m_minX)) / (exp(m_maxX) - exp(m_minX));
		double p = normedExp + m_distNorm.P();
		return Math.max(Math.min(1., p), 0);
	}

	public String toString() {
		return m_distNorm.toString() + ", minX = " + m_minX + ", maxX = " + m_maxX;
	}
}