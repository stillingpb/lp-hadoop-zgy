package v2.label;

import v2.label.ILabelChoosen.MaxEqualChoosen;
import v2.label.ILabelChoosen.RandomEqualChoosen;

public class LabelChoosenFactory {

	public static final String MAX_LABEL_CHOOSEN = "max";
	public static final String RANDOM_LABEL_CHOOSEN = "random";

	public static ILabelChoosen createLabelChoosenFactory(String method) {
		if (MAX_LABEL_CHOOSEN.equals(method))
			return new MaxEqualChoosen();
		if (RANDOM_LABEL_CHOOSEN.equals(method))
			return new RandomEqualChoosen();
		return null;
	}
}
