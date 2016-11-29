package aai.base.table;

import java.io.Serializable;

/**
 * A Matrix = [key1, key2, value], where value is indexed by String
 * key=[key1.hashCode_key2.hashCode].
 * 
 * @author Allen Lin, 23 June 2016
 */
public class Matrix implements Serializable {
	private static final long serialVersionUID = -5282826794782001765L;

	/** sub-class of MatrixBase need to instantialize Table */
	private TableBase m_Matrix;

	/** symmetric matrix? */
	private boolean m_symmetric;

	public Matrix(boolean symmetric) {
		m_symmetric = symmetric;
		m_Matrix = symmetric ? new TableKeyLst() : new TableKeySet();
	}

	public boolean symmetric() {
		return m_symmetric;
	}

	/** main functions ***************************************/
	/** put in [keys[], value] */
	public void put(Object key1, Object key2, Object value) {
		m_Matrix.put(value, key1, key2);
	}

	/** get value by keys[] */
	public Object remove(Object key1, Object key2) {
		return m_Matrix.remove(key1, key2);
	}

	/** get value by keys[] */
	public Object get(Object key1, Object key2) {
		return m_Matrix.get(key1, key2);
	}

	/** output functions ***************************************/
	public String toString() {
		return (symmetric() ? "" : "a") + "symmetric matrix\n" + m_Matrix.toString();
	}

	public static void main(String[] args) throws Exception {
		Matrix mb = new Matrix(true);
		mb.put(1, 2, "Allen");
		mb.put(1, 0, "Hello");
		mb.put(0, 1, "World");
		System.out.println(mb.toString());

		mb = new Matrix(false);
		mb.put(1, 2, "Allen");
		mb.put(1, 0, "Hello");
		mb.put(0, 1, "World");
		System.out.println(mb.toString());
	}
}