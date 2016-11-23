package allen.address;

public class KwdSet extends KeySet {

	public Kwd addKwd(String key) {
		key = key.intern();
		if (super.exist(key)) {
			return (Kwd) super.get(key);
		}
		Kwd kwd = new Kwd(key);
		add(key, kwd);
		return kwd;
	}
}