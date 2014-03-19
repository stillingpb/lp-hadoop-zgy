package v3.label;

import org.apache.hadoop.mapreduce.Counter;

import v3.label.OptimizeController.OptimizeState;

/**
 * 决裁lp算法的迭代行为，是否停止
 * 
 * 
 */
public class JudgeMonitor {
	/**
	 * 计数器group
	 */
	public static final String COUNTER_GROUP = "lp";

	/**
	 * vertexNum计数器的名称
	 */
	public static final String COUNTER_VERTEX_NUM = "vertex_num";

	/**
	 * graphEdgeNum计数器的名称
	 */
	public static final String COUNTER_EDGES = "graph_edges";

	/**
	 * lableChangedVertex计数器的名称
	 */
	public static final String COUNTER_VERTEX_LABEL_CHANGED = "vertex_label_changed";

	/**
	 * optimizeVertexCounter计数器的名称
	 */
	public static final String COUNTER_VERTEX_OPTIMIZE = "vertext_label_optimize";

	/**
	 * 统计图中顶点数量的计数器
	 */
	private Counter vertexNum;

	/**
	 * 图的边数
	 */
	private Counter graphEdgeNum;

	/**
	 * 统计当前标签传递中，有多少顶点的标签发生变化
	 */
	private Counter lableChangedVertex;

	/**
	 * 统计在lp优化算法中，在当前迭代过程中，有多少个节点免处理
	 */
	private Counter optimizeVertexCounter;

	/**
	 * 记录算法的迭代次数
	 */
	private int reverseTimes = 0;

	/**
	 * 获取未改变标签占总标签的比例
	 * 
	 * @return unchangedPercent
	 */
	public double getUnchangedPercent() {
		if (lableChangedVertex == null)
			return 0.0;
		long changed = lableChangedVertex.getValue();
		long sum = vertexNum.getValue();
		return 1 - 1.0 * changed / sum;
	}

	/**
	 * 判断lp迭代是否应该停止
	 * 
	 * @param lableChangedVertex
	 *            统计标签发生变化的顶点的计数器
	 * @return true 继续迭代 false 停止迭代
	 */
	public boolean judgeReverseStop() {
		double unchangedPercent = getUnchangedPercent();
		return unchangedPercent < PropertyManager.STOP_THREAD;
	}

	/**
	 * 打印一次lp算法迭代的效果
	 */
	public void printOneReverseEffect(OptimizeState state) {
		long changed = lableChangedVertex.getValue();
		long sum = vertexNum.getValue();
		double unchangedPercent = getUnchangedPercent();

		String ifContinueReverse;
		boolean continueReverse = unchangedPercent >= 1 - PropertyManager.STOP_THREAD;
		if (continueReverse)
			ifContinueReverse = "continue lp Reverse";
		else
			ifContinueReverse = "stop lp Reverse";
		System.out.println("\nreverse Times = " + this.reverseTimes
				+ "\nstop thread = " + PropertyManager.STOP_THREAD
				+ "\t\tunchanged percent = " + unchangedPercent
				+ "\nall label = " + sum + "\t\tlabel changed = " + changed
				+ "\t\tlabel unchanged = " + (sum - changed));

		System.out.println("optimizeState = " + state + "\t\tlower_limit = "
				+ PropertyManager.LPA_OPTIMIZE_LOWER_LIMIT
				+ "\t\tupper_limit = "
				+ PropertyManager.LPA_OPTIMIZE_UPPER_LIMIT);
		System.out.println("免处理节点 = " + optimizeVertexCounter.getValue());
		System.out.println(ifContinueReverse + "------------------------\n");
	}

	/**
	 * 增加迭代次数
	 * 
	 * @param added
	 */
	public void incrementReverseNum(int added) {
		this.reverseTimes += added;
	}

	/**
	 * 输出运行结果
	 */
	public void printResult() {
		System.out.println("处理结果：\n系统共处理节点 = " + this.vertexNum.getValue()
				+ "\n共处理边 = " + this.graphEdgeNum.getValue() + "\n迭代次数 = "
				+ this.reverseTimes + "\n");
	}

	public Counter getVertexNum() {
		return vertexNum;
	}

	public void setVertexNum(Counter vertexNum) {
		this.vertexNum = vertexNum;
	}

	public Counter getLableChangedVertex() {
		return lableChangedVertex;
	}

	public void setLableChangedVertex(Counter lableChangedVertex) {
		this.lableChangedVertex = lableChangedVertex;
	}

	public Counter getGraphEdgeNum() {
		return graphEdgeNum;
	}

	public void setGraphEdgeNum(Counter graphEdgeNum) {
		this.graphEdgeNum = graphEdgeNum;
	}

	public Counter getOptimizeVertexCounter() {
		return optimizeVertexCounter;
	}

	public void setOptimizeVertexCounter(Counter optimizeVertexCounter) {
		this.optimizeVertexCounter = optimizeVertexCounter;
	}

}
