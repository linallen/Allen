package step1.distribution;

/**
 * Exponential distribution. P(x) = exp(x)
 * 
 * @author Allen, 22 Sep 2016
 */
public class DistExp extends Distribution {
	/** paras[0]: x */
	@Override
	public double P(Object... paras) {
		double p = Math.exp((Double) paras[0]);
		return p;
	}
}