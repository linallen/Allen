package distribution;

/**
 * Base class of distribution.
 * 
 * @author Allen, 22 Sep 2016
 */
public abstract class Distribution {

	/** 1. set parameters for the distribution */
	public void setParas(Object... paras) {
	};

	/** 2. get the possibility */
	public abstract double P(Object... paras);
}
