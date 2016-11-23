package allen.base.set;

/**
 * interface of Set.
 * 
 * @author Allen Lin, 18 June 2016.
 */
public interface AllenSet {
	public int size();

	public default boolean isEmpty() {
		return size() == 0;
	}
}