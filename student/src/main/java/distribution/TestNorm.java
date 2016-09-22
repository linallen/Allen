package distribution;

public class TestNorm {

	public static void main(String[] args) {
		DistNorm dist = new DistNorm();
		dist.setParas(0.2, 0.1);
		for (int i = 0; i < 100; i++) {
			double rand = dist.P();
			System.out.println(rand);
		}
	}
}