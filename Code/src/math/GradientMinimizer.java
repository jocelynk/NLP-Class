package math;

public interface GradientMinimizer {
	double[] minimize(DifferentiableFunction function, double[] initial,
					  double tolerance);
}
