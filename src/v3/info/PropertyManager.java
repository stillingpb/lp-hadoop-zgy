package v3.info;

public class PropertyManager {
	/**
	 * 信息传播时，顶点的传播策略
	 */
	public static final BetaParam BETA_PARAM = BetaParam.RANDOM_OUT;

	/**
	 * 信息传播时，控制传播节点的比例，例如alphaParam=0.3f,且顶点有10个出度顶点，那么选择30%，也就是3个顶点来传递信息
	 */
//	public static final float ALPHA_PARAM = 1f;
	public static final float ALPHA_PARAM = 0.7f;

	/**
	 * 信息需要传播的轮次
	 */
	public static final int END_SPREAD_ROUND = 2;

	/**
	 * 设置信息的初始传播顶点,即数据源顶点
	 */
	public static final int BEGIN_SPREAD_VERTEX = 1;
}
