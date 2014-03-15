package v2.label;

import v2.label.ILabelChoosen.MaxEqualChoosen;
import v2.label.ILabelChoosen.RandomEqualChoosen;

public class LabelChoosenFactory {

	public static ILabelChoosen createLabelChoosenFactory(String method) {
		if ("max".equals(method))
			return new MaxEqualChoosen();
		if ("random".equals(method))
			return new RandomEqualChoosen();
		return null;
	}
}
