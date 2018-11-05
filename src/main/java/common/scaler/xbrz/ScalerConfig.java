package common.scaler.xbrz;

public class ScalerConfig {
	public final double luminanceWeight;
	public final double equalColorTolerance;
	public final double dominantDirectionThreshold;
	public final double steepDirectionThreshold;

	public ScalerConfig() {
		// These are the default values:
		this(1, 30, 3.6, 2.2);
	}

	public ScalerConfig(double luminanceWeight, double equalColorTolerance, double dominantDirectionThreshold, double steepDirectionThreshold) {
		this.luminanceWeight = luminanceWeight;
		this.equalColorTolerance = equalColorTolerance;
		this.dominantDirectionThreshold = dominantDirectionThreshold;
		this.steepDirectionThreshold = steepDirectionThreshold;
	}
}
