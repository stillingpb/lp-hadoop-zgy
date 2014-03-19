package v3.label;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;

public interface ILabelChoosen {

	/**
	 * 从本身标签 和 邻居节点的标签中，选择出一个新的标签
	 * 
	 * @param neighberLabels
	 *            格式为<label,appearTime>
	 * @param nativeLabel
	 *            需要获得新标签的顶点的自身的标签
	 * @return 选择出的节点的新的标签
	 */
	public int chooseLabel(Map<Integer, Integer> neighberLabels, int nativeLabel);

	/**
	 * 在拥有相同数量的标签中，选择标签值最大的一个标签
	 * 
	 * @author pb
	 * 
	 */
	public static class MaxEqualChoosen implements ILabelChoosen {
		@Override
		public int chooseLabel(Map<Integer, Integer> neighberLabels,
				int nativeLabel) {
			int maxLabelSum = 1;
			int maxLabel = nativeLabel;
			for (Entry<Integer, Integer> entry : neighberLabels.entrySet()) {
				if (maxLabelSum < entry.getValue()) {
					maxLabelSum = entry.getValue();
					maxLabel = entry.getKey();
				} else if (maxLabelSum == entry.getValue()) {
					if (maxLabel < entry.getKey())
						maxLabel = entry.getKey();
				}
			}
			return maxLabel;
		}
	}

	/**
	 * 在拥有相同数量的标签中，随机选择一个标签
	 * 
	 * @author pb
	 * 
	 */
	public static class RandomEqualChoosen implements ILabelChoosen {
		private static Random random = new Random(); // 产生随机数

		@Override
		public int chooseLabel(Map<Integer, Integer> neighberLabels,
				int nativeLabel) {
			int maxLabelSum = 1;
			List<Integer> candidateLabel = new ArrayList<Integer>();
			candidateLabel.add(nativeLabel);
			for (Entry<Integer, Integer> entry : neighberLabels.entrySet()) {
				if (maxLabelSum < entry.getValue()) {
					maxLabelSum = entry.getValue();
					candidateLabel.clear();
					candidateLabel.add(entry.getValue());
				} else if (maxLabelSum == entry.getValue()) {
					candidateLabel.add(entry.getValue());
				}
			}
			return candidateLabel.get(random.nextInt(candidateLabel.size()));
		}
	}
}
