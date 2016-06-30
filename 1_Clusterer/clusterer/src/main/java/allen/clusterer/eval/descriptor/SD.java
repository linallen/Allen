package allen.clusterer.eval.descriptor;

/**
 * Used for evaluating similarity measures, Descriptor SD (Sum-Dissimilarity) is
 * the sum of object dissimilarities within all the clusters. Smaller SD means
 * better similarity measure.
 * 
 * @author Allen Lin, 29 Mar 2016
 */
public class SD extends Descriptor {
	/** return descriptor value SD: the bigger, the better */
	@Override
	protected String getDescriptor() {
		return m_intraSum + "";
	}
}