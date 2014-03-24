package v3.info;

/**
 * 系统的beta参数，控制顶点的选取策略
 *
 */
public enum BetaParam {
	/**
	 * 取出度最大的顶点
	 */
	TOP_OUT,
	/**
	 * 取出度最小的顶点
	 */
	TAIL_OUT,
	/**
	 * 随机选择顶点
	 */
	RANDOM_OUT
}
