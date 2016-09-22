package distribution;

public class TestExp {
	static double minX = 1, maxX = 13;

	public static void main(String[] args) {
		DistExpStudent dist = new DistExpStudent();
		dist.setParas(0.1, 0.05, minX, maxX);
		for (double x = minX; x <= maxX; x++) {
			double rand = dist.P(x);
			System.out.println(rand);
		}
	}
}