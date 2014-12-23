package samuelpalmer.sensorautorotation.unused.facedetection;

import android.hardware.Camera.Size;

import java.util.List;

public class OptimalSizeChooser {

	private int targetWidth;
	private List<Size> sizes;

	public OptimalSizeChooser(List<Size> sizes, int targetWidth) {
		this.sizes = sizes;
		this.targetWidth = targetWidth;
	}

	public Size choose() {
		Size best = sizes.get(0);

		for (Size current : sizes)
			if (best.width < targetWidth && current.width >= targetWidth
					|| best.width >= targetWidth && current.width < best.width)
				best = current;

		return best;
	}

}
