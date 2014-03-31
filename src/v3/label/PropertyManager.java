package v3.label;

/**
 * 本系统的可调参数
 * 
 */
public class PropertyManager {
	/**
	 * 标签传递算法停止的阈值
	 */
	public static final double STOP_THREAD = 0.99;

	/**
	 * lp算法优化是否打开
	 * 
	 * 如果打开，算法迭代时间减少，但会影响社团划分效果
	 * 
	 * 如果关闭，算法迭代时间变长，但会提高社团划分精度
	 */
	public static final boolean LPA_OPTIMIZE_SHUTDOWN = false;
	/**
	 * lp算法优化的上下界，在这个范围内，如果一些节点的标签没有发生变化，
	 * 
	 * 那么这些节点的标签就被设置为最终标签，不参与到下一轮lp算法迭代中， 减少处理时间
	 */
	public static final double LPA_OPTIMIZE_LOWER_LIMIT = 0.9; // 上界
	public static final double LPA_OPTIMIZE_UPPER_LIMIT = 0.95;// 下界

	/**
	 * 标签选择时采用的方法
	 */
	public static final String LABEL_CHOOSEN_METHOD = LabelChoosenFactory.MAX_LABEL_CHOOSEN;

	/**
	 * 控制是否采用异步方式传递标签
	 */
	public static final boolean LABEL_PROPAGATION_ASYNCHRONOUS = true;
}
