package step1.distribution;

public class TestExpStudent {
	static double minX = 1, maxX = 89;

	public static void main(String[] args) {
		DistExpStudent dist = new DistExpStudent();
		dist.setParas(0.15, 0.05, minX, (Double) (maxX / 7 + 1));
		for (double x = minX; x <= maxX; x++) {
			double rand = dist.P(x / 7.0 + 0.5);
			System.out.println(rand);
		}
	}
}