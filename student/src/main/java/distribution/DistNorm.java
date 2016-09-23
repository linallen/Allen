package distribution;

/**
 * Normal (Guassian) distribution. P(x) = Guassian(sd, mean)
 * 
 * @author Allen, 22 Sep 2016
 */
public class DistNorm extends Distribution {
	/** mean */
	protected Double m_mean;
	/** standard deviation */
	protected Double m_sd;

	public DistNorm(Object... paras) {
		setParas(paras);
	}

	/**
	 * paras[0]: mean<br>
	 * paras[1]: standard deviation
	 */
	@Override
	public void setParas(Object... paras) {
		if (paras.length >= 2) {
			m_mean = (Double) paras[0];
			m_sd = (Double) paras[1];
		}
	}

	/** No parameter */
	@Override
	public double P(Object... paras) {
		double p = m_random.nextGaussian() * m_sd + m_mean;
		return Math.max(Math.min(1., p), 0.);
	}

	public String toString() {
		return "Mean = " + m_mean + ", StdDev = " + m_sd;
	}
}